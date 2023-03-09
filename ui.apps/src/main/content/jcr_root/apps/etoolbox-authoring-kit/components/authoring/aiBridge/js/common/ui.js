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

    const ATTR_VARIANTS = 'data-eak-assistant-variants';
    const ATTR_VARIANT = 'data-eak-assistant-variant';

    const DATA_KEY_EVENTS_INITIALIZED = 'eak-assistant-events';

    const CLS_FIELD_WRAPPER = 'eak-Form-field-wrapper';
    const CLS_ACTION_BUTTON = 'eak-assistant-button';

    const FIELD_SETTING_DELIMITER = '@';

    const DEBOUNCE_DELAY = 300;

    ns.Assistant = ns.Assistant || {};
    Object.assign(ns.Assistant, {

        ATTR_ACTION: 'data-eak-assistant-action',
        ATTR_ACTION_PARAMS: 'data-eak-assistant-action-params',
        ATTR_SOURCE: 'data-eak-assistant-source',
        ATTR_SOURCE_REF: 'data-eak-assistant-source-ref',
        ATTR_SOURCE_SETTING_REF: 'data-eak-assistant-source-setting',
        ATTR_TARGET: 'data-eak-assistant-target',

        DATA_KEY_SETUP: 'eak-assistant-setup',
        DATA_KEY_ACTION_PARAMS: 'eak-assistant-action-params',

        CLS_FACILITY_LIST: 'eak-assistant-facilities',

        initAssistantUi: async function (field, options = {}) {
            const facilities = await this.getFacilities();

            const $field = $(field);
            const fieldValue = $field.is(`[${this.ATTR_SOURCE}]`)
                ? $field.val()
                : $field.find(`[${this.ATTR_SOURCE}]`).val();

            const button = new Coral.AnchorButton();
            button.set({
                icon: 'assistant',
                iconSize: 'S',
                disabled: !fieldValue
            });

            const facilityList = new Coral.ButtonList();
            for (const facility of facilities) {
                if (!facility.id.startsWith(options.facilityPrefix || 'text.')) {
                    continue;
                }
                const flatVariants = facility.variants ? facility.variants : [facility];
                const variantsJson = JSON.stringify(flatVariants.map((variant) => {
                    return {
                        title: variant.vendorName,
                        id: variant.id,
                        hasSettings: variant.settings && variant.settings.length > 0
                    };
                }));
                const newButton = this.initAssistantFacilityUi(facility, { variantIdAttribute: 'data-eak-assistant-variant' });
                newButton.setAttribute(ATTR_VARIANTS, variantsJson);
                facilityList.items.add(newButton);
                facilityList.classList.add(this.CLS_FACILITY_LIST);
            }
            const popover = new Coral.Popover();
            popover.set({
                alignMy: 'right top',
                alignAt: 'right bottom',
                target: button,
            });
            popover.content.appendChild(facilityList);

            attachEventHandlers($field);

            const $wrapper = $field.wrap(`<div class="${CLS_FIELD_WRAPPER}"></div>`).parent();
            $(button).addClass(CLS_ACTION_BUTTON).appendTo($wrapper);
            $(popover).appendTo($wrapper);
        },

        initAssistantFacilityUi: function (facility, options = {}) {
            const flatVariants = facility.variants ? facility.variants : [facility];
            const variantsHtml = flatVariants
                .map((variant) => {
                    let iconAttrString = '';
                    let iconContentString = '';
                    if (variant.vendorLogo && variant.vendorLogo.startsWith('data:image/')) {
                        iconAttrString = 'icon="eak-inline"';
                        iconContentString = `<coral-icon style="background-image:url('${variant.vendorLogo}')"></coral-icon>`;
                    } else if (variant.vendorLogo) {
                        iconAttrString = `icon="${variant.vendorLogo}"`;
                    }
                    const vendorString = variant.vendorName.substring(0, 1);
                    const dataString = `${options.variantIdAttribute}="${options.variantIdTransform ? options.variantIdTransform(variant.id) : variant.id}"`;
                    return `<a is="coral-anchorbutton" variant="minimal" ${iconAttrString} iconsize="S" ${dataString}>${iconContentString}${vendorString}</a>`;
                })
                .join('');
            const contentHtml = `<span class="title">${facility.title}</span><div class="eak-assistant-smallbuttons">${variantsHtml}</div>`;
            return new Coral.ButtonList.Item().set({
                icon: facility.icon,
                value: facility.id,
                content: {
                    innerHTML: contentHtml
                }
            });
        },

        getDialogSize: function (member) {
            const $member = $(member);
            const $dialog = $member.closest('coral-dialog');
            if (!$dialog.length || $dialog.is('.coral3-Dialog--fullscreen')) {
                return null;
            }
            const $dialogWrapper = $dialog.find('.coral3-Dialog-wrapper');
            return {width: $dialogWrapper.outerWidth(), height: $dialogWrapper.outerHeight()};
        }
    });

    function attachEventHandlers($field) {
        const $dialog = $field.closest('coral-dialog');
        if ($dialog.data(DATA_KEY_EVENTS_INITIALIZED)) {
            return;
        }
        $dialog.on('click', `[${ATTR_VARIANTS}],[${ATTR_VARIANT}]`, handleFacilityClick);
        $dialog.on('keydown', `[${ns.Assistant.ATTR_SOURCE}]`, $.debounce(DEBOUNCE_DELAY, handleTextInputChange));
        $dialog.on('change', `[${ns.Assistant.ATTR_SOURCE_SETTING_REF}]`, handleSettingChange);
        $dialog.data(DATA_KEY_EVENTS_INITIALIZED, true);
    }

    function handleFacilityClick(e) {
        e.preventDefault();
        e.stopPropagation();
        const $this = $(e.target);
        const $button = $this.closest(`[${ATTR_VARIANT}]`);
        const $masterButton = $this.closest(`[${ATTR_VARIANTS}]`)
        $masterButton.closest('coral-popover').hide();

        const $fieldWrapper = $masterButton.closest(`.${CLS_FIELD_WRAPPER}`);
        const $valueSource = getValueSource($fieldWrapper);
        const $valueTarget = getValueTarget($fieldWrapper);
        const acceptDelegate = $valueTarget.get(0).acceptDelegate || function (value) {
            setValue($valueTarget, value);
        }
        const dialogSetup = {
            sourceField: getSettingsSourceFieldName($fieldWrapper),
            settings: {
                text: getValue($valueSource)
            },
            variants: JSON.parse($masterButton.attr(ATTR_VARIANTS)),
            selectedVariantId: $button.attr(ATTR_VARIANT),
            acceptDelegate: acceptDelegate
        };
        const dialogSize = ns.Assistant.getDialogSize($button);
        if (dialogSize) {
            dialogSetup.size = dialogSize;
        }
        ns.Assistant.openRequestDialog(dialogSetup);
    }

    function handleTextInputChange(e) {
        const $this = $(e.target);
        const $button = $this.closest(`.${CLS_FIELD_WRAPPER}`).find(`.${CLS_ACTION_BUTTON}`);
        if ($this.val()) {
            $button.removeAttr('disabled');
        } else {
            $button.attr('disabled', true);
        }
    }

    function handleSettingChange(e) {
        const $this = $(e.target);
        const settingReference = $this.attr(ns.Assistant.ATTR_SOURCE_SETTING_REF);
        if (!settingReference || !settingReference.includes(FIELD_SETTING_DELIMITER)) {
            return;
        }
        const sourceFieldName = settingReference.split(FIELD_SETTING_DELIMITER)[0];
        const settingName = settingReference.split(FIELD_SETTING_DELIMITER)[1];
        ns.Assistant.storeSettings(sourceFieldName, settingName, $this.val());
    }

    function getValueSource($container) {
        const candidate = $container.find(`[${ns.Assistant.ATTR_SOURCE}]`);
        return candidate.length ? candidate : $container.find('[name]');
    }

    function getValueTarget($container) {
        const candidate = $container.find(`[${ns.Assistant.ATTR_TARGET}]`);
        return candidate.length ? candidate : getValueSource($container);
    }

    function getSettingsSourceFieldName($container) {
        const sourceFieldValue = $container.find(`[${ns.Assistant.ATTR_SOURCE_SETTING_REF}]`).attr(ns.Assistant.ATTR_SOURCE_SETTING_REF);
        if (sourceFieldValue) {
            return sourceFieldValue.split(FIELD_SETTING_DELIMITER)[0]
        }
        return $container.find('[name]').attr('name');
    }


    function getValue($source) {
        const start = $source[0].selectionStart;
        const end = $source[0].selectionEnd;
        if (end > start) {
            return $source.val().substring(start, end);
        }
        return $source.val();
    }

    function setValue($target, text) {
        const start = $target[0].selectionStart;
        const end = $target[0].selectionEnd;
        if (end > start) {
            const newText = $target.val().substring(0, start) + text + $target.val().substring(end);
            $target.val(newText);
        } else {
            $target.val(text);
        }
    }
})(window.eak = window.eak || {});
