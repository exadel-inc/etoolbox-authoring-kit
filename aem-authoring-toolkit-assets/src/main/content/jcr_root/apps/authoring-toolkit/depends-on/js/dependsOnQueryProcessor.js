/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.2.2
 *
 * DependsOn Query Processor
 * parse & compile DependsOn queries
 * */
(function (document, $, ns) {
    'use strict';

    const REFERENCE_REGEXP = /@(@)?(\w+)([\s]*\(([^)]+)\))?/g;

    class QueryProcessor {
        /**
         * @readonly
         * */
        static get REFERENCE_REGEXP() { return REFERENCE_REGEXP; }

        /**
         * Evaluate parsed query
         * @param {string} query - parsed query
         * @param {object} context - context to execute
         * */
        static evaluateQuery(query, context) {
            const refs = [].concat(ns.ElementReferenceRegistry.refs).concat(ns.GroupReferenceRegistry.refs);
            try {
                const args = refs.map((ref) => ref.id).join(',');
                const exec = new Function(args, 'return ' + query + ';'); //NOSONAR: not a javascript:S3523 case, real evaluation should be done
                return exec.apply(context || null, refs);
            } catch (e) {
                console.error('[DependsOn]: error while evaluating "' + query + '" using ', refs, e);
            }
        }

        /**
         * Parse query to evaluable one, replace references definitions by reference instances aliases
         * @param {string} query
         * @param {JQuery} $root
         * @param {function} [changeHandlerCB]
         * */
        static parseQuery(query, $root, changeHandlerCB) {
            return query.replace(REFERENCE_REGEXP, (q, isGroup, name, selWrapper, sel) => {
                const $context = ns.findBaseElement($root, sel);

                if (name === 'this' && (isGroup || sel)) {
                    console.warn(`[DependsOn]: ${q} is always referencing current element, could be replaced by simple @this`);
                }

                const reference = name === 'this' ?
                    ns.ElementReferenceRegistry.registerElement($root) :
                    isGroup ?
                        ns.GroupReferenceRegistry.register(name, $context) :
                        ns.ElementReferenceRegistry.register(name, $context);

                reference.subscribe(changeHandlerCB);
                return `${reference.id}.value`;
            });
        }
    }
    ns.QueryProcessor = QueryProcessor;
})(document, Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
