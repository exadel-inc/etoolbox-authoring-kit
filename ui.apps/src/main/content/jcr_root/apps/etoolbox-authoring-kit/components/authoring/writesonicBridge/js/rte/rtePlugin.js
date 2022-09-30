(function (RTE, Class, ns) {
    'use strict';

    const GROUP = 'writesonic';
    const FEATURE = GROUP;
    const ICON = FEATURE;

    const WritesonicRtePlugin = new Class({
        toString: 'WritesonicRtePlugin',

        extend: RTE.plugins.Plugin,

        buttonUi: null,

        getFeatures: function () {
            return [FEATURE];
        },

        initializeUI: function (tbGenerator, options) {
            if (!this.isFeatureEnabled(FEATURE)) {
                return;
            }

            this.buttonUi = new RTE.ui.cui.PopupButton(FEATURE, this, false, { title: 'Writesonic Bridge' });
            tbGenerator.addElement(GROUP, 999, this.buttonUi, 10);
            tbGenerator.registerIcon('#' + FEATURE, ICON);

            const popoverContent = {
                ref: FEATURE,
                items: 'writesonic:getOptions:generic-dropdown'
            };
            options.uiSettings.cui.inline.popovers.writesonic = popoverContent;
            options.uiSettings.cui.fullscreen.popovers.writesonic = popoverContent;
            options.uiSettings.cui.dialogFullScreen.popovers.writesonic = popoverContent;

            const rteTemplates = window.Coral.templates.RichTextEditor;
            rteTemplates.generic_dropdown = rteTemplates.generic_dropdown || this.populateDropdown;
        },

        getOptions: function () {
            return ns.Writesonic.menuOptions;
        },

        updateState: function (selfDef) {
            const isEmpty = !selfDef.nodeList.commonAncestor.textContent.length;
            this.buttonUi.setDisabled(isEmpty);
        },

        execute: async function (id, value, params) {
            $('[data-id="writesonic"]').hide();
            if (value === 'settings') {
                return ns.Writesonic.openSettingsDialog();
            }
            const options = await ns.Writesonic.getBasicOptions();
            options.command = value;
            options.params = params;
            options.payload = this.getSelectedText();
            options.acceptDelegate = (text) => {
                if (!this.hasSelection()) {
                    this.editorKernel.relayCmd('clear');
                }
                this.editorKernel.relayCmd('inserthtml', text);
            }
            ns.Writesonic.openRequestDialog(options);
        },

        populateDropdown: function (options) {
            const fragment = document.createDocumentFragment();
            const $buttonList = $('<coral-buttonlist></coral-buttonlist>').addClass('rte-toolbar-list');
            for (const option of options) {
                const title = option.title || option.id.substring(0, 1).toUpperCase() + option.id.substring(1).toLowerCase();
                const $button = $(`<button is="coral-buttonlist-item" type="button" class="coral-buttonlist-item" data-action="${FEATURE}#${option.id}"></button>`);
                if (option.icon) {
                    $button.attr('icon', option.icon);
                }
                if (option.params) {
                    $button.attr('data-action-params', JSON.stringify(option.params));
                }
                $button.text(title);
                $button.appendTo($buttonList);
            }
            fragment.appendChild($buttonList[0]);
            return fragment;
        },

        hasSelection: function () {
            const range = RTE.Selection.createProcessingSelection(this.editorKernel.editContext);
            return range.endOffset > range.startOffset;
        },

        getSelectedText: function () {
            if (this.hasSelection()) {
                return window.getSelection().toString();
            }
            return this.editorKernel.editContext.root.innerText;
        }
    });
    RTE.plugins.PluginRegistry.register(GROUP, WritesonicRtePlugin);
})(window.CUI.rte, window.Class, window.eak = window.eak || {});
