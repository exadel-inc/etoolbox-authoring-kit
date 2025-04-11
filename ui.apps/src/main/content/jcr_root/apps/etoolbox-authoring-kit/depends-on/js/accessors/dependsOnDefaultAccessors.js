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
 * DependsOn ElementAccessors defaults
 * */
(function ($, ns) {
    'use strict';

    const FIELD_WRAPPER = '.coral-Form-fieldwrapper';

    ns.ElementAccessors.registerAccessor({
        selector: '*',
        preferableType: 'string',
        findTarget: function ($el) {
            if ($el.length > 1) {
                console.warn('[DependsOn]: requested a reference with multiple targets, the first target is used.', $el);
            }
            return $el.first();
        },
        findWrapper: function ($el) {
            return $el.closest(FIELD_WRAPPER);
        },
        get: function ($el) {
            return $el.val() || '';
        },
        set: function ($el, value, notify) {
            if (ns.isObject(value)) {
                value = JSON.stringify(value);
            }
            $el.val(value);
            notify && $el.trigger('change');
        },
        placeholder: function ($el, value) {
            $el.attr('placeholder', value);
        },
        readonly: function ($el, state) {
            $el.attr('readonly', state ? 'true' : null);
        },
        required: function ($el, val) {
            const fieldApi = $el.adaptTo('foundation-field');
            if (fieldApi && typeof fieldApi.setRequired === 'function') {
                fieldApi.setRequired(val);
            } else {
                $el.attr('required', val ? 'true' : null);
            }
            ns.ElementAccessors.updateValidity($el, true);
        },
        visibility: function ($el, state) {
            $el.attr('hidden', state ? null : 'true');
            ns.ElementAccessors.findWrapper($el)
                .attr('hidden', state ? null : 'true')
                .attr('data-dependson-controllable', 'true');
            // Force update validity if the field is hidden
            if (!state) {
                ns.ElementAccessors.updateValidity($el);
            }
            ns.ElementAccessors.clearValidity($el);
            ns.ElementAccessors.updateSubmittables($el.parent());
        },
        disabled: function ($el, state) {
            $el.attr('disabled', state ? 'true' : null);
            ns.ElementAccessors.findWrapper($el).attr('disabled', state ? 'true' : null);

            const fieldAPI = $el.adaptTo('foundation-field');
            // Try to disable the field by foundation api
            if (fieldAPI && fieldAPI.setDisabled) {
                fieldAPI.setDisabled(state);
            }
            // Force update validity if field disabled
            if (state) {
                ns.ElementAccessors.updateValidity($el);
            }
            ns.ElementAccessors.clearValidity($el);
        }
    });
})(Granite.$, Granite.DependsOnPlugin = (Granite.DependsOnPlugin || {}));
