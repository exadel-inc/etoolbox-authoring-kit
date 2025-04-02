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
 * DependsOn Managed State Helper - utility to make state updates actor aware.
 * If multiple actors are requested to update the state, state returns to false only if all actors are free state.
 * */
(function ($, ns) {
    'use strict';

    class MangedStateHelper {
        static get($el, type) {
            const name = type + '-actors';
            const actors = $el.data(name);
            return actors && actors.size > 0;
        }

        static set($el, type, value, actor) {
            const name = type + '-actors';
            const actors = $el.data(name) || new Set();
            value ? actors.add(actor) : actors.delete(actor);
            $el.data(name, actors);
            return actors.size > 0;
        }

        static clear($el, type) {
            $el.data(type + '-actors', null);
        }

        static wrap(handler, type, inverted) {
            return ($el, value, actor) => {
                if (!actor) {
                    handler($el, value, actor);
                    this.clear($el, type);
                    return value;
                }
                const state = this.set($el, type, inverted ? !value : value, actor);
                handler($el, inverted ? !state : state, actor);
                return inverted ? !state : state;
            };
        }
    }

    ns.MangedStateHelper = MangedStateHelper;
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
