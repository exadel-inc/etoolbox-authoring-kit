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

Granite.EToolboxTrojanHorse = Granite.EToolboxTrojanHorse || (function () {

    return {
        /** Join JCR path parts */
        joinJCRPath: function (path1, path2, ...rest) {
            if (rest.length > 0) return HPEUtils.joinJCRPath(HPEUtils.joinJCRPath(path1, path2), ...rest);
            return (path1 + '/' + path2).replace(/\/(\/)+/g, '/');
        },
        /** Join and normalize JCR path. Removes ending '/' */
        resolveJCRPath: function (path, ...rest) {
            if (rest.length > 0) return HPEUtils.resolveJCRPath(HPEUtils.joinJCRPath(path, ...rest));
            return path.replace(/\/$/, '').replace(/\/_jcr_content\//g, '/jcr:content/');
        },
        /** Resolve path to the JCR content */
        resolveJCRContentPath: function (path) {
            return path ? HPEUtils.joinJCRPath(path, '/_jcr_content/') : '';
        },

        /** Check if the passed resource type can be placed inside of passed path */
        canInsert: function (resourceType, parentPath) {
            if (!resourceType) return 'No component to insert';
            if (!parentPath) return 'Insertion target is incorrect';

            const component = Granite.author.components.find({resourceType})[0];
            if (!component) return 'Component resourceType is not is the list';

            const componentPath = component.getPath();
            const componentRelativePath = componentPath.replace(/^\/[a-z]+\//, '');
            const componentGroup = 'group:' + component.getGroup();

            const resolvedPath = HPEUtils.resolveJCRPath(parentPath);
            const allowedComponents = Granite.author.components.allowedComponentsFor[resolvedPath];

            if (!allowedComponents || !allowedComponents.length) {
                return `No allowed components found for '${resolvedPath}'`;
            }

            if (allowedComponents.indexOf(componentPath) === -1 &&
                allowedComponents.indexOf(componentRelativePath) === -1 &&
                allowedComponents.indexOf(componentGroup) === -1) {
                return `Component '${component.getTitle()}' is not allowed for current target`;
            }
            return true;
        }
    };
})();
