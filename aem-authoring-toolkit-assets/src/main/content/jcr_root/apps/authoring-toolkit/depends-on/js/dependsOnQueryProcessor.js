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
    const REFERENCE_REGEXP = /@(@)?(\w+)([\s]*\(([^)]+)\))?/g;

    class QueryProcessor {
        static get REFERENCE_REGEXP() { return REFERENCE_REGEXP; }

        /**
         * Evaluate query
         * */
        static evaluateQuery(query, context) {
            const refs = [].concat(ns.ElementReferenceRegistry.refs).concat(ns.GroupReferenceRegistry.refs).concat(ns.ElementAccessors);
            try {
                const args = refs.map((ref) => ref.id).join(',');
                const exec = new Function(args, 'return ' + query + ';'); //NOSONAR: not a javascript:S3523 case, real evaluation should be done
                return exec.apply(context || null, refs);
            } catch (e) {
                console.error('[DependsOn]: error while evaluating "' + query + '" using ', refs, e);
            }
        }

        /**
         * Register new query
         * {String} query
         * {JQueryElement} $root
         * {Function} [cb]
         * */
        static registerQuery(query, $root, changeHandlerCB) {
            return query.replace(REFERENCE_REGEXP, (q, isGroup, name, selWrapper, sel) => {
                const $context = QueryProcessor.findBaseElement($root, sel);
                const reference = isGroup ?
                    ns.GroupReferenceRegistry.register(name, $context) :
                    ns.ElementReferenceRegistry.register(name, $context);

                reference.subscribe(changeHandlerCB);
                return `${reference.id}.value`;
            });
        }

        /**
         * Find element by provided selector. Use back-forward search:
         * First part of selector will be used to find closest element
         * If the second part after '|>' provided will search back element by second part of selector inside of closest parent
         * founded on the previous state.
         * If 'this' passed as a sel $root will be returned
         * If sel is not provided then result will be $(document).
         *
         * @param $root {JQuery}
         * @param sel {string}
         * */
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
    }
    ns.QueryProcessor = QueryProcessor;
})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
