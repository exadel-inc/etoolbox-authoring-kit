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
 * DependsOn Query Processor.
 *
 * Parses and compiles DependsOn queries
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
         * Evaluate the parsed query
         * @param {string} query - parsed query
         * @param {object} context - context to execute
         * */
        static evaluateQuery(query, context) {
            const refs = [].concat(ns.ElementReferenceRegistry.refs).concat(ns.GroupReferenceRegistry.refs);
            try {
                const args = refs.map((ref) => ref.id).join(',');
                const exec = new Function(args, 'return ' + query + ';'); // NOSONAR: not a javascript:S3523 case, real evaluation should be done
                return exec.apply(context || null, refs);
            } catch (e) {
                console.error('[DependsOn]: error while evaluating "' + query + '" using ', refs, e);
            }
        }

        /**
         * Parse the query to an evaluable one, replace reference definitions with reference instances aliases
         * @param {string} query
         * @param {JQuery} $root
         * @param {function} [changeHandlerCB]
         * */
        static parseQuery(query, $root, changeHandlerCB) {
            return query.replace(REFERENCE_REGEXP, (q, isGroup, name, selWrapper, sel) => {
                const $context = ns.findScope($root, sel);

                if (name === 'this' && (isGroup || sel)) {
                    console.warn(`[DependsOn]: ${q} is always referencing the current element, could be replaced with simple @this`);
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
