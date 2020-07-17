/**
 * @author Alexey Stsefanovich (ala'n), Liubou Masiuk (liubou-masiuk), Yana Bernatskaya (YanaBr)
 * @version 2.3.0
 *
 * Custom action to get component property
 * property path can be relative (e.g. 'node/nestedProperty' or '../../parentCompProperty')
 *
 * {string} query - property path
 * */

(function (Granite, $, DependsOn) {
    'use strict';

    const PARENT_DIR_REGEX = /\.\.\//g;

    function getParentLevel(path) {
        const dirMatches = path.match(PARENT_DIR_REGEX);
        return dirMatches && dirMatches.length || 0;
    }

    function buildResourcePath($el, path) {
        const parentLevel = getParentLevel(path);
        const resourcePath = DependsOn.getDialogPath($el);
        const targetPath = DependsOn.getNthParent(resourcePath, parentLevel);
        return targetPath + '.infinity.json';
    }

    /**
     * Action definition
     * @param {string} path - query
     * */
    function getParentProperty(path) {
        if (typeof path !== 'string') {
            console.warn('[DependsOn]: can not execute \'get-property\', query should be a string');
            return;
        }

        const $el = this.$el;
        const name = path.replace(PARENT_DIR_REGEX, '');
        const requestPath = buildResourcePath($el, path);

        DependsOn.RequestCache.instance
            .get(requestPath)
            .then(
                (data) => DependsOn.get(data, name, '/'),
                (e) => {
                    console.warn('Can not get data from node ' + requestPath, e);
                    return '';
                }
            )
            .then((res) => DependsOn.ElementAccessors.setValue($el, res));
    }

    DependsOn.ActionRegistry.register('get-property', getParentProperty);
})(Granite, Granite.$, Granite.DependsOnPlugin);
