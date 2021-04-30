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
 * @author Alexey Stsefanovich (ala'n), Liubou Masiuk, Yana Bernatskaya (YanaBr)
 *
 * DependsOn Fetch Action.
 *
 * An action to set the result of fetching an arbitrary resource.
 * Uses a query as the target path to a node or property.
 * The path should end with the property name or '/' to retrieve the whole node.
 * The path can be relative (e.g. 'node/property' or '../../property') or absolute
 * ('whole/path/to/the/node/property')
 *
 * {string} query - the property path
 * {string} config.map - the function to process the fetched value before setting the result
 * {string} config.err - the function to process an error before setting the result
 * {string} config.postfix - string to append to the path if it is not already present
 * */

(function (Granite, $, DependsOn) {
    'use strict';

    const END_PATH_TERM_REGEX = /\/?$/;
    const RELATIVE_TERMS_REGEX = /\.?\.\//g;
    const DUPLICATE_DELIMITER_REGEX = /\/\//g;
    const PARENT_PATH_GROUP_REGEX = /[^/]+\/\.\.\//;
    const REDUNDANT_PATH_TERM_REGEX = /(^|\/)\.\//g;

    const DEFAULT_RESOLVE = (res, name) => {
        const val = res && name ? res[name] : res;
        if (typeof val === 'undefined' || val === null) return '';
        return val;
    };
    const DEFAULT_FALLBACK = (e, name, path) => {
        console.warn(`[Depends On]: "fetch" cannot get ${name ? `property ${name}` : 'resource'} from "${path}": `, e);
        return '';
    };

    /**
     * Action definition
     * @param {string} query
     * @param {FetchCfg} config
     *
     * @typedef FetchCfg
     * @property {string} map
     * @property {string} err
     * @property {string} postfix
     */
    function fetchAction(query, config) {
        // If not a string -> incorrect input
        if (typeof query !== 'string') {
            query && console.warn('[DependsOn]: cannot execute "fetch", query should be a string');
            return;
        }
        const $el = this.$el;
        // If empty string then just set empty string
        if (query === '') {
            DependsOn.ElementAccessors.setValue($el, '');
            return;
        }

        // Apply default config
        config = Object.assign({
            postfix: '.json'
        }, config);

        // Processing and resolving path and property
        const { name, path } = parsePathString(query);
        const basePath = DependsOn.getDialogPath($el);
        const resourcePath = resolvePath(path, basePath, config.postfix);

        // Request data from cache or AEM
        DependsOn.RequestCache.instance.get(resourcePath)
            .then(
                (res) => DependsOn.evalFn(config.map, DEFAULT_RESOLVE)(res, name, path),
                (err) => DependsOn.evalFn(config.err, DEFAULT_FALLBACK)(err, name, path)
            )
            .then((res) => (res !== undefined) && DependsOn.ElementAccessors.setValue($el, res))
            .catch((e) => console.warn('[DependsOn]: "fetch" failed while executing post-request mappers: ', e));
    }

    DependsOn.ActionRegistry.register('fetch', fetchAction);

    /**
     * Split the path coming from a query into separate "path" and "property name" parts
     * @param {string} path
     * @return {FetchPathParams}
     *
     * @typedef FetchPathParams
     * @property {string} path
     * @property {string} name
     */
    function parsePathString(path) {
        path = path.trim();
        const nameStart = path.lastIndexOf('/') + 1;
        return {
            name: path.substr(nameStart),
            path: path.substr(0, nameStart)
        };
    }

    /**
     * Resolve the path
     * @param {string} path
     * @param {string} [basePath]
     * @param {string} [postfix]
     * @return {string}
     */
    function resolvePath(path, basePath = '', postfix = '') {
        const absPath = path.startsWith('.') ? `${basePath}/${path}` : path;
        const resPath = reducePath(absPath);
        if (RELATIVE_TERMS_REGEX.test(resPath)) {
            throw new Error(`Could not resolve path "${path}". Result path "${resPath}" is incorrect.`);
        }
        const _postfix = resPath.endsWith(postfix) ? '' : postfix;
        return resPath.replace(END_PATH_TERM_REGEX, _postfix);
    }

    /**
     * Resolve "../" and remove "./" parts from the path
     * @param {string} path
     * @return {string}
     */
    function reducePath(path) {
        path = path.replace(DUPLICATE_DELIMITER_REGEX, '/');
        path = path.replace(REDUNDANT_PATH_TERM_REGEX, '/');
        let original = path;
        do {
            path = (original = path).replace(PARENT_PATH_GROUP_REGEX, '');
        } while (original.length !== path.length);
        return path;
    }
})(Granite, Granite.$, Granite.DependsOnPlugin);
