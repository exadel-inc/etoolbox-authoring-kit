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

    /**
     * Join JCR path parts
     * */
    ns.joinJCRPath = function (path1, path2, ...rest) {
        if (rest.length > 0) return ns.joinJCRPath(ns.joinJCRPath(path1, path2), ...rest);
        return (path1 + '/' + path2).replace(/\/(\/)+/g, '/');
    };

    /**
     * Join and normalize JCR path. Removes ending '/'
     * */
    ns.resolveJCRPath = function (path, ...rest) {
        if (rest.length > 0) return ns.resolveJCRPath(ns.joinJCRPath(path, ...rest));
        return path.replace(/\/$/, '').replace(/\/_jcr_content\//g, '/jcr:content/');
    };

    /**
     * Resolve path to the JCR content
     * */
    ns.resolveJCRContentPath = function (path) {
        return path ? ns.joinJCRPath(path, '/_jcr_content/') : '';
    };

    /**
     * Check if the passed resource type can be placed inside of passed path
     * */
    ns.canInsert = function (resourceType, parentPath) {
        if (!resourceType) return Granite.I18n.get('No component to insert');
        if (!parentPath) return Granite.I18n.get('Insertion target is incorrect');

        const component = author.components.find({ resourceType })[0];
        if (!component) return Granite.I18n.get('Component resourceType is not is the list');

        const componentPath = component.getPath();
        const componentRelativePath = componentPath.replace(/^\/[a-z]+\//, '');
        const componentGroup = 'group:' + component.getGroup();

        const resolvedPath = ns.resolveJCRPath(parentPath);
        const allowedComponents = author.components.allowedComponentsFor[resolvedPath];

        if (!allowedComponents || !allowedComponents.length) {
            return Granite.I18n.get('No allowed components found for {0}').replace('{0}', resolvedPath);
        }

        if (allowedComponents.indexOf(componentPath) === -1 &&
            allowedComponents.indexOf(componentRelativePath) === -1 &&
            allowedComponents.indexOf(componentGroup) === -1) {
            return Granite.I18n.get('Component {0} is not allowed for current target').replace('{0}', component.getTitle());
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
    ns.setupInsertionFormTarget = function ($el, path) {
        path = ns.joinJCRPath(path, '/');
        $el.closest('form[action]').attr('action', path);
        return path;
    };

    /**
     * Fetch action successful response handler to resolve first content component
     * */
    ns.mapImportResourcePath = function (response, parentName, parentPath) {
        try {
            const rootName = findRootName(response);
            if (!rootName) return '';
            const root = response[rootName];
            const nodeNames = Object.keys(root);
            for (const name of nodeNames) {
                const node = root[name];
                if (!isValidComponentNode(node)) continue;
                const path = ns.resolveJCRPath(parentPath, '/', rootName, '/', name);
                const type = node['sling:resourceType'];
                return JSON.stringify({ type, path, name });
            }
        } catch {
            // no action
        }
        return '';
    };
    /** Fetch action unsuccessful response handler stub */
    ns.mapImportResourcePathError = () => '';
}(Granite.author, Granite.EToolboxContentBlueprints = (Granite.EToolboxContentBlueprints || {})));
