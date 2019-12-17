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
            const refs = [].concat(ns.ReferenceRegistry.refs).concat(ns.HeavyReferenceRegistry.refs);
            try {
                const args = refs.map((ref) => ref.id).join(',');
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
            return query.replace(REFERENCE_REGEXP, (q, heavy, id, selWrapper, sel) => {
                const $context = QueryProcessor.findBaseElement($root, sel);
                const reference = QueryProcessor.getReference(id, $context, !!heavy);

                reference.subscribe(changeHandlerCB);
                return `${reference.id}.value`;
            });
        }

        static getReference(id, $context, isHeavy) {
            if (isHeavy) {
                return  ns.HeavyReferenceRegistry.registerElement(id, $context);
            } else {
                const $el = $context.find('[data-dependsonref="' + id + '"]');
                return  ns.ReferenceRegistry.registerElement($el);
            }
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
    }
    ns.QueryProcessor = QueryProcessor;
})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
