/**
 * @author Alexey Stsefanovich (ala'n), Liubou Masiuk (liubou-masiuk), Yana Bernatskaya (YanaBr)
 * @version 2.4.0
 *
 * Custom action to get component property
 * property path can be relative (e.g. 'node/nestedProperty' or '../../parentCompProperty')
 *
 * {string} query - property path
 *
 * {string} config.map - function to process result before set (can be used for mapping)
 * */

(function (Granite, $, DependsOn) {
    'use strict';

    const END_PATH_TERM_REGEX = /\/?$/;
    const RELATIVE_TERMS_REGEX = /\.?\.\//g;
    const DUPLICATE_DELIMITER_REGEX = /\/\//g;
    const PARENT_PATH_GROUP_REGEX = /[^/]+\/\.\.\//g;
    const REDUNDANT_PATH_TERM_REGEX = /(^|\/)\.\//g;

    /**
     * Action definition
     * @param {string} query
     * @param {GetPropertyCfg} config
     *
     * @typedef GetPropertyCfg
     * @property {string} map
     * @property {string} postfix
     */
    function getParentProperty(query, config) {
        if (typeof query !== 'string') {
            console.warn('[DependsOn]: can not execute \'get-property\', query should be a string');
            return;
        }

        config = Object.assign({
            postfix: '.json'
        }, config);

        const $el = this.$el;
        const {name, path} = parsePathString(query);
        const basePath = DependsOn.getDialogPath($el);
        const resourcePath = resolvePath(path, basePath, config.postfix);

        DependsOn.RequestCache.instance.get(resourcePath)
            .then(
                (data) => name ? data[name] : data,
                (e) => {
                    console.warn('Can not get data from node ' + resourcePath, e);
                    return '';
                }
            )
            .then(DependsOn.evalFn(config.map, (res) => res))
            .then((res) => DependsOn.ElementAccessors.setValue($el, res));
    }

    DependsOn.ActionRegistry.register('get-property', getParentProperty);

    /**
     * Split path coming from query into separate path and property name parts
     * @param {string} path
     * @return {GetPropertyPathParams}
     *
     * @typedef GetPropertyPathParams
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
