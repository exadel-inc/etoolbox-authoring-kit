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

    const GROUP = 'assistant';
    const FEATURE = GROUP;
    const ICON = FEATURE;

    // noinspection JSUnusedGlobalSymbols
    const AssistantRtePlugin = new Class({
        toString: 'AssistantRtePlugin',

        extend: RTE.plugins.Plugin,

        sourceField: null,

        buttonUi: null,

        currentSelection: null,

        getFeatures: function () {
            return [FEATURE];
        },

        initializeUI: function (tbGenerator, context) {
            if (!this.isFeatureEnabled(FEATURE)) {
                return;
            }

            this.sourceField = context.$editable.parent().find('.coral-RichText-editable').attr('name') || '';

            this.buttonUi = new RTE.ui.cui.PopupButton(FEATURE, this, false, { title: 'Assistant' });
            tbGenerator.addElement(GROUP, 999, this.buttonUi, 10);
            tbGenerator.registerIcon('#' + FEATURE, ICON);

            const popoverContent = {
                ref: FEATURE,
                items: 'assistant:getOptions:assistant-dropdown'
            };
            context.uiSettings.cui.inline.popovers.assistant = popoverContent;
            context.uiSettings.cui.fullscreen.popovers.assistant = popoverContent;
            context.uiSettings.cui.dialogFullScreen.popovers.assistant = popoverContent;

            const rteTemplates = window.Coral.templates.RichTextEditor;
            rteTemplates.assistant_dropdown = rteTemplates.assistant_dropdown || this.populateDropdown;
        },

        getOptions: async function () {
            const allFacilities = await ns.Assistant.getFacilities();
            let facilityFilter = $(`[name="${this.sourceField}"]`).attr('data-eak-assistant-filter');
            if (typeof facilityFilter === 'string' || facilityFilter instanceof String) {
                facilityFilter = new RegExp(facilityFilter);
            } else if (!facilityFilter) {
                facilityFilter = /^text\./i;
            }
            return (allFacilities || []).filter(f => facilityFilter.test(f.id));
        },

        updateState: function (selfDef) {
            const isEmpty = !selfDef.nodeList.commonAncestor.textContent.length;
            this.buttonUi && this.buttonUi.setDisabled(isEmpty);
        },

        execute: async function (id, value, params) {
            this.currentSelection = this.hasSelection() ? this.getSelectionRange() : null;
            const dialogSetup = {
                callerDialog: this.buttonUi.$ui.closest('coral-dialog'),
                sourceField: this.sourceField,
                settings: {
                  text:   this.getSelectedText()
                },
                variants: params.variants,
                selectedVariantId: value,
                acceptDelegate: (text) => {
                    if (!this.currentSelection) {
                        this.editorKernel.relayCmd('clear');
                    } else {
                        const bookmark = RTE.Selection.bookmarkFromProcessingSelection(
                            this.editorKernel.editContext,
                            this.currentSelection);
                        RTE.Selection.selectBookmark(this.editorKernel.editContext, bookmark);
                    }
                    this.editorKernel.relayCmd('inserthtml', RTE.Utils.htmlEncode(text));
                }
            };
            ns.Assistant.openRequestDialog(dialogSetup);
        },

        populateDropdown: function (optionsOrPromise) {
            const fragment = document.createDocumentFragment();
            const facilityList = new Coral.ButtonList();
            facilityList.classList.add('rte-toolbar-list', ns.Assistant.CLS_FACILITY_LIST);
            Promise.resolve(optionsOrPromise).then((facilities) => {
                for (const facility of facilities) {
                    if (!facility.id.startsWith('text.')) {
                        continue;
                    }

                    const flatVariants = facility.variants || [facility];
                    const actionParams = {};
                    actionParams.variants = flatVariants.map((variant) => ns.Assistant.getAssistantFacilityVariantData(variant));
                    const actionParamsString = JSON.stringify(actionParams);

                    const newButton = ns.Assistant.initAssistantFacilityUi(facility, {
                        variantIdAttribute: ns.Assistant.ATTR_ACTION,
                        variantIdTransform: (id) => FEATURE + '#' + id
                    });
                    newButton.setAttribute(ns.Assistant.ATTR_ACTION, `${FEATURE}#${flatVariants[0].id}`);
                    newButton.setAttribute(ns.Assistant.ATTR_ACTION_PARAMS, actionParamsString);
                    facilityList.items.add(newButton);
                }
            });
            fragment.appendChild(facilityList);
            return fragment;
        },

        hasSelection: function () {
            const range = this.getSelectionRange();
            return range && range.endOffset > range.startOffset;
        },

        getSelectionRange: function () {
            return RTE.Selection.createProcessingSelection(this.editorKernel.editContext);
        },

        getSelectedText: function () {
            if (this.hasSelection()) {
                return window.getSelection().toString();
            }
            return this.editorKernel.editContext.root.innerText;
        }
    });
    RTE.plugins.PluginRegistry.register(GROUP, AssistantRtePlugin);
})(window.CUI.rte, window.Class, window.eak = window.eak || {});
