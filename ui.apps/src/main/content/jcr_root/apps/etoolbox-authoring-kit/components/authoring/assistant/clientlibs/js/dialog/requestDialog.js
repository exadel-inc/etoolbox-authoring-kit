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
(function (ns, editor) {
    'use strict';

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
                            <button is="coral-button" class="refresh-button disabled-on-loading" variant="secondary" icon="refresh"></button>
                            <button is="coral-button" class="settings-button" variant="secondary" icon="gears"></button>
                        </facilities>
                        <notifications class="visible-on-error"></notifications>
                        <coral-wait size="M" class="visible-on-loading"></coral-wait>
                        <options class="grow scrollable hidden-on-loading hidden-on-error"></options>`
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
        ns.Assistant.unsetErrorState(ns.Assistant.unsetLoadingState($dialog));
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
        $dialog.addClass(ns.Assistant.CLS_ASSISTANT_DIALOG);
        $dialog.find('coral-dialog-header,coral-dialog-content,coral-dialog-footer').addClass('assistant-options');

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
        const allowEmptyText = setup.settings && setup.settings.allowEmpty;

        if (!command || (!text && !allowEmptyText)) {
            return;
        }

        ns.Assistant.setLoadingState($dialog, 'Processing request');

        const headerText = getHeaderText(setup.settings.title || command);
        const headerTextWithExtract = headerText + (text.trim().length ? ` <span class="citation">${text.trim()}</span>` : '');

        $dialog.attr(ns.Assistant.ATTR_TITLE, headerText);
        $dialog.find('coral-dialog-header').html(headerTextWithExtract);

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
        const effectiveSettings = Object.assign(settingsObject, { pagePath: editor.page && editor.page.path });
        const effectiveSetup = Object.assign(
            {},
            setup,
            { command: command, settings: effectiveSettings });

        const serviceLink = ns.Assistant.getServiceLink(effectiveSetup);

        const abortController = new AbortController();
        $dialog.data(ns.Assistant.DATA_KEY_ABORT_CONTROLLER, abortController);

        ns.Assistant.setLoadingState($dialog, 'Processing request');
        $dialog.find('notifications').empty();
        $dialog.find('options').empty();

        fetch(serviceLink, {signal: abortController.signal})
            .then((res) => res.json())
            .then((json) => populateOptionList($dialog, json))
            .then(() => ns.Assistant.unsetLoadingState($dialog))
            .then(() => {
                if ($dialog.find('options button').length === 1) {
                    $dialog.find('options button').get(0).click();
                }
            })
            .catch((err) => ns.Assistant.setErrorState($dialog, err));
    }

    function isAheadSettingsDialogNeeded (facilityVariant, settingsObject) {
        return facilityVariant.transientSettings.length
            || facilityVariant.requiredSettings.some((name) => !settingsObject[name]);
    }

    function getHeaderText(title) {
        let commandTitle = /(\w+\.)+\w+/.test(title) ? title.split('.').slice(0, -1) : [title];
        commandTitle = commandTitle.slice(-1)[0];
        return commandTitle.substring(0, 1).toUpperCase() + commandTitle.substring(1).toLowerCase();
    }

    async function populateOptionList($dialog, solution) {
        if (!solution) {
            throw new Error('Empty response');
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

    async function handleOptionClick() {
        const $this = $(this);
        const $dialog = $this.closest(SELECTOR_DIALOG);
        const setup = $dialog.data(ns.Assistant.DATA_KEY_SETUP);
        const value = $this.attr('value') || $(this).find('coral-list-item-content').text();
        const acceptDelegate = setup.acceptDelegate;
        if (value && acceptDelegate) {
            ns.Assistant.setLoadingState($dialog, setup.acceptingMessage);
            const readyValue = value.toString().trim().replace(/\n/g, '<br>');
            try {
                await acceptDelegate(readyValue);
                ns.Assistant.unsetLoadingState($dialog);
                $dialog.get(0).hide();
            } catch (e) {
                ns.Assistant.setErrorState($dialog, e);
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
            ns.Assistant.abortNetworkRequest($dialog);
            completeRequest($dialog, Object.assign({}, setup.settings, ns.Assistant.getSettings(setup.sourceField, setup.command)));
        });
    }

    function handleFacilityVariantChange() {
        const $this = $(this);
        const $dialog = $this.closest(SELECTOR_DIALOG);
        ns.Assistant.abortNetworkRequest($dialog);
        updateSettingsButton($dialog, $this);
        Object.assign($dialog.data(ns.Assistant.DATA_KEY_SETUP), { command: $this.val() });
        startRequest($dialog);
    }

})(window.eak = window.eak || {}, Granite.author);
