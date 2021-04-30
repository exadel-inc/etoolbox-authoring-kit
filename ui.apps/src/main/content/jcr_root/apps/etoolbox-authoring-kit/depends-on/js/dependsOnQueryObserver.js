/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Alexey Stsefanovich (ala'n)
 *
 * QueryObserver compiles a query using the QueryProcessor and applies the defined action to the target.
 * The target supports multiple actions and queries separated with ';'.
 *
 * Attributes:
 * data-dependson - condition queries separated with ';'
 * data-dependsonaction - action types separated with ';'
 * data-dependsonskipinitial - the marker to skip the initial update.
 *
 * NOTE:
 * Condition query is a plain JS expression with special syntax for references.
 * Single condition query should not contain ';'. You need to move complex logic to separate functions / hidden fields
 * */
(function ($, ns) {
    'use strict';

    const OBSERVERS_SQ = ns.createSequence();

    /**
     * DependsOn Observer (Target) instance.
     * Attached to a dependent element.
     * Initiates references registration
     * */
    class QueryObserver {
        static get DATA_STORE() { return 'dependsonobserver'; }

        /**
         * Initialize dependson observer instances on the target
         * @param {JQuery} $el - target element
         * */
        static init($el) {
            $el = ns.ElementAccessors.findTarget($el);

            if ($el.data(QueryObserver.DATA_STORE)) return $el.data(QueryObserver.DATA_STORE);

            const queries = ns.splitAndTrim($el.attr('data-dependson') || '');
            const actions = ns.splitAndTrim($el.attr('data-dependsonaction') || ns.ActionRegistry.DEFAULT);
            if (actions.length !== queries.length) {
                throw new Error('[DependsOn]: The numbers of actions and queries do not match');
            }

            // Initialize observers
            const observers = QueryObserver.initObserversList($el, queries, actions);

            // Initial update
            if (!$el.is('[data-dependsonskipinitial]')) {
                observers.forEach((observer) => observer.update());
            }

            $el.data(QueryObserver.DATA_STORE, observers);
        }

        /**
         * Initialize observer instances
         * @param {JQuery} $el
         * @param {string[]} queries
         * @param {string[]} actions
         * */
        static initObserversList($el, queries, actions) {
            const actionCounter = {};
            return queries.map((query, i) => {
                const action = actions[i];
                actionCounter[action] = actionCounter[action] || 0;
                const data = ns.parseActionData($el[0], action, actionCounter[action]++);
                return new QueryObserver($el, query, action, data);
            });
        }

        /**
         * Update observers instances
         * @param {JQuery} $el
         * @param {string[]} [actions]
         * @return {boolean} operation's state
         * */
        static updateObservers($el, actions) {
            $el = ns.ElementAccessors.findTarget($el);

            const observers = $el.data(QueryObserver.DATA_STORE);
            if (!observers || !observers.length) return false;

            const targetObservers = actions ?
                observers.filter((observer) => actions.indexOf(observer.action) !== -1) :
                observers;
            if (!targetObservers.length) return false;

            targetObservers.forEach((observer) => observer.update());

            return true;
        }

        /**
         * @property {JQuery} $el
         * @property {string} query
         * @property {string} action
         * @property {object} [data]
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
         * Request evaluation of the query and execute the action
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

    ns.QueryObserver = QueryObserver;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
