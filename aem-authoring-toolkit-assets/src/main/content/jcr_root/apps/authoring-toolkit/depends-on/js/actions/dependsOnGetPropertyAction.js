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

    function resolveResourcePath(path, $el) {
        if (path[0] !== '.') return path;
        const parentLevel = countParentLevel(path);
        const currentPath = DependsOn.getDialogPath($el);
        const targetPath = DependsOn.getNthParent(currentPath, parentLevel);
        const extension = targetPath.substr(-5) === '.json' ? '' : '.infinity.json'
        return targetPath + extension;
    }

    function evalMapFunction(fn, defaultFn) {
        try {
            return (new Function(fn))() || defaultFn;
        } catch (e) {
            console.error(`[DependsOn]: can not process map function '${fn}'`);
        }
        return defaultFn;
    }

    /**
     * Action definition
     * @param {string} query
     * @param {GetPropertyCfg} config
     *
     * @typedef GetPropertyCfg
     * @property {string} map
     * */
    function getParentProperty(query, config) {
        if (typeof query !== 'string') {
            console.warn('[DependsOn]: can not execute \'get-property\', query should be a string');
            return;
        }

        const $el = this.$el;
        const nameStart = query.lastIndexOf('/') + 1;
        const name = query.substr(nameStart);
        const path = query.substr(0, nameStart);
        const resourcePath = resolveResourcePath(path, $el);

        DependsOn.RequestCache.instance.get(resourcePath)
            .then(
                (data) => DependsOn.get(data, name),
                (e) => {
                    console.warn('Can not get data from node ' + resourcePath, e);
                    return '';
                }
            )
            .then(config.map ? evalMapFunction(config.map, DEFAULT_MAP_FN) : DEFAULT_MAP_FN)
            .then((res) => DependsOn.ElementAccessors.setValue($el, res));
    }

    DependsOn.ActionRegistry.register('get-property', getParentProperty);
})(Granite, Granite.$, Granite.DependsOnPlugin);
