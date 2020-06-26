/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.2.2
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
        static get DEFAULT() {
            return 'visibility';
        }

        /**
         * Registered DependsOn action names
         * @returns {string[]}
         * */
        static get registeredActionNames() {
            return Object.keys(actionRegistryMap);
        }

        /**
         * @param {string} name - action name
         * @returns {function} action callback
         * */
        static getAction(name) {
            const action = actionRegistryMap[name];
            if (typeof action !== 'function') {
                const knownActions = ActionRegistry.registeredActionNames.map((key) => `"${key}"`).join(', ');
                throw new Error(`[DependsOn]: Action "${name}" doesn't have a valid definition in DependsOnPlugin.ActionRegistry. Known actions: ${knownActions}`);
            }
            return action;
        }

        /**
         * Function to remove symbols that don't match to the pattern
         * @param {string} target - target string
         * @param {RegExp} pattern -  pattern for correct symbols
         * */
        static _replaceForbiddenSymbols(target, pattern) {
           return target.replace(new RegExp(pattern + '|.', 'g'),
               (match) => (match.search(pattern) !== -1  ? match : ''));
        }

        /**
         * Function to develop action's name (allowed symbols a-z, 0-9, -)
         * @param {string} name - action name
         * @returns {string} correct action name (according to nameRegex)
         * */
        static _refactorName(name) {
            const nameRegex = /^[a-z0-1-]+$/;
            let resultName = name;
            if (name.trim().search(nameRegex) === -1) {
                resultName = resultName.toLocaleLowerCase();
                resultName = this._replaceForbiddenSymbols(resultName, nameRegex);
                console.warn(`[DependsOn]: Action's name ${name} was overridden by ${resultName} (allowed symbols: a-z, 0-9, -)`);
            }
            return resultName;
        }

        /**
         * @param {string} name - is action name
         * @param {function} actionFn - function to set state (queryResult: any) => void
         * @returns {function} actual actionCb after register
         * */
        static register(name, actionFn) {
            name = this._refactorName(name);
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
