(function (ns) {
    'use strict';

    const APPS_REGEXP = /^\/(apps\/)?/;

    Granite.ACL = Granite.ACL || {};

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
