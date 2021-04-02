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

/**
 * @author Alexey Stsefanovich (ala'n), Bernatskaya Yana (YanaBr)
 *
 * Coral 3 RTE accessor
 * */
(function ($, ns) {
    const RTE_CONTAINER = '.cq-RichText';
    const RTE_DATA_INSTANCE = 'rteinstance';
    const RTE_INPUT_SELECTOR = 'input[type="hidden"][data-cq-richtext-input]';
    const RTE_EDITOR_SELECTOR = '.cq-RichText-editable';

    ns.ElementAccessors.registerAccessor({
        selector: `${RTE_CONTAINER} ${RTE_INPUT_SELECTOR}`,
        findTarget: function ($el) {
            return $el.closest(RTE_CONTAINER).find(RTE_EDITOR_SELECTOR);
        }
    });
    ns.ElementAccessors.registerAccessor({
        selector: `${RTE_CONTAINER} ${RTE_EDITOR_SELECTOR}, ${RTE_CONTAINER} ${RTE_INPUT_SELECTOR}`,
        preferableType: 'string',
        get: function ($el) {
            return $el.closest(RTE_CONTAINER).find(RTE_INPUT_SELECTOR).val() || '';
        },
        set: function ($el, value, notify) {
            const $rteContainer = $el.closest(RTE_CONTAINER);
            const $editor = $rteContainer.find(RTE_EDITOR_SELECTOR);
            const rteInstance = $editor.data(RTE_DATA_INSTANCE);
            const updateValue = () => {
                rteInstance.setContent && rteInstance.setContent(value);
                notify && $editor.trigger('change');
            };

            if (!rteInstance) return;

            $rteContainer.find(RTE_INPUT_SELECTOR).val(value);
            rteInstance.isActive ? updateValue() : rteInstance.on('editing-start', updateValue);
        },
        required: function ($el, val) {
            const $rteContainer = $el.closest(RTE_CONTAINER);
            const $rteInput = $rteContainer.find(RTE_INPUT_SELECTOR);
            $rteInput.each(function () { this.required = !!val; });
            $rteContainer.find(RTE_EDITOR_SELECTOR).attr('aria-required', !!val);
            ns.ElementAccessors.updateValidity($rteInput, true);
        },
        visibility: function ($el, val) {
            const $rteContainer = $el.closest(RTE_CONTAINER);
            ns.ElementAccessors.DEFAULT_ACCESSOR.visibility($rteContainer, val);
        },
        disabled: function ($el, val) {
            ns.ElementAccessors.DEFAULT_ACCESSOR.disabled($el, val);
            ns.ElementAccessors.DEFAULT_ACCESSOR.disabled($el.parent().find(RTE_INPUT_SELECTOR), val);

            const rteInstance = $el.data(RTE_DATA_INSTANCE);
            if (!rteInstance) return;

            // disable rte editing
            setTimeout(function () {
                if (val) {
                    rteInstance.suspend();
                } else {
                    // use old content as initial content to reactivate rte
                    const initContent = rteInstance.editorKernel && rteInstance.editorKernel.getProcessedHtml();
                    rteInstance.reactivate(initContent);
                }
            }, 100);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
