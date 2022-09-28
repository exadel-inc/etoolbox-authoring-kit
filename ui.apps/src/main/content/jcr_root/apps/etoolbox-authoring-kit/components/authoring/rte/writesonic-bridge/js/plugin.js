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

    const PopupButton = new Class({
        toString: 'PopupButton',

        extend: RTE.ui.cui.ElementImpl,

        notifyToolbar: function (toolbar, skipHandlers) {
            this.superClass.notifyToolbar.call(this, toolbar, skipHandlers);

            const $tbCont = RTE.UIUtils.getToolbarContainer(toolbar.getToolbarContainer(), toolbar.tbType);
            this.$ui = $tbCont.find(`button[data-action^="#${FEATURE}"]`);
            this.superClass.$ui = this.$ui;
            this.superClass.toolbar = toolbar;
            this.setDisabled(true);

            if (skipHandlers) {
                return;
            }

            const self = this;
            const $childUi = $tbCont.find(`button[data-action^="${FEATURE}#"]`);
            $childUi.on('click.rte-handler', function () {
                const $this = $(this);
                const action = $this.data('action').split('#')[1];
                const editContext = self.plugin.editorKernel.getEditContext();
                editContext.setState('CUI.SelectionLock', 1);
                self.plugin.execute('doAction', action, $this.data('action-params'));
                self.plugin.editorKernel.enableFocusHandling();
                self.plugin.editorKernel.focus(editContext);
            });
        },

        setDisabled: function (disabled) {
            if (!disabled) {
                this.$ui.removeClass(RTE.Theme.TOOLBARITEM_DISABLED_CLASS);
                this.$ui.removeAttr('disabled');
            } else {
                this.$ui.attr('disabled', 'disabled');
                this.$ui.addClass(RTE.Theme.TOOLBARITEM_DISABLED_CLASS);
            }
        }
    });

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

            this.buttonUi = new PopupButton(FEATURE, this, false, { title: 'Writesonic Bridge' });
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
            rteTemplates.generic_dropdown = rteTemplates.generic_dropdown || this._populateDropdown;
        },

        getOptions: function () {
            return MENU_OPTIONS;
        },

        updateState: function (selfDef) {
            // this.buttonUi.$ui.attr('data-action', '#styles');
        },

        execute: function (id, value, params) {
            const command = value;
            if (command === 'settings') {
                this._produceDialog({
                    id: 'writesonic-settings',
                    header: {
                        innerHTML: 'Writesonic Settings'
                    },
                    content: {
                        innerHTML: `
                            <label id="label-num-options" class="coral-Form-fieldlabel">Number of options</label>
                            <coral-numberinput class="coral-Form-field" labelledby="label-num-options" value="5"></coral-numberinput>`
                    },
                    footer: {
                        innerHTML: '<button is="coral-button" variant="primary" coral-close>Ok</button>'
                    }
                });
            } else {
                const self = this;
                const ek = this.editorKernel;
                const selectedText = window.getSelection().toString() || ek.editContext.root.innerText || '';
                if (!selectedText.length) {
                    return;
                }
                console.debug('Processing', '"' + selectedText + '"');

                this._produceDialog({
                    id: 'writesonic-request',
                    header: {
                        innerHTML: 'Writesonic'
                    },
                    content: {
                        innerHTML: '<notifications></notifications><coral-wait size="M"></coral-wait><options></options>'
                    },
                    footer: {
                        innerHTML: '<button is="coral-button" variant="secondary" coral-close>Cancel</button>'
                    }

                }, {
                    onShow: function (dialog) {
                        const $dialog = $(dialog);
                        const title = command
                            .split('-')
                            .map(chunk => chunk.substring(0, 1).toUpperCase() + chunk.substring(1).toLowerCase())
                            .join(' ');
                        const truncatedText = selectedText.length <= 30 ? selectedText : selectedText.substring(0, 27) + '...';
                        $dialog.find('coral-dialog-header').text(`${title} "${truncatedText}"`);

                        const payloadName = params && params.payloadName ? params.payloadName : 'content_to_rephrase';
                        const body = { tone_of_voice: 'excited' };
                        body[payloadName] = selectedText;

                        fetch(`https://api.writesonic.com/v1/business/content/${command}?engine=economy&language=en`, {
                            headers: {
                                'content-type': 'application/json',
                                accept: 'application/json',
                                'X-API-KEY': 'a055b444-1abe-4c8e-afa2-59f892743f0a'
                            },
                            body: JSON.stringify(body),
                            method: 'POST'
                        })
                            .then(res => res.json())
                            .then(json => self._populateDialogOptionList(dialog, json))
                            .catch(err => self._displayDialogError(dialog, err))
                            .finally(() => $dialog.removeClass('is-loading'));
                    },

                    onAccept: function (dialog, result) {
                        dialog.hide();
                        result && ek.execCmd('inserthtml', result);
                    }
                });
            }
        },

        _populateDropdown: function (options) {
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
        },

        _produceDialog: function (content, handlers = {}) {
            let dialog = document.getElementById(content.id);
            if (!dialog) {
                dialog = new Coral.Dialog().set(content);
                document.body.appendChild(dialog);
                $(dialog).find('options').on('click', 'button', function () {
                    const text = $(this).find('coral-list-item-content').text();
                    handlers.onAccept && handlers.onAccept(dialog, text);
                });
            }

            const $dialog = $(dialog);
            $dialog.removeClass('is-error').addClass('is-loading');
            $dialog.find('notifications').empty();
            $dialog.find('options').empty();

            dialog.show();
            handlers.onShow && handlers.onShow(dialog);
        },

        _populateDialogOptionList: function (dialog, options) {
            const $content = $(dialog).find('options');
            const $list = $('<coral-buttonlist></coral-buttonlist>');
            for (const option of options) {
                $('<button></button>')
                    .attr('is', 'coral-buttonlist-item')
                    .attr('icon', 'textEdit')
                    .attr('value', option.text)
                    .text(option.text)
                    .appendTo($list);
            }
            $content.empty();
            $content.append($list);
        },

        _displayDialogError: function (dialog, err) {
            const $dialog = $(dialog);
            $dialog.addClass('is-error');
            $dialog.find('notifications').append(`<coral-alert variant="warning"><coral-alert-content></coral-alert-content>${err}</coral-alert>`);
        }
    });
    RTE.plugins.PluginRegistry.register(GROUP, WritesonicBridge);
})(window.CUI.rte, window.Class);
