/**
 * @author Alexey Stsefanovich (ala'n)
 * @version 2.2.4
 *
 * Cached resource request utility.
 * */
(function (Granite, $, DependsOn) {
    'use strict';

    let instance;
    class RequestCache {
        constructor(timeout = 2000) {
            this.timeout = timeout;

            this._clearTimeout = null;
            this._cacheMap = new Map();
        }

        /**
         * Requesting resource by url
         * @param {string} url
         */
        get(url) {
            url = Granite.HTTP.externalize(url);

            if (!this._cacheMap.has(url)) {
                this._cacheMap.set(url, $.get(url));
            }

            this._clearTimeout && clearTimeout(this._clearTimeout);
            this._clearTimeout = setTimeout(() => this.clear(), this.timeout);

            return this._cacheMap.get(url);
        }

        /**
         * Clear requests cache
         */
        clear() {
            this._cacheMap.clear();
            console.debug('[DependsOn] Custom action "get-property" cache cleared.');
        }

        static get instance() {
            return instance || (instance = new RequestCache());
        }
    }

    DependsOn.RequestCache = RequestCache;
})(Granite, Granite.$, Granite.DependsOnPlugin);
