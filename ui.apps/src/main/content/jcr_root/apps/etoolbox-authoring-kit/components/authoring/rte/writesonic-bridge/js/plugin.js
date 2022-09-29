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
    const PROPERTY_KEY = 'eak.writesonic.key';

    const PROPERTY_ENGINE = 'eak.writesonic.engine';
    const DEFAULT_ENGINE = 'economy';
    const ENGINE_OPTIONS = ['business', DEFAULT_ENGINE];

    const PROPERTY_LANGUAGE = 'eak.writesonic.language';
    const DEFAULT_LANGUAGE = 'en';
    const LANGUAGE_OPTIONS = [DEFAULT_LANGUAGE, 'nl', 'fr', 'de', 'it', 'pl', 'es', 'pt-pt', 'pt-br', 'ru', 'ja', 'zh', 'bg', 'cs', 'da', 'el', 'hu', 'lt', 'lv', 'ro', 'sk', 'sl', 'sv', 'fi', 'et'];

    const PROPERTY_TONE = 'eak.writesonic.tone';
    const DEFAULT_TONE = 'professional';
    const TONE_OPTIONS = ['excited', DEFAULT_TONE, 'funny', 'encouraging', 'dramatic', 'witty', 'sarcastic', 'engaging', 'creative'];


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
            $('[data-id="writesonic"]').hide();
            if (value === 'settings') {
                return this.editorKernel.execCmd('wbsettings', {
                    engine: {
                        propId: PROPERTY_ENGINE,
                        options: ENGINE_OPTIONS,
                        defaultValue: DEFAULT_ENGINE
                    },
                    language: {
                        propId: PROPERTY_LANGUAGE,
                        options: LANGUAGE_OPTIONS,
                        defaultValue: DEFAULT_LANGUAGE
                    },
                    tone: {
                        propId: PROPERTY_TONE,
                        options: TONE_OPTIONS,
                        defaultValue: DEFAULT_TONE
                    }
                });
            } else {
                return this.editorKernel.execCmd('wbrequest', {
                    command: value,
                    engine: localStorage.getItem(PROPERTY_ENGINE) || DEFAULT_ENGINE,
                    language: localStorage.getItem(PROPERTY_LANGUAGE) || DEFAULT_LANGUAGE,
                    tone: localStorage.getItem(PROPERTY_TONE) || DEFAULT_TONE,
                    params: params,
                    key: sessionStorage.getItem(PROPERTY_KEY),
                    editorKernel: this.editorKernel
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
