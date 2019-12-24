/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.1.0
 *
 * Initialize DependsOnObservers - instances that are linked with the target element.
 * DependsOnObserver compile query using QueryProcessor and apply defined acton to the target.
 * Target supports multiple actions and queries separated by ';'
 *
 * Attributes:
 * data-dependson - condition queries separated by ';'
 * data-dependsonaction - action types separated by ';'
 * data-dependsonskipinitial - marker to skip initial update
 *
 * NOTE:
 * condition query is a plane JS expression with special syntax for references
 * single condition query should not contain ';' move complex logic to separate functions / hidden fields
 * */
(function ($, ns) {
    'use strict';

    const OBSERVERS_SQ = ns.createSequence();

    /**
     * DependsOn Observer (Target) instance
     * Attached to dependent element
     * Initiate references registration
     * */
    class DependsOnObserver {

        /**
         * @param $el {JQuery}
         * */
        static init($el) {
            if ($el.data('dependsonobserver')) return $el.data('dependsonobserver');

            const queries = ns.splitAndTrim($el.attr('data-dependson') || '');
            const actions = ns.splitAndTrim($el.attr('data-dependsonaction') || 'visibility');
            if (actions.length !== queries.length) {
                throw new Error('[DependsOn]: The number of actions and queries does not match');
            }

            // Initialize observers
            const observers = DependsOnObserver.initObserversList($el, queries, actions);

            // Initial update
            if (!$el.is('[data-dependsonskipinitial]')) {
                observers.forEach((observer) => observer.update());
            }

            $el.data('dependsonobserver', observers);
        }

        /**
         * Initialize observer instances
         * @param $el {JQuery}
         * @param queries {string[]}
         * @param actions {string[]}
         * */
        static initObserversList($el, queries, actions) {
            const actionCounter = {};
            return queries.map((query, i) => {
                const action = actions[i];
                actionCounter[action] = actionCounter[action] || 0;
                const data = ns.parseActionData($el[0], action, actionCounter[action]++);
                return new DependsOnObserver($el, query, action, data);
            });
        }

        /**
         * @constructor
         * @param $el {JQuery}
         * @param query {string}
         * @param action {string}
         * @param data {object}
         * */
        constructor($el, query, action, data = {}) {
            this.id = OBSERVERS_SQ.next();
            this.$el = $el;
            this.data = data;
            this.action = action;
            this.query = query;

            this.update = this.update.bind(this);
            this.parsedQuery = ns.QueryProcessor.parseQuery(this.query, this.$el, this.update);
        }

        /**
         * Request evaluation of query and execute action.
         * */
        update() {
            if (!this.$el || this.$el.closest('html').length === 0) {
                // Remove if detached
                return true;
            }
            const queryResult = ns.QueryProcessor.evaluateQuery(this.parsedQuery, this.$el);
            ns.ActionRegistry.getAction(this.action).call(this, queryResult, this.data, this);
        }
    }

    ns.DependsOnObserver = DependsOnObserver;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
