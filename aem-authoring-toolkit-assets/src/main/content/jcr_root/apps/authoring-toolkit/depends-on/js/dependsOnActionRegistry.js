/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * DependsOn Actions Registry
 * Action defines steps to process query result
 * */
(function ($, ns) {
    'use strict';

    const actionRegistryMap = {};
    class ActionRegistry {
        /**
         * @param name {string} - action name
         * @returns {function} action callback
         * */
        static getAction(name) {
            const action = actionRegistryMap[name];
            if (typeof action !== 'function') {
                throw new Error(`[DependsOn]: Action ${action} doesn't have a valid definition in DependsOnPlugin.ActionRegistry`);
            }
            return action;
        }

        /**
         * @param name {string} - is action name
         * @param actionFn {function} - function to set state (queryresult: any) => void
         * @returns {function} actual actionCb after register
         * */
        static register(name, actionFn) {
            if (typeof actionFn !== 'function') {
                throw new Error(`[DependsOn]: Action ${actionFn} is not a valid action definition`);
            }
            if (actionRegistryMap[name]) {
                console.log(`[DependsOn]: Action ${name} was overridden`);
            }
            return actionRegistryMap[name] = actionFn;
        }
    }

    ns.ActionRegistry = ActionRegistry;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
