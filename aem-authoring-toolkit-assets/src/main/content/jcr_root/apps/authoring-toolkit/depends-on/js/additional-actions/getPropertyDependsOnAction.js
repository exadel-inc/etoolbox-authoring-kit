/**
 * @author Alexey Stsefanovich (ala'n), Liubou Masiuk (liubou-masiuk), Yana Bernatskaya (YanaBr)
 * @version 2.2.4
 * Custom action to get component property
 * property path can be relative (e.g. 'node/nestedProperty' or '../../parentCompProperty')
 *
 * query - property path
 * */

(function (Granite, $, DependsOn) {
    'use strict';

    const PARENT_DIR_REGEX = /\.\.\//g;

    function getLevel(path) {
        const dirMatches = path.match(PARENT_DIR_REGEX);
        return dirMatches && dirMatches.length || 0;
    }

    class RequestCache {
        static timeout = 2000;
        static _clearTm = null;
        static _map = new Map();

        static clear() {
            this._map.clear();
            console.debug('[DependsOn] Custom action "get-property" cache cleared.');
        }

        static get(url) {
            url = Granite.HTTP.externalize(url);
            if (!this._map.has(url)) this._map.set(url, $.get(url));
            if (this._clearTm) clearTimeout(this._clearTm);
            this._clearTm = setTimeout(this.clear, this.timeout);
            return this._map.get(url);
        }
    }

    function getParentProperty(path) {
        const $el = this.$el;
        const resourcePath = DependsOn.getDialogPath($el);
        const level = getLevel(path);
        const name = path.replace(PARENT_DIR_REGEX, '');
        RequestCache
            .get(DependsOn.getNthParent(resourcePath, level) + '.infinity.json')
            .then((data) => DependsOn.get(data, name, '/'), (e) => {
                console.warn('Can not get data from node ' + resourcePath, e);
                return '';
            })
            .then((res) => DependsOn.ElementAccessors.setValue($el, res));
    }
    getParentProperty.cache = RequestCache;
    DependsOn.ActionRegistry.register('get-property', getParentProperty);
})(Granite, Granite.$, Granite.DependsOnPlugin);
