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
