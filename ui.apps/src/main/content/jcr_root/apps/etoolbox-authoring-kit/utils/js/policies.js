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
(function (ns, author) {
    'use strict';

    /**
     * @param {Editable} editable
     * @param {string} name - the name of the property to find
     * @returns {any} value of the given property defined in the policy
     */
    function findPropertyFromPolicy(editable, name) {
        const cell = author.util.resolveProperty(author.pageDesign, editable.config.policyPath);
        return cell && cell[name] ? cell[name] : null;
    }

    /**
     * @param {Editable} editable
     * @param {string} name - the name of the property to find
     * @see /libs/cq/gui/components/authoring/editors/clientlibs/core/js/storage/components.js _findAllowedComponentsFromDesign
     * @returns {any} value of the given property from design object
     */
    function findPropertyFromDesign(editable, name) {
        const cellSearchPaths = editable.config.cellSearchPath || [];
        for (let i = 0; i < cellSearchPaths.length; i++) {
            const cell = author.util.resolveProperty(author.pageDesign, cellSearchPaths[i]);
            if (cell && cell[name]) return cell[name];
        }
        return null;
    }

    /**
     * @param {Editable} editable
     * @param {string} name - the name of the property to find
     * @returns {any} value of the given property of an editable from policy or design configuration
     */
    function findPropertyFromConfig(editable, name) {
        if (editable && editable.config) {
            if (editable.config.policyPath) {
                return findPropertyFromPolicy(editable, name);
            } else {
                return findPropertyFromDesign(editable, name);
            }
        }
        return null;
    }

    // Public API
    ns.findPropertyFromPolicy = findPropertyFromPolicy;
    ns.findPropertyFromDesign = findPropertyFromDesign;
    ns.findPropertyFromConfig = findPropertyFromConfig;
}((Granite.EAK = Granite.EAK || {}), Granite.author));
