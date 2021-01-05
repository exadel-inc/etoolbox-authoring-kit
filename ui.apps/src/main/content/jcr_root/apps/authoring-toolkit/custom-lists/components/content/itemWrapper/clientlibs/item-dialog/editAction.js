(function ($document, author, ns) {
    const CONFIG_SELECTOR = '.item-config';
    const DIALOG_PATH_ATTR = 'data-dialog-path';

    const actionConfig = {
        icon: 'coral-Icon--wrench',
        text: 'Open Dialog',
        order: 'before COPY',
        isNonMulti: true,
        condition: function (editable) {
            return editable.type === ns.ACL.Utils.WRAPPER_RES_TYPE;
        },
        handler: function (editable) {
            const config = editable.dom.find(CONFIG_SELECTOR);
            const itemPath = config.attr(DIALOG_PATH_ATTR);
            itemPath && ns.ACL.Dialog.launchReferenceDialog(editable, itemPath);
        }
    };

    $document.on('cq-layer-activated', function (ev) {
        if (ev.layer === 'Edit') {
            author.EditorFrame.editableToolbar.registerAction('OPEN_ITEM_DIALOG', actionConfig);
        }
    });
})($(document), Granite.author, Granite);
