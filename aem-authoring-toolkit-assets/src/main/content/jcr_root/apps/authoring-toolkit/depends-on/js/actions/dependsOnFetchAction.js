/**
 * @author Alexey Stsefanovich (ala'n), Liubou Masiuk (liubou-masiuk), Yana Bernatskaya (YanaBr)
 * @version 2.4.1
 *
 * Action to set the result of fetching an arbitrary resource.
 * Uses query as a target path to node or property.
 * Path should end with the property name or '/' to retrieve the whole node.
 * Path can be relative (e.g. 'node/property' or '../../property') or absolute ('whole/path/to/the/node/property').
 *
 * {string} query - property path
 *
 * {string} config.map - function to process fetched value before setting the result
 * {string} config.err - function to process error before setting the result
 * {string} config.postfix - string to append to the path if it is not presented already
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
        console.warn(`[Depends On]: "fetch" can not get ${name ? `property ${name}` : 'resource'} from "${path}": `, e);
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
            query && console.warn('[DependsOn]: can not execute "fetch", query should be a string');
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
     * Split path coming from query into separate path and property name parts
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
     * Resolve path
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
     * Resolve ../ and remove ./ path parts.
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
