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
(function ($, ns, editor) {
    'use strict';

    const SELECTOR_ACCEPT_BUTTON = 'button[variant="primary"]';

    ns.Assistant = ns.Assistant || {};

    ns.Assistant.openPageCreatorDialog = function () {
        let $dialog = producePageCreatorDialog(
            {
                id: 'assistant-page-creator',
                header: {
                    innerHTML: 'Design an idea for the page'
                },
                content: {
                    innerHTML: `<textarea is="coral-textarea" name="prompt" class="coral-Form-field foundation-layout-util-resizable-none coral3-Textfield coral3-Textfield--multiline" placeholder="E.g.: AI is the future of content authoring" rows="20" data-eak-assistant-source></textarea>
                                <notifications class="visible-on-error"></notifications>
                                <coral-wait size="M" class="visible-on-loading"></coral-wait>`
                },
                footer: {
                    innerHTML:
                        '<button is="coral-button" variant="primary" class="disabled-on-loading disabled-on-error" disabled>OK</button>' +
                        '<button is="coral-button" variant="secondary" coral-close>Cancel</button>'
                },
                movable: true,
                backdrop: 'static'
            });
        $dialog = ns.Assistant.unsetLoadingState($dialog);
        $dialog = ns.Assistant.unsetErrorState($dialog);
        $dialog[0].show();
        loadPageTitle($dialog);
    };

    function producePageCreatorDialog(content) {
        let $dialog = $(document).find('#' + content.id);
        if ($dialog.length) {
            return $dialog;
        }
        $dialog = $(new Coral.Dialog().set(content));

        $dialog.addClass(`${ns.Assistant.CLS_ASSISTANT_DIALOG} coral-Form--vertical fixed-size`);
        $dialog.find('coral-dialog-content').addClass('hidden-on-error hidden-on-loading');

        $dialog.on('click', SELECTOR_ACCEPT_BUTTON, handleAcceptButtonClick);
        $dialog.on('keydown', `[${ns.Assistant.ATTR_SOURCE}]`, $.debounce(ns.Assistant.DEBOUNCE_DELAY, handleTextInputChange));

        document.body.appendChild($dialog.get(0));
        return $dialog;
    }

    function loadPageTitle($dialog) {
        const path = editor.page.path;
        if (!path) {
            return;
        }
        fetch(path + '/jcr:content.json')
            .then((response) => response.json())
            .then((json) => {
                const title = json['jcr:title'];
                title && $dialog.find('[name="prompt"]').val(title);
            });
    }

    async function runRequest($dialog) {
        ns.Assistant.abortNetworkRequest($dialog);
        $dialog.data(ns.Assistant.DATA_KEY_ABORT_CONTROLLER, new AbortController());
        const settings = getCreatePageSettings($dialog);
        if (!settings.path || !settings.text) {
            console.error('[Assistant] Missing page path and/or prompt');
            return false;
        }
        try {
            let response = {};
            do {
                response = await runRequestStage($dialog, settings, response.next);
            } while (response.next);
            return true;
        } catch (err) {
            ns.Assistant.setErrorState($dialog, err);
        }
        return false;
    }

    async function runRequestStage($dialog, settings, continuation = {}) {
        ns.Assistant.setLoadingState($dialog, continuation.message || 'Designing content');
        const effectiveSettings = Object.assign({}, settings, { stage: continuation.stage || 'summary' })
        const serviceLink = ns.Assistant.getServiceLink({
            command: 'page.create.oai',
            settings: effectiveSettings
        });
        const fetchResult = await fetch(serviceLink, { method: 'POST', signal: $dialog.data(ns.Assistant.DATA_KEY_ABORT_CONTROLLER).signal });
        if (fetchResult.status !== 200) {
            const message = await getExceptionMessage(fetchResult) || 'HTTP request returned status ' + fetchResult.status;
            throw new Error(message);
        }
        try {
            return await fetchResult.json();
        } catch (e) {
            throw new Error('Empty response');
        }
    }

    async function getExceptionMessage(fetchResult) {
        try {
            const json = await fetchResult.json();
            if (json.messages) {
                return [].concat(json.messages).join('');
            }
        } catch (e) {
            return undefined;
        }
    }

    function getCreatePageSettings($dialog) {
        let pageSrc = editor.page && editor.page.path;
        if (!pageSrc) {
            const $form = $dialog.find('form');
            pageSrc = ($form.attr('data-cq-dialog-pageeditor') || '')
                .replace(/(^\/editor\.html|\.html$)/gi, '');
        }
        const text = $dialog.find('textarea').val();
        return { path: pageSrc, text: text };
    }

    async function handleAcceptButtonClick(e) {
        e.preventDefault();
        const $dialog = $(e.target).closest('coral-dialog');
        const success = await runRequest($dialog);
        if (success) {
            editor.ContentFrame.reload();
            $dialog.get(0).hide();
        }
    }

    function handleTextInputChange () {
        const $this = $(this);
        const $dialog = $this.closest('coral-dialog');
        const $button = $dialog.find(SELECTOR_ACCEPT_BUTTON);
        if ($this.val()) {
            $button.removeAttr('disabled');
        } else {
            $button.attr('disabled', true);
        }
    }
})(Granite.$, window.eak = window.eak || {}, Granite.author);
