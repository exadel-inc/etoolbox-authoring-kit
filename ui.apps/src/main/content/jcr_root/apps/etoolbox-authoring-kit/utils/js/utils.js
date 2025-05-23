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
(function (ns) {
    'use strict';

    /**
     * Wraps the given function with the provided wrapper. Does not wrap twice.
     * @param {function} fn
     * @param {function} wrapper
     * @returns {function} - wrapped function
     */
    ns.decorate = function decorate(fn, wrapper) {
        if (fn.wrapper === wrapper) return fn;
        const fnDecorated = function decoratedCondition(...args) {
            try {
                return wrapper.apply(this, [fn].concat(args));
            } catch (e) {
                console.error('Error during condition evaluation', e);
                return false;
            }
        };
        fnDecorated.original = fn;
        fnDecorated.wrapper = wrapper;
        return fnDecorated;
    };

    /**
     * Executes listener for the given editable
     * @param {Editable} editable
     * @param {string} name
     * @param {Array} params
     */
    ns.executeListener = function executeListener(editable, name, ...params) {
        if (!editable.config || !editable.config.editConfig) return null;
        if (typeof editable.config.editConfig.listeners[name] === 'function') {
            try {
                return editable.config.editConfig.listeners[name].apply(editable, params);
            } catch (e) {
                console.error('Error while executing listener %s for editable: %o', name, e);
            }
        }
        return null;
    };

    /**
     * Debounce function wrapper
     * @param {function} fn
     * @param {number} timeout
     */
    ns.debounce = function (fn, timeout) {
        let timer;
        return function (...args) {
            clearTimeout(timer);
            timer = setTimeout(() => fn.apply(this, args), timeout);
        };
    };
}((Granite.EAKUtils = Granite.EAKUtils || {})));
