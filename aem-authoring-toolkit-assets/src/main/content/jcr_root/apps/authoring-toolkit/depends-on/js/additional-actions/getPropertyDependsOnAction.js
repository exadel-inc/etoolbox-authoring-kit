/**
 * @author Alexey Stsefanovich (ala'n), Liubou Masiuk (liubou-masiuk), Yana Bernatskaya (YanaBr)
 * @version 2.2.4
 * Custom action to get component property
 * property path can be relative (e.g. 'node/nestedProperty' or '../../parentCompProperty')
 *
 * query - current node (this)
 * */

(function (Granite, $, DependsOn) {
    'use strict';

    const PARENT_DIR_REGEX = /\.\.\//g;

    function getLevel(path) {
        const dirMatches = path.match(PARENT_DIR_REGEX);
        return dirMatches && dirMatches.length || 0;
    }

    const RequestCache = (function (timeout) {
        let clearTm = null;
        let map = new Map();

        function clearCache() {
            map.clear();
            console.debug('[DependsOn] Custom action "get-property" cache cleared.');
        }

        function get(url) {
            url = Granite.HTTP.externalize(url);
            if (!map.has(url)) map.set(url, $.get(url));
            if (clearTm) clearTimeout(clearTm);
            clearTm = setTimeout(clearCache, timeout);
            return map.get(url);
        }

        return {get: get, clear: clearCache};
    })(2000);

    function getParentProperty(currentResource, options) {
        const resourcePath = DependsOn.getDialogPath(currentResource);
        const $el = this.$el;
        const level = getLevel(options.path);
        const name = options.path.replace(PARENT_DIR_REGEX, '');
        RequestCache.get(DependsOn.getNthParent(resourcePath, level) + '.infinity.json').then(
            function (data) {
                return DependsOn.get(data, name, '/');
            },
            function (e) {
                console.warn('Can not get data from node ' + resourcePath, e);
                return '';
            }
        ).then(function (res) {
            DependsOn.ElementAccessors.setValue($el, res);
        });
    }
    getParentProperty.cache = RequestCache;
    DependsOn.ActionRegistry.register('get-property', getParentProperty);
})(Granite, Granite.$, Granite.DependsOnPlugin);
