/**
 * @author Alexey Stsefanovich (ala'n), Yana Bernatskaya (YanaBr)
 *
 * DependsOn Actions Registry
 * Action defines steps to process query result
 * */
(function ($, ns) {
    'use strict';

    const NAME_REGEX = /[^a-z0-9-]+/g;
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
         * Function to remove symbols that don't match the pattern
         * @param {string} target - target string
         * */
        static sanitizeName(target) {
            return target.toLowerCase().replace(NAME_REGEX, '');
        }

        /**
         * @param {string} name - action name
         * @returns {function} action callback
         * */
        static getAction(name) {
            const sanitizedName = ActionRegistry.sanitizeName(name);
            if (name !== sanitizedName) {
                console.warn(`[DependsOn]: Action "${sanitizedName}" accessed via incorrect name "${name}" (allowed symbols: a-z, 0-9, -)`);
            }
            const action = actionRegistryMap[sanitizedName];
            if (typeof action !== 'function') {
                const knownActions = ActionRegistry.registeredActionNames.map((key) => `"${key}"`).join(', ');
                throw new Error(`[DependsOn]: Action "${name}" doesn't have a valid definition in DependsOnPlugin.ActionRegistry. Known actions: ${knownActions}`);
            }
            return action;
        }

        /**
         * @param {string} name - is action name
         * @param {function} actionFn - function to set state (queryResult: any) => void
         * @returns {function} actual actionCb after register
         * */
        static register(name, actionFn) {
            name = name.trim();
            const sanitizedName = ActionRegistry.sanitizeName(name);
            if (name !== sanitizedName) {
                console.warn(`[DependsOn]: Action's name "${name}" was sanitized to "${sanitizedName}" (allowed symbols: a-z, 0-9, -)`);
            }
            if (typeof actionFn !== 'function') {
                throw new Error(`[DependsOn]: Action ${actionFn} is not a valid action definition`);
            }
            if (actionRegistryMap[sanitizedName]) {
                console.warn(`[DependsOn]: Action ${sanitizedName} was overridden by ${actionFn}`);
            }
            actionRegistryMap[sanitizedName] = actionFn;
            return actionFn;
        }
    }

    ns.ActionRegistry = ActionRegistry;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
