/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * Initialize DependsOnObserver - instance that linked with the target element.
 * DependsOnObserver compile query using DependsOnRegistry and apply defined acton to the target.
 * Supports multiple actions and queries separated by ';'
 *
 * Attributes:
 * data-dependson - condition queries separated by ';'
 * data-dependsonaction - action types separated by ';'
 * data-dependsonskipinitial - marker to skip initial update
 *
 * NOTE:
 * condition query is a plane JS expression with special syntax for references
 * single condition query should not contain ';' move your complex logic to separate functions / hidden fields
 * */
(function ($, ns) {
    'use strict';

    const SEPARATOR = ';';
    function splitAndTrim(value) {
        return value.split(SEPARATOR).map((term) => term.trim());
    }

    /**
     * DependsOn Observer (Target) instance
     * Attached to dependent element
     * Initiate references registration
     * */
    class DependsOnObserver {

        /**
         * Shortcut to initialize observer
         * */
        static init($el) {
            new DependsOnObserver($el);
        }

        constructor($el) {
            if ($el.data('dependsonobserver')) {
                return $el.data('dependsonobserver');
            }

            this.$el = $el;
            this.actions = splitAndTrim($el.attr('data-dependsonaction') || 'visibility');
            this.originalQueries = splitAndTrim($el.attr('data-dependson') || '');

            this.update = this.update.bind(this);

            if (this.actions.length !== this.originalQueries.length) {
                throw new Error('[DependsOn]: The number of actions and queries does not match');
            }

            // Delegate query registration to reference registry
            this.queries = this.originalQueries.map((query, index) => ns.ReferenceRegistry.registerQuery(
                query,
                this.$el,
                this.update.bind(this, index)
            ));

            $el.data('dependsonobserver', this);

            // Initial update
            if (!$el.is('[data-dependsonskipinitial]')) {
                this.updateAll();
            }
        }

        /**
         * Request evaluation of query and set state accordingly.
         * */
        update(index) {
            const queryResult = ns.ReferenceRegistry.evaluateQuery(this.queries[index], this.$el);
            this.setState(this.actions[index], queryResult);
        }
        /**
         * Request evaluation of all queries and set state accordingly.
         * */
        updateAll() {
            this.actions.forEach((action, index) => this.update(index));
        }

        /**
         * Execute action with the new state
         * */
        setState(actionName, value) {
            ns.ActionRegistry.getAction(actionName).call(this, value, this);
        }
    }

    ns.DependsOnObserver = DependsOnObserver;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
