/**
 *  Import Toolbar Action to import "template" page content into current position
 **/
(function ($document, author, ns) {
    const IMPORT_ACTION = 'content-blueprint-import-action';

    const JCR_CONTENT = '/jcr:content/';
    const NEWPAR_TYPES = ['wcm/foundation/components/parsys/newpar', 'wcm/foundation/components/responsivegrid/new'];
    const IMPORT_DIALOG_PATH = '/mnt/override/apps/etoolbox-authoring-kit/content-blueprints/components/content/toolbar/dialog/_cq_dialog.html';

    function condition(editable) {
        return author.pageInfoHelper.canModify() && editable.hasAction('INSERT');
    }

    function openImportDialog(editable) {
        const target = editable.getParent();
        const path = target.path;
        const order = NEWPAR_TYPES.includes(editable.type) ? 'last' : `after ${editable.name}`;
        const parentType = editable.getParent().type;

        const page = path.substr(0, path.indexOf(JCR_CONTENT) + JCR_CONTENT.length);
        const dialogPath = ns.EToolboxContentBlueprints.joinJCRPath(IMPORT_DIALOG_PATH, page);
        const dialogConfig = {
            src: Granite.HTTP.externalize(dialogPath.replace(/\*\//, '')),
            loadingMode: 'auto',
            layout: 'auto'
        };
        const dlg = new author.ui.Dialog({
            getConfig: () => dialogConfig,
            getRequestData: () => ({ order, parentType, path }),
            onSuccess: () => setTimeout(() => {
                author.editableHelper.actions.REFRESH.execute(target);
            }, 250)
        });

        author.DialogFrame.openDialog(dlg);
    }

    $document.off('cq-layer-activated.content-blueprint-import-action').on('cq-layer-activated.content-blueprint-import-action', function (ev) {
        if (ev.layer === 'Edit') {
            author.EditorFrame.editableToolbar.registerAction(IMPORT_ACTION, {
                icon: 'coral-Icon--dataUpload',
                text: Granite.I18n.get('Import from EToolbox Content Blueprint'),
                handler: openImportDialog,
                condition: condition,
                isNonMulti: false
            });
            // Restore custom action after MSM
            setTimeout(() => {
                const action = author.EditorFrame.editableToolbar.config.actions[IMPORT_ACTION];
                if (action && action.decoratedCondition) {
                    action.condition = action.decoratedCondition;
                    delete action.decoratedCondition;
                    console.log('Action "', action.name, '" undecorated');
                }
            });
        }
    });
})($(document), Granite.author, Granite);
