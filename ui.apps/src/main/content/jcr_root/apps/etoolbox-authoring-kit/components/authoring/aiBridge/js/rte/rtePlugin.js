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
(function (RTE, Class, ns) {
    'use strict';

    const GROUP = 'ai';
    const FEATURE = GROUP;
    const ICON = FEATURE;

    const AiRtePlugin = new Class({
        toString: 'AiRtePlugin',

        extend: RTE.plugins.Plugin,

        buttonUi: null,

        getFeatures: function () {
            return [FEATURE];
        },

        initializeUI: function (tbGenerator, context) {
            if (!this.isFeatureEnabled(FEATURE)) {
                return;
            }

            this.aiSettings = this.getAiSettings(context);

            this.buttonUi = new RTE.ui.cui.PopupButton(FEATURE, this, false, { title: 'AI Bridge' });
            tbGenerator.addElement(GROUP, 999, this.buttonUi, 10);
            tbGenerator.registerIcon('#' + FEATURE, ICON);

            const popoverContent = {
                ref: FEATURE,
                items: 'ai:getOptions:ai-dropdown'
            };
            context.uiSettings.cui.inline.popovers.ai = popoverContent;
            context.uiSettings.cui.fullscreen.popovers.ai = popoverContent;
            context.uiSettings.cui.dialogFullScreen.popovers.ai = popoverContent;

            const rteTemplates = window.Coral.templates.RichTextEditor;
            rteTemplates.ai_dropdown = rteTemplates.ai_dropdown || this.populateDropdown;
        },

        getOptions: function () {
            return ns.Ai.getMenuOptions();
        },

        updateState: function (selfDef) {
            const isEmpty = !selfDef.nodeList.commonAncestor.textContent.length;
            this.buttonUi.setDisabled(isEmpty);
        },

        execute: async function (id, value, params) {
            $('[data-id="ai"]').hide();
            if (value === 'settings') {
                return ns.Ai.openSettingsDialog();
            }
            const setup = {
                payload: this.getSelectedText(),
                variants: params.variants,
                params: Object.assign({}, params, { variants: undefined}),
                acceptDelegate: (text) => {
                    if (!this.hasSelection()) {
                        this.editorKernel.relayCmd('clear');
                    }
                    this.editorKernel.relayCmd('inserthtml', text);
                }
            };
            ns.Ai.openRequestDialog(setup);
        },

        getAiSettings: function (context) {
            if (!context.$editable) {
                return {};
            }
            const fieldName = context.$editable.parent().find('.coral-RichText-editable').attr('name') || '';
            const settingsFieldName = fieldName && fieldName + '__aiSettings';
            const settings = context.$editable.closest('form').find(`[name="${settingsFieldName}"]`).val();
            return settings ? JSON.parse(settings) : {};
        },

        populateDropdown: function (optionsOrPromise) {
            const fragment = document.createDocumentFragment();
            const $buttonList = $('<coral-buttonlist></coral-buttonlist>').addClass('rte-toolbar-list');
            Promise.resolve(optionsOrPromise).then((options) => {
                for (const option of options) {
                    const title = option.title || option.id.substring(0, 1).toUpperCase() + option.id.substring(1).toLowerCase();
                    const $button = $(`<button is="coral-buttonlist-item" type="button" data-action="${FEATURE}#${option.id}"></button>`);
                    if (option.icon) {
                        $button.attr('icon', option.icon);
                    }
                    const params = option.params || {};
                    params.variants = option.variants
                        ? option.variants.map(v => { return { title: v.vendor, id: v.id }; })
                        : [{ title: option.vendor, id: option.id }];
                    if (this.aiSettings) {
                        params.settings = this.aiSettings;
                    }
                    $button.attr('data-action-params', JSON.stringify(params));
                    $button.text(title);
                    $button.appendTo($buttonList);
                }
            });
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
    RTE.plugins.PluginRegistry.register(GROUP, AiRtePlugin);
})(window.CUI.rte, window.Class, window.eak = window.eak || {});
