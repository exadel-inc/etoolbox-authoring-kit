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

    const PARENT_DIR_REGEX = /\.\.\//g;
    const DEFAULT_MAP_FN = (res) => res;

    function countParentLevel(path) {
        const dirMatches = path.match(PARENT_DIR_REGEX);
        return dirMatches && dirMatches.length || 0;
    }

    function parsePathString(path) {
        path = path.trim();
        const nameStart = path.lastIndexOf('/');
        return {
            name: path.substr(nameStart + 1),
            path: path.substr(0, nameStart)
        };
    }

    function resolveResourcePath(path, $el, postfix) {
        if (!path.startsWith('.')) return path;
        const parentLevel = countParentLevel(path);
        const currentPath = DependsOn.getDialogPath($el);
        const targetPath = DependsOn.getNthParent(currentPath, parentLevel);
        const extension = targetPath.endsWith(postfix) ? '' : postfix;
        return targetPath + extension;
    }

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
        const resourcePath = resolveResourcePath(path, $el, config.postfix);

        DependsOn.RequestCache.instance.get(resourcePath)
            .then(
                (data) => name ? data[name] : data,
                (e) => {
                    console.warn('Can not get data from node ' + resourcePath, e);
                    return '';
                }
            )
            .then(DependsOn.evalFn(config.map, DEFAULT_MAP_FN))
            .then((res) => DependsOn.ElementAccessors.setValue($el, res));
    }

    DependsOn.ActionRegistry.register('get-property', getParentProperty);
})(Granite, Granite.$, Granite.DependsOnPlugin);
