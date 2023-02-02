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
(function ($, ns) {
    'use strict';

    const WRAPPER_SELECTOR = 'eak-Form-field-wrapper';
    const INPUT_SELECTOR = '[data-eak-plugins*="ai"]';
    const BUTTON_SELECTOR = '[is="coral-anchorbutton"]';

    $(document).on('coral-overlay:open', function (e) {
        const $dialog = $(e.target);
        $dialog.find(INPUT_SELECTOR).each(async function () {
            const $input = $(this);
            const buttonId = 'input-' + $input.attr('name').replace(/^[^\w]+|[^\w]+$/, '').replace(':', '-');
            const menuOptions = await ns.Ai.getMenuOptions();
            const menuContent = '<coral-buttonlist>' +
                menuOptions
                    .map(option => {
                        const variants = option.variants
                            ? option.variants.map(v => { return { title: v.vendor, id: v.id }; })
                            : [{ title: option.vendor, id: option.id }];
                        const variantsString = JSON.stringify(variants);
                        const paramsString = option.params
                            ? JSON.stringify(option.params).replace(/"/g, '&quot;')
                            : '';
                        return `<button is="coral-buttonlist-item" icon="${option.icon}" data-variants=${variantsString} data-params="${paramsString}" value="${option.id}">${option.title}</button>`;
                    })
                    .join('') +
                '</coral-buttonlist>';

            const $wrapper = $input.wrap(`<div class="${WRAPPER_SELECTOR}"></div>`).parent();
            const $button = $(`<a is="coral-anchorbutton" id="${buttonId}" icon="ai" iconsize="S"></a>`);
            if (!$input.val()) {
                $button.attr('disabled', true);
            }
            $button.appendTo($wrapper);

            const $popover = $(`<coral-popover placement="bottom" target="#${buttonId}"><coral-popover-content>${menuContent}</coral-popover-content></coral-popover>`).appendTo($wrapper);
            $popover.on('click', 'button', onMenuItemClick);
        });
        $dialog.on('keydown', INPUT_SELECTOR, $.debounce(500, onTextChange));
    });

    async function onMenuItemClick(e) {
        e.preventDefault();
        const $this = $(this);
        $this.closest('coral-popover').hide();
        const command = $this.attr('value');
        if (command === 'settings') {
            return ns.Ai.openSettingsDialog();
        }

        const $input = $this.closest('.' + WRAPPER_SELECTOR).find('input');
        const dialogSetup = {
            payload: getText($input),
            variants: $this.data('variants'),
            params: $this.data('params'),
            acceptDelegate: function (text) {
                setText($input.attr('name'), text);
            }
        };
        ns.Ai.openRequestDialog(dialogSetup);
    }

    function getText($input) {
        const start = $input[0].selectionStart;
        const end = $input[0].selectionEnd;
        if (end > start) {
            return $input.val().substring(start, end);
        }
        return $input.val();
    }

    function setText(name, text) {
        const $input = $(document).find(`[name="${name}"]`);
        const start = $input[0].selectionStart;
        const end = $input[0].selectionEnd;
        if (end > start) {
            const newText = $input.val().substring(0, start) + text + $input.val().substring(end);
            $input.val(newText);
        } else {
            $input.val(text);
        }
    }

    function onTextChange(e) {
        const $this = $(e.target);
        const $button = $this.closest('.' + WRAPPER_SELECTOR).find(BUTTON_SELECTOR);
        if ($this.val()) {
            $button.removeAttr('disabled');
        } else {
            $button.attr('disabled', true);
        }
    }
})(Granite.$, window.eak = window.eak || {});