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

    const APPS_REGEXP = /^\/(apps\/)?/;

    ns.ACL = ns.ACL || {};

    ns.ACL.WRAPPER_RES_TYPE = 'authoring-toolkit/custom-lists/components/content/itemWrapper';

    ns.ACL.launchReferenceDialog = function (editable, itemPath) {
        const dlg = new ns.author.ui.Dialog({
            getConfig: getDialogConfig(editable, itemPath),
            getRequestData: function () {
                return { resourceType: ns.ACL.WRAPPER_RES_TYPE };
            },
            onSuccess: reloadEditable(editable)
        });
        ns.author.DialogFrame.openDialog(dlg);
    };

    function reloadEditable(editable) {
        return function () {
            ns.author.edit.EditableActions.REFRESH.execute(editable).done(function () {
                ns.author.selection.select(editable);
                editable.afterEdit();

                const editableParent = ns.author.editables.getParent(editable);
                editableParent && editableParent.afterChildEdit(editable);
            });
        };
    }

    function getDialogConfig(editable, itemResType) {
        const dialogPath = '/mnt/override/apps/' + itemResType.replace(APPS_REGEXP, '') + '/_cq_dialog.html' + editable.path;
        const dialogUrl = ns.HTTP.externalize(dialogPath);
        return function () {
            return {
                src: dialogUrl,
                loadingMode: 'auto',
                layout: 'auto'
            };
        };
    }
}(Granite));
