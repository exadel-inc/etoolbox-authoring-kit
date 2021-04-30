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
 * CQ Coral3-based FileUpload accessor
 * */
(function ($, ns) {
    const SELECTOR = '.cq-FileUpload, .coral3-FileUpload';
    const FILEUPLOAD_FILE_REFERENCE = '[data-cq-fileupload-parameter="filereference"]';
    const FILEUPLOAD_INPUT_SELECTOR = '.coral3-FileUpload-input';

    ns.ElementAccessors.registerAccessor({
        selector: SELECTOR,
        preferableType: 'string',
        get: function ($el) {
            return $el.find(FILEUPLOAD_FILE_REFERENCE).val();
        },
        visibility: function ($el, state) {
            $el.find(FILEUPLOAD_INPUT_SELECTOR).attr('readonly', state ? null : '');
            ns.ElementAccessors.DEFAULT_ACCESSOR.visibility($el, state);
        },
        disabled: function ($el, val) {
            ns.ElementAccessors.DEFAULT_ACCESSOR.disabled($el, val);

            $el.find(':input, button').each(function () {
                this.disabled = val;
            });
        }
    });

    // CQ Coral3 Fixes
    $(document).on('click', `${SELECTOR} [coral-fileupload-clear]`, function () {
        // Clear handler is not producing change event so handle it manually
        $(this).closest(SELECTOR).trigger('change');
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
