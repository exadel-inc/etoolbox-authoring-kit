'use strict';
(function (RTE, Class) {
    const GROUP = 'writesonic';
    const FEATURE = GROUP;
    const ICON = FEATURE;

    const MENU_OPTIONS = [
        { id: 'sentence-expand', title: 'Expand', icon: 'textEdit', params: { payloadName: 'content_to_expand' } },
        { id: 'content-shorten', title: 'Shorten', icon: 'textEdit', params: { payloadName: 'content_to_shorten' } },
        { id: 'content-rephrase', title: 'Rephrase', icon: 'textEdit', params: { payloadName: 'content_to_rephrase' } },
        { id: 'settings', icon: 'gears' }
    ];

    const KEY_ENDPONT = '/conf/etoolbox-authoring-kit/components/authoring/rte/writesonic-bridge.json';
    const PROPERTY_KEY = 'writesonicKey';

    const WritesonicBridge = new Class({
        toString: 'WritesonicBridge',

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
            this.retrieveApiKey();
        },

        getOptions: function () {
            return MENU_OPTIONS;
        },

        updateState: function (selfDef) {
            const isEmpty = !selfDef.nodeList.commonAncestor.textContent.length;
            this.buttonUi.setDisabled(isEmpty);
        },

        execute: function (id, value, params) {
            if (value === 'settings') {
                return this.editorKernel.execCmd('wbsettings');
            } else {
                return this.editorKernel.execCmd('wbrequest', {
                    command: value,
                    params: params,
                    key: sessionStorage.getItem(PROPERTY_KEY)
                });
            }
        },

        retrieveApiKey: function () {
            if (!sessionStorage.getItem(PROPERTY_KEY)) {
                fetch(KEY_ENDPONT).then(res => res.json()).then(json => sessionStorage.setItem(PROPERTY_KEY, json.key));
            }
        },

        populateDropdown: function (options) {
            const fragment = document.createDocumentFragment();
            const $buttonList = $('<coral-buttonlist></coral-buttonlist>').addClass('rte-toolbar-list');
            for (const option of options) {
                const title = option.title || option.id.substring(0, 1).toUpperCase() + option.id.substring(1).toLowerCase();
                const $button = $('<button></button>')
                    .addClass('coral-buttonlist-item')
                    .attr('is', 'coral-buttonlist-item')
                    .attr('type', 'button')
                    .attr('data-action', FEATURE + '#' + option.id);
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
        }
    });
    RTE.plugins.PluginRegistry.register(GROUP, WritesonicBridge);
})(window.CUI.rte, window.Class);
