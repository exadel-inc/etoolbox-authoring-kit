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
 * @author Liubou Masiuk
 *
 * Accessor for widgets that do not have a wrapper element
 * */
(function ($, ns) {
    const NO_WRAPPER_FIELDS_SELECTOR =
        '.coral-Form-fieldset, input[type=hidden], .coral-Heading, .coral3-Alert, .coral3-Button, a.coral-Link, span';

    ns.ElementAccessors.registerAccessor({
        selector: NO_WRAPPER_FIELDS_SELECTOR,
        findWrapper: function () {
            return $([]);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
