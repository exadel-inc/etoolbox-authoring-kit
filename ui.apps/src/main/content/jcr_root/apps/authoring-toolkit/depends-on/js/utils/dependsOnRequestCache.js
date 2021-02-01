/**
 * @author Alexey Stsefanovich (ala'n)
 *
 * Cached resource request utility.
 * */
(function (Granite, $, DependsOn) {
    'use strict';

    /**
     * @return {Promise}
     */
    function promisify(jqPromise) {
        return new Promise((resolve, reject) => jqPromise.then(resolve, reject));
    }

    let instance;
    class RequestCache {
        static get DEFAULT_TIMEOUT() { return 2000; }

        /**
         * @return {RequestCache} global RequestCache instance
         */
        static get instance() {
            return instance || (instance = new RequestCache());
        }

        constructor(timeout = RequestCache.DEFAULT_TIMEOUT) {
            this.timeout = timeout;

            this._clearTimeout = null;
            this._cacheMap = new Map();
        }

        /**
         * Requesting resource by url
         * @param {string} url
         * @return {Promise}
         */
        get(url) {
            url = Granite.HTTP.externalize(url);

            if (!this._cacheMap.has(url)) {
                this._cacheMap.set(url, promisify($.get(url)));
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
            console.debug('[DependsOn] Requests cache was cleared.');
        }
    }

    DependsOn.RequestCache = RequestCache;
})(Granite, Granite.$, Granite.DependsOnPlugin);
