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

    const CLS_LOADING = 'is-loading';
    const CLS_ERROR = 'is-error';
    const CLS_DISABLED_ON_LOADING = 'disabled-on-loading';

    ns.Assistant = ns.Assistant || {};

    ns.Assistant.openRequestDialog = function (setup = {}) {
        const $dialog = produceRequestDialog(
            {
                id: 'assistant-request',
                header: {
                    innerHTML: 'Assistant'
                },
                content: {
                    innerHTML: `
                        <vendors class="flex-block">
                            <coral-select class="coral-Form-field grow"></coral-select>
                            <button is="coral-button" class="refresh-button ${CLS_DISABLED_ON_LOADING}" variant="secondary" icon="refresh"></button>
                            <button is="coral-button" class="settings-button" variant="secondary" icon="gears"></button>
                        </vendors>
                        <notifications></notifications>
                        <coral-wait size="M"></coral-wait>
                        <options class="grow scrollable"></options>`
                },
                footer: {
                    innerHTML: '<button is="coral-button" variant="secondary" coral-close>Cancel</button>'
                }
            });
        $dialog.data(ns.Assistant.DATA_KEY_SETUP, setup);
        adjustRequestDialogSize($dialog, setup);
        populateVendorsList($dialog, setup);
        $dialog.get(0).show();
        runRequest($dialog, Object.assign(setup, {command: setup.selectedVariantId || setup.variants[0].id}));
    };

    function produceRequestDialog(content) {
        let $dialog = $(document).find('#' + content.id);
        if ($dialog.length) {
            return $dialog;
        }
        const dialog = new Coral.Dialog().set(content);
        document.body.appendChild(dialog);
        $dialog = $(dialog);

        $dialog.on('coral-overlay:close', function () {
            dialog.abortController && dialog.abortController.abort();
        });

        $dialog.find('.refresh-button').on('click', function () {
            $dialog.find('vendors coral-select').trigger('change');
        });

        $dialog.find('.settings-button').on('click', function () {
            ns.Assistant.openSettingsDialog($dialog, function () {
                $dialog.get(0).abortController && $dialog.get(0).abortController.abort();
                runRequest($dialog);
            });
        });

        const $options = $dialog.find('options');
        $options.on('click', 'button', handleOptionClick);
        $options.on('click', '[data-url]', handleLinkClick);
        return $dialog;
    }

    function adjustRequestDialogSize($dialog, setup) {
        if (!setup.size) {
            return;
        }
        const dialogWrapper = $dialog.find('.coral3-Dialog-wrapper').get(0);
        dialogWrapper.style.width = setup.size.width + 'px';
        dialogWrapper.style.height = setup.size.height + 'px';
    }

    function populateVendorsList($dialog, setup) {
        const $vendors = $dialog.find('vendors coral-select');
        const variants = setup.variants;
        $vendors.attr('disabled', variants.length <= 1);
        const vendorOptions = variants
            .map(variant => {
                const item = new Coral.Select.Item().set({
                    value: variant.id,
                    selected: variant.id === setup.selectedVariantId,
                    content: {
                        textContent: variant.title
                    }
                });
                if (variant.hasSettings) {
                    item.content.classList.add('has-settings');
                }
                return item;
            });

        Coral.commons.ready($vendors.get(0), function () {
            $vendors.get(0).items.clear();
            vendorOptions.forEach(item => $vendors.get(0).items.add(item));
            updateSettingsButton($dialog, $vendors.get(0));
        });

        $vendors.on('change', function (e) {
            e.preventDefault();
            $dialog.get(0).abortController && $dialog.get(0).abortController.abort();
            updateSettingsButton($dialog, e.target);
            Object.assign($dialog.data(ns.Assistant.DATA_KEY_SETUP), {command: $(e.target).val()});
            runRequest($dialog);
        });
    }

    function updateSettingsButton($dialog, vendorsList) {
        $dialog.find('.settings-button').attr('disabled', function () {
            const targetItem = vendorsList.selectedItem || vendorsList.items.first();
            return !targetItem || !$(targetItem).is('.has-settings');
        });
    }

    function runRequest($dialog) {
        const setup = $dialog.data(ns.Assistant.DATA_KEY_SETUP);
        const command = setup.command || $dialog.find('vendors coral-select').val();
        const text = (setup.settings && setup.settings.text) || '';

        setLoading($dialog);
        $dialog.find('notifications').empty();
        $dialog.find('options').empty();
        $dialog.find('coral-dialog-header').text(getHeaderText(command, text));

        const effectiveSettings = Object.assign({}, setup.settings, ns.Assistant.getSettings(setup.sourceField, setup.command));
        const effectiveSetup = Object.assign(
            {},
            setup,
            {command: command, settings: effectiveSettings});

        const serviceLink = ns.Assistant.getServiceLink(effectiveSetup);
        const abortController = new AbortController();
        $dialog.get(0).abortController = abortController;

        fetch(serviceLink, {signal: abortController.signal})
            .then((res) => res.json())
            .then((json) => populateOptionList($dialog, json))
            .then(() => unsetLoading($dialog))
            .catch((err) => displayError($dialog, err));
    }

    function getHeaderText(command, text) {
        let commandTitle = command.includes('.') ? command.split('.').slice(0, -1) : [command];
        commandTitle = commandTitle.slice(-1)[0];
        commandTitle = commandTitle.substring(0, 1).toUpperCase() + commandTitle.substring(1).toLowerCase();

        const truncatedText = text.length <= 40 ? text : text.substring(0, 37) + '...';
        return `${commandTitle} "${truncatedText}"`;
    }

    async function populateOptionList($dialog, solution) {
        if (!solution) {
            throw new Error('Empty result received');
        }
        const messages = solution.messages;
        if (Array.isArray(messages) && messages[0]) {
            throw new Error(messages[0]);
        } else if (messages) {
            throw new Error(messages);
        }
        const $optionsContent = $dialog.find('options');
        const $list = $('<coral-buttonlist></coral-buttonlist>');
        if (Array.isArray(solution.options)) {
            const imageMode = solution.args && /^image\./.test(solution.args.cmd);
            if (imageMode) {
                await populateImageOptionList(solution, $list);
            } else {
                populateTextOptionList(solution, $list);
            }
        }
        $optionsContent.empty().append($list);
    }

    function populateTextOptionList(solution, $list) {
        const options = solution.options || solution;
        for (const option of options) {
            const text = option.text || option;
            const buttonHtml = `<button is="coral-buttonlist-item" icon="text" class="text-option">${text}</button>`;
            $(buttonHtml).appendTo($list);
        }
    }

    async function populateImageOptionList(solution, $list) {
        const options = solution.options || solution.images || solution;
        const preloadResult = await preloadImages(options.map(option => option.text || option));
        const validImages = {};
        preloadResult
            .filter((entry) => entry.image)
            .forEach((entry) => validImages[entry.url] = entry.image);
        for (const option of options) {
            const url = option.text || option;
            if (!validImages[url]) {
                continue;
            }
            const quickActions = new Coral.QuickActions();
            quickActions.target = '_prev';
            const quickActionsItem = new Coral.QuickActions.Item().set({
                type: 'button',
                icon: 'preview',
                content: {
                    textContent: 'Preview'
                }
            });
            quickActionsItem.setAttribute('data-url', url);
            quickActions.items.add(quickActionsItem);

            const button = new Coral.ButtonList.Item().set({
                value: url
            });
            button.classList.add('image-option');
            button.appendChild(validImages[url]);
            button.appendChild(quickActions);
            $list.append($(button));
        }
    }

    function preloadImages(urls) {
        const promises = urls.map((url) => new Promise((resolve) => {
            const image = new Image();
            image.onload = () => resolve({ url: url, image: image });
            image.onerror = () => resolve({ url: url, image: null });
            image.src = url;
        }));
        return Promise.all(promises);
    }

    function setLoading($dialog) {
        $dialog.removeClass(CLS_ERROR).addClass(CLS_LOADING).find(`.${CLS_DISABLED_ON_LOADING}`).attr('disabled', true);
        return $dialog;
    }

    function unsetLoading($dialog) {
        $dialog.removeClass(CLS_LOADING).find(`.${CLS_DISABLED_ON_LOADING}`).removeAttr('disabled');
        return $dialog;
    }

    function displayError($dialog, err) {
        if (err.toString().includes('AbortError')) {
            return;
        }
        unsetLoading($dialog).addClass(CLS_ERROR);
        $dialog.find('notifications').append(`<coral-alert variant="warning"><coral-alert-content>${err}</coral-alert-content></coral-alert>`);
    }

    async function handleOptionClick() {
        const $this = $(this);
        const $dialog = $this.closest('coral-dialog');
        const value = $this.attr('value') || $(this).find('coral-list-item-content').text();
        const acceptDelegate = $dialog.data(ns.Assistant.DATA_KEY_SETUP).acceptDelegate;
        if (value && acceptDelegate) {
            setLoading($dialog);
            try {
                await acceptDelegate(value);
                unsetLoading($dialog);
                $dialog.get(0).hide();
            } catch (e) {
                displayError($dialog, e);
            }
        }
    }

    function handleLinkClick(e) {
        e.preventDefault();
        e.stopPropagation();
        const $this = $(this);
        const url = $this.data('url');
        window.open(url, '_blank');
    }
})(window.eak = window.eak || {});
