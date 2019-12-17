/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.0.0
 *
 * DependsOn Query Processor
 * parse & compile DependsOn queries
 * */
(function (document, $, ns) {
    'use strict';

    const $document = $(document);
    const REFERENCE_REGEXP = /@(\w+)([\s]*\(([^)]+)\))?/g;

    class QueryProcessor {
        static get REFERENCE_REGEXP() { return REFERENCE_REGEXP; }

        /**
         * Subscribe callback cb to the references used in the parsed query
         * */
        static subscribeQuery(query, cb) {
            QueryProcessor.getReferences(query).map(ns.ReferenceRegistry.getById).forEach((ref) => {
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
            const ids = ns.ReferenceRegistry.ids;
            const args = ids.join(',');
            const refs = ns.ReferenceRegistry.refs;

            try {
                const exec = new Function(args, 'return ' + query + ';'); //NOSONAR: not a javascript:S3523 case, real evaluation should be done
                return exec.apply(context || null, refs);
            } catch (e) {
                console.log('[DependsOn]: error while evaluating "' + query + '" using ', refs, e);
            }
        }

        /**
         * Register new query
         * {String} query
         * {JQueryElement} $root
         * {Function} [cb]
         * */
        static registerQuery(query, $root, changeHandlerCB) {
            const processedQuery = QueryProcessor._processQuery(query, $root);
            QueryProcessor.subscribeQuery(processedQuery, changeHandlerCB);
            return processedQuery;
        }

        /**
         * @private
         * */
        static _processQuery(query, $root) {
            return query.replace(REFERENCE_REGEXP, (q, id, selWrapper, sel) => {
                const $el = QueryProcessor.findBaseElement($root, sel).find('[data-dependsonref="' + id + '"]');
                const ref = ns.ReferenceRegistry.registerElement($el).id;
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
    ns.QueryProcessor = QueryProcessor;
})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
