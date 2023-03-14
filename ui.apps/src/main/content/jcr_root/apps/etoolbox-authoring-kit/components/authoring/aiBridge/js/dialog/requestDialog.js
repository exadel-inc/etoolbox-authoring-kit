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

    const CLS_ERROR = 'is-error';
    const CLS_LOADING = 'is-loading';
    const CLS_DISABLED_ON_LOADING = 'disabled-on-loading';

    const SELECTOR_DIALOG = 'coral-dialog';
    const SELECTOR_FACILITIES = 'facilities coral-select';

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
                        <facilities class="flex-block">
                            <coral-select class="coral-Form-field grow"></coral-select>
                            <button is="coral-button" class="refresh-button ${CLS_DISABLED_ON_LOADING}" variant="secondary" icon="refresh"></button>
                            <button is="coral-button" class="settings-button" variant="secondary" icon="gears"></button>
                        </facilities>
                        <notifications></notifications>
                        <coral-wait size="M"></coral-wait>
                        <options class="grow scrollable"></options>`
                },
                footer: {
                    innerHTML: '<button is="coral-button" variant="secondary" coral-close>Cancel</button>'
                },
                movable: true,
                backdrop: 'static'
            });

        $dialog.data(
            ns.Assistant.DATA_KEY_SETUP,
            Object.assign(setup, { command: setup.selectedVariantId || setup.variants[0].id })
        );
        populateFacilityVariantsList($dialog, setup);
        $dialog.attr(ns.Assistant.ATTR_ASSISTANT_MODE, 'options');
        Coral.commons.ready($dialog.get(0), function () {
            adjustRequestDialogSize($dialog);
        });
        unsetLoadingState($dialog).removeClass(CLS_ERROR);
        $dialog.get(0).show();
        startRequest($dialog);
    };

    function produceRequestDialog(content) {
        let $dialog = $(document).find('#' + content.id);
        if ($dialog.length) {
            return $dialog;
        }

        const dialog = new Coral.Dialog().set(content);
        document.body.appendChild(dialog);

        $dialog = $(dialog);
        $dialog.find('coral-dialog-header,coral-dialog-content,coral-dialog-footer').addClass('assistant-options');

        $dialog.on('coral-overlay:close', handleDialogClose);
        $dialog.find('.settings-button').on('click', handleSettingsButtonClick);
        $dialog.find('.refresh-button').on('click', handleRefreshButtonClick);
        $dialog.find(SELECTOR_FACILITIES).on('change', handleFacilityVariantChange);

        const $options = $dialog.find('options');
        $options.on('click', 'button', handleOptionClick);
        $options.on('click', '[data-url]', handleLinkClick);

        return $dialog;
    }

    function adjustRequestDialogSize($dialog) {
        const setup = $dialog.data(ns.Assistant.DATA_KEY_SETUP);
        if (!setup.callerDialog) {
            return;
        }
        const $callerDialog = $(setup.callerDialog);
        if ($callerDialog.is('.coral3-Dialog--fullscreen')) {
            return;
        }
        const dialogWrapper = $dialog.find('.coral3-Dialog-wrapper').get(0);
        const callerDialogWrapper = $callerDialog.find('.coral3-Dialog-wrapper').get(0);
        dialogWrapper.style.marginTop = callerDialogWrapper.style.marginTop;
        dialogWrapper.style.marginLeft = callerDialogWrapper.style.marginLeft;
        dialogWrapper.style.width = callerDialogWrapper.style.width || $(callerDialogWrapper).outerWidth() + 'px';
        dialogWrapper.style.height = callerDialogWrapper.style.height || $(callerDialogWrapper).outerHeight() + 'px';
    }

    function populateFacilityVariantsList($dialog, setup) {
        const $facilityVariantsList = $dialog.find(SELECTOR_FACILITIES);
        const setupVariants = setup.variants;
        $facilityVariantsList.attr('disabled', setupVariants.length <= 1);
        const vendorOptions = setupVariants
            .map(variant => new Coral.Select.Item().set({
                    value: variant.id,
                    selected: variant.id === setup.selectedVariantId,
                    content: {
                        textContent: variant.title
                    }
                }));

        Coral.commons.ready($facilityVariantsList.get(0), function () {
            $facilityVariantsList.get(0).items.clear();
            vendorOptions.forEach(item => $facilityVariantsList.get(0).items.add(item));
            updateSettingsButton($dialog, $facilityVariantsList);
        });
    }

    function updateSettingsButton($dialog, $facilityVariants) {
        const setup = $dialog.data(ns.Assistant.DATA_KEY_SETUP);
        $dialog.find('.settings-button').attr('disabled', function () {
            const targetItem = $facilityVariants.get(0).selectedItem || $facilityVariants.get(0).items.first();
            if (!targetItem) {
                return true;
            }
            const matchingVariant = setup.variants.filter((variant) => variant.id === targetItem.value)[0];
            return !matchingVariant || !matchingVariant.persistentSettings || !matchingVariant.persistentSettings.length;
        });
    }

    function startRequest($dialog) {
        const setup = $dialog.data(ns.Assistant.DATA_KEY_SETUP);

        const selectedFacilityVariant = setup.command
            ? setup.variants.filter((variant) => variant.id === setup.command)[0]
            : setup.variants[0];

        const command = selectedFacilityVariant?.id;
        const text = (setup.settings && setup.settings.text) || '';

        if (!command || !text) {
            return;
        }

        setLoadingState($dialog);
        $dialog.find('coral-dialog-header').html(getHeaderText(command, text));

        const effectiveSettings = Object.assign(
            {},
            setup.settings,
            ns.Assistant.getSettings(setup.sourceField, setup.command));

        if (!isAheadSettingsDialogNeeded(selectedFacilityVariant, effectiveSettings)) {
            return completeRequest($dialog, effectiveSettings);
        }

        const transientSettings = selectedFacilityVariant.transientSettings;
        const displayedSettings = [].concat(selectedFacilityVariant.requiredSettings).concat(transientSettings);
        ns.Assistant.openSettingsUi(
            $dialog,
            function (moreSettings) {
                return completeRequest($dialog, Object.assign(effectiveSettings, moreSettings))
            },
            {
                closesParent: true,
                display: displayedSettings,
                doNotSave: transientSettings
            }
        );
    }

    function completeRequest($dialog, settingsObject) {
        const setup = $dialog.data(ns.Assistant.DATA_KEY_SETUP);

        const command = setup.command || setup.variants[0].id;
        const effectiveSetup = Object.assign(
            {},
            setup,
            { command: command, settings: settingsObject });

        const serviceLink = ns.Assistant.getServiceLink(effectiveSetup);

        const abortController = new AbortController();
        $dialog.get(0).abortController = abortController;

        setLoadingState($dialog);
        $dialog.find('notifications').empty();
        $dialog.find('options').empty();

        fetch(serviceLink, {signal: abortController.signal})
            .then((res) => res.json())
            .then((json) => populateOptionList($dialog, json))
            .then(() => unsetLoadingState($dialog))
            .catch((err) => displayError($dialog, err));
    }

    function isAheadSettingsDialogNeeded (facilityVariant, settingsObject) {
        return facilityVariant.transientSettings.length
            || facilityVariant.requiredSettings.some((name) => !settingsObject[name]);
    }

    function getHeaderText(command, text) {
        let commandTitle = command.includes('.') ? command.split('.').slice(0, -1) : [command];
        commandTitle = commandTitle.slice(-1)[0];
        commandTitle = commandTitle.substring(0, 1).toUpperCase() + commandTitle.substring(1).toLowerCase();
        return `${commandTitle} <span class="ellipsis">${text}</span>`;
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

    function setLoadingState($dialog) {
        $dialog.removeClass(CLS_ERROR).addClass(CLS_LOADING).find(`.${CLS_DISABLED_ON_LOADING}`).attr('disabled', true);
        return $dialog;
    }

    function unsetLoadingState($dialog) {
        $dialog.removeClass(CLS_LOADING).find(`.${CLS_DISABLED_ON_LOADING}`).removeAttr('disabled');
        return $dialog;
    }

    function displayError($dialog, err) {
        if (err.toString().includes('AbortError')) {
            return;
        }
        unsetLoadingState($dialog).addClass(CLS_ERROR);
        $dialog.find('notifications').append(`<coral-alert variant="warning"><coral-alert-content>${err}</coral-alert-content></coral-alert>`);
    }

    function handleDialogClose (e) {
        const dialog = e.target;
        dialog.abortController && dialog.abortController.abort();
    }

    async function handleOptionClick() {
        const $this = $(this);
        const $dialog = $this.closest(SELECTOR_DIALOG);
        const value = $this.attr('value') || $(this).find('coral-list-item-content').text();
        const acceptDelegate = $dialog.data(ns.Assistant.DATA_KEY_SETUP).acceptDelegate;
        if (value && acceptDelegate) {
            setLoadingState($dialog);
            const readyValue = value.toString().trim().replace(/\n/g, '<br>');
            try {
                await acceptDelegate(readyValue);
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

    function handleRefreshButtonClick() {
        const $dialog = $(this).closest(SELECTOR_DIALOG);
        $dialog.find(SELECTOR_FACILITIES).trigger('change');
    }

    function handleSettingsButtonClick() {
        const $dialog = $(this).closest(SELECTOR_DIALOG);
        const setup = $dialog.data(ns.Assistant.DATA_KEY_SETUP);
        ns.Assistant.openSettingsUi($dialog, function () {
            $dialog.get(0).abortController && $dialog.get(0).abortController.abort();
            completeRequest($dialog, Object.assign({}, setup.settings, ns.Assistant.getSettings(setup.sourceField, setup.command)));
        });
    }

    function handleFacilityVariantChange() {
        const $this = $(this);
        const $dialog = $this.closest(SELECTOR_DIALOG);
        $dialog.get(0).abortController && $dialog.get(0).abortController.abort();
        updateSettingsButton($dialog, $this);
        Object.assign($dialog.data(ns.Assistant.DATA_KEY_SETUP), { command: $this.val() });
        startRequest($dialog);
    }

})(window.eak = window.eak || {});
