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
 */(function (RTE, Class) {
    'use strict';

    const FEATURE = 'writesonic';
    RTE.ui.cui.PopupButton = new Class({
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
                self.plugin.execute('run', action, $this.data('action-params'));
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
})(window.CUI.rte, window.Class);
