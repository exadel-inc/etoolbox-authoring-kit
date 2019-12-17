/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * DependsOn Reference Registry
 * - hold Reference list
 * - register reference
 * - parse & compile queries
 * */
(function (document, $, ns) {
    'use strict';

    const $document = $(document);
    const REFERENCE_REGEXP = /@(\w+)([\s]*\(([^)]+)\))?/g;

    const valueMap = {};
    class ReferenceRegistry {
        static get REFERENCE_REGEXP() { return REFERENCE_REGEXP; }
        static get keys() { return Object.keys(valueMap); }
        static get valueMap() { return valueMap; }

        /**
         * Register ElementReference
         * Returns existing if it is already registered
         * */
        static registerElement($el) {
            const subj = new ns.ElementReference($el);
            valueMap[subj.id] = subj;
            return subj;
        }

        /**
         * Subscribe callback cb to the references used in the parsed query
         * */
        static subscribeQuery(query, cb) {
            ReferenceRegistry.getReferences(query).forEach((id) => {
                const ref = ReferenceRegistry.valueMap[id];
                if (ref) {
                    ref.subscribe(cb);
                } else {
                    console.error('[DependsOn] Reference ' + id + ' not found');
                }
            });
        }

        /**
         * Evaluate query
         * */
        static evaluateQuery(query, context) {
            try {
                const ids = ReferenceRegistry.keys;
                const args = ids.join(',');
                const values = ids.map((id) => valueMap[id]);

                const exec = new Function(args, 'return ' + query + ';'); //NOSONAR: not a javascript:S3523 case, real evaluation should be done
                return exec.apply(context || null, values);
            } catch (e) {
                console.log('[DependsOn]: error while evaluating "' + query + '" using ', valueMap, e);
            }
        }

        /**
         * Register new query
         * {String} query
         * {JQueryElement} $root
         * {Function} [cb]
         * */
        static registerQuery(query, $root, changeHandlerCB) {
            const processedQuery = ReferenceRegistry._processQuery(query, $root);
            ReferenceRegistry.subscribeQuery(processedQuery, changeHandlerCB);
            return processedQuery;
        }

        static cleanDetachedRefs() {
            ReferenceRegistry.keys.forEach((id) => {
                const value = valueMap[id];
                // Skip if referencing element is in actual html
                if (value.$el.closest('html').length > 0) return;
                // Delete reference otherwise
                delete valueMap[id];
                value.clean();
            });
        }

        /**
         * @private
         * */
        static _processQuery(query, $root) {
            return query.replace(REFERENCE_REGEXP, (q, id, selWrapper, sel) => {
                const $el = ReferenceRegistry.findBaseElement($root, sel).find('[data-dependsonref="' + id + '"]');
                const ref = ReferenceRegistry.registerElement($el).id;
                return `${ref}.value`;
            });
        }

        static findBaseElement($root, sel) {
            if (!sel) return $document;
            if (sel.trim() === 'this') return $root;
            const selParts = sel.split('|>');
            if (selParts.length > 1) {
                return $root.closest(selParts[0].trim()).find(selParts[1].trim());
            } else {
                return $root.closest(sel.trim());
            }
        }

        static getReferences(query) {
            const regexp = /(\$\w+)/g;
            const matchesMap = {};
            let match;
            while ((match = regexp.exec(query))) {
                matchesMap[match[1]] = true;
            }
            return Object.keys(matchesMap);
        }
    }
    ns.ReferenceRegistry = ReferenceRegistry;
})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
