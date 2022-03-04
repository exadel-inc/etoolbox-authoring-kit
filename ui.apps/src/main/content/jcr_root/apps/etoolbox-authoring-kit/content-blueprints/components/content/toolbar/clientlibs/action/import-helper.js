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
(function (author, ns) {
    'use strict';

    ns.EToolboxContentBlueprints = ns.EToolboxContentBlueprints || {}

    /**
     * Join JCR path parts
     * */
    ns.EToolboxContentBlueprints.joinJCRPath = function (path1, path2, ...rest) {
        if (rest.length > 0) return ns.EToolboxContentBlueprints.joinJCRPath(ns.EToolboxContentBlueprints.joinJCRPath(path1, path2), ...rest);
        return (path1 + '/' + path2).replace(/\/(\/)+/g, '/');
    };

    /**
     * Join and normalize JCR path. Removes ending '/'
     * */
    ns.EToolboxContentBlueprints.resolveJCRPath = function (path, ...rest) {
        if (rest.length > 0) return ns.EToolboxContentBlueprints.resolveJCRPath(ns.EToolboxContentBlueprints.joinJCRPath(path, ...rest));
        return path.replace(/\/$/, '').replace(/\/_jcr_content\//g, '/jcr:content/');
    }

    /**
     * Resolve path to the JCR content
     * */
    ns.EToolboxContentBlueprints.resolveJCRContentPath = function (path) {
        return path ? ns.EToolboxContentBlueprints.joinJCRPath(path, '/_jcr_content/') : '';
    };

    /**
     * Check if the passed resource type can be placed inside of passed path
     * */
    ns.EToolboxContentBlueprints.canInsert = function (resourceType, parentPath) {
        if (!resourceType) return 'No component to insert';
        if (!parentPath) return 'Insertion target is incorrect';

        const component = Granite.author.components.find({resourceType})[0];
        if (!component) return 'Component resourceType is not is the list';

        const componentPath = component.getPath();
        const componentRelativePath = componentPath.replace(/^\/[a-z]+\//, '');
        const componentGroup = 'group:' + component.getGroup();

        const resolvedPath = ns.EToolboxContentBlueprints.resolveJCRPath(parentPath);
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
    };

    const isValidComponentNode = (item) => {
        return item && item['sling:resourceType'];
    };

    const findRootName = (content) => {
        if (Object.hasOwnProperty.call(content, 'content-blueprints-parsys')) return 'content-blueprints-parsys';
        return null;
    };

    /**
     * Add ending '/' to the form action.
     * @returns result path
     * */
    ns.EToolboxContentBlueprints.setupInsertionFormTarget = function ($el, path) {
        path = ns.EToolboxContentBlueprints.joinJCRPath(path, '/');
        $el.closest('form[action]').attr('action', path);
        return path;
    };

    /**
     * Fetch action successful response handler to resolve first content component
     * */
    ns.EToolboxContentBlueprints.mapImportResourcePath = function (response, parentName, parentPath) {
        try {
            const rootName = findRootName(response);
            if (!rootName) return '';
            const root = response[rootName];
            const nodeNames = Object.keys(root);
            for (const name of nodeNames) {
                const node = root[name];
                if (!isValidComponentNode(node)) continue;
                const path = ns.EToolboxContentBlueprints.resolveJCRPath(parentPath, '/', rootName, '/', name);
                const type = node['sling:resourceType'];
                return JSON.stringify({type, path, name});
            }
        } catch {
            // no action
        }
        return '';
    };
    /** Fetch action unsuccessful response handler stub */
    ns.EToolboxContentBlueprints.mapImportResourcePathError = () => '';

}(Granite.author, Granite));
