/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.1.0
 *
 * DependsOn Actions Registry
 * Action defines steps to process query result
 * */
(function ($, ns) {
    'use strict';

    const actionRegistryMap = {};
    class ActionRegistry {
        /**
         * Default action name
         * */
        static get DEFAULT () { return 'visibility'; }

        /**
         * @param {string} name - action name
         * @returns {function} action callback
         * */
        static getAction(name) {
            const action = actionRegistryMap[name];
            if (typeof action !== 'function') {
                const knownActions = Object.keys(actionRegistryMap).map((key) => `"${key}"`).join(', ');
                throw new Error(`[DependsOn]: Action "${name}" doesn't have a valid definition in DependsOnPlugin.ActionRegistry. Known actions: ${knownActions}`);
            }
            return action;
        }

        /**
         * @param {string} name - is action name
         * @param {function} actionFn - function to set state (queryresult: any) => void
         * @returns {function} actual actionCb after register
         * */
        static register(name, actionFn) {
            if (typeof actionFn !== 'function') {
                throw new Error(`[DependsOn]: Action ${actionFn} is not a valid action definition`);
            }
            if (actionRegistryMap[name]) {
                console.warn(`[DependsOn]: Action ${name} was overridden by ${actionFn}`);
            }
            return actionRegistryMap[name] = actionFn;
        }
    }

    ns.ActionRegistry = ActionRegistry;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
