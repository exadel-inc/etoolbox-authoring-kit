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
"use strict";

(function(window, document, $, Granite) {

    const DEFAULT_SCRIPT_SOURCE = 'https://ajaxorg.github.io/ace-builds/src-min-noconflict/ace.js';
    const EDITOR_SELECTOR = '.eak-editor';
    const VALIDATION_MESSAGE = 'Error: Please fill out this field.';

    $(document).on('foundation-contentloaded', init);

    /**
     * Registers the editor host for use with Granite UI and retrieves the Ace Editor script
     */
    function init() {
        registerFieldAdapter();
        registerValidator();
        registerValueHook();

        const scriptSrcAttribute = $(EDITOR_SELECTOR + '[data-source]').map((index, element) => $(element).data('source'));
        const scriptSrc = scriptSrcAttribute && scriptSrcAttribute.length ? scriptSrcAttribute[0] : DEFAULT_SCRIPT_SOURCE;
        $.getScript(scriptSrc)
            .done(appendEditors)
            .fail(function(jqxhr, settings, ex) {
                console.error('Could not retrieve the editor script', ex);
            });
    }

    /**
     * Registers the editor host as a {@code foundation-field} entity in the global Granite UI registry
     */
    function registerFieldAdapter() {
        $(window).adaptTo('foundation-registry').register('foundation.adapters', {
            type: 'foundation-field',
            selector: EDITOR_SELECTOR,
            adapter: function(el) {
                const input = $(el).find('input')[0];
                return {
                    getName: function () {
                        return input.name;
                    },
                    setName: function (name) {
                        input.name = name;
                    },
                    isDisabled: function () {
                        return el.disabled;
                    },
                    setDisabled: function (disabled) {
                        el.disabled = disabled;
                        el.editor && el.editor.setReadOnly(disabled || this.isReadOnly());
                    },
                    isInvalid: function () {
                        return $(el).attr('aria-invalid') === 'true';
                    },
                    setInvalid: function (invalid) {
                        $(el).attr('aria-invalid', invalid ? 'true' : 'false');
                        $(el).toggleClass('is-invalid', invalid);
                    },
                    isReadOnly: function () {
                        return $(el).attr('readonly');
                    },
                    setReadOnly: function (readOnly) {
                        $(el).attr('readonly', readOnly);
                        el.editor && el.editor.setReadOnly(readOnly || this.isDisabled());
                    },
                    isRequired: function() {
                        return $(el).attr('aria-required') === 'true';
                    },
                    setRequired: function (required) {
                        $(el).attr('aria-required', required ? 'true' : 'false');
                    },
                    getValue: function () {
                        return el.editor && el.editor.getValue();
                    },
                    setValue: function (value) {
                        el.editor && el.editor.setValue(value);
                    },
                    getLabelledBy: function () {
                        return $(el).attr('aria-labelledby');
                    },
                    setLabelledBy: function(labelledBy) {
                        $(el).attr('aria-labelledby', labelledBy);
                    },
                    getValues: function () {
                        return [ el.value ];
                    },
                    setValues: function (values) {
                        el.value = values[0];
                    },
                    clear: function() {
                        el.editor && el.editor.setValue('');
                    }
                };
            }
        });

        $(document).on("change", EDITOR_SELECTOR, function() {
            $(this).trigger("foundation-field-change");
        });
    }

    /**
     * Registers the editor host in the global Granite UI registry as subject to validation
     */
    function registerValidator() {
        const registry = $(window).adaptTo('foundation-registry');
        registry.register('foundation.validation.selector', {
            submittable: EDITOR_SELECTOR,
            candidate: EDITOR_SELECTOR + ':not([readonly]):not([disabled])',
            exclusion: EDITOR_SELECTOR + ' *'
        });
        registry.register("foundation.validation.validator", {
            selector: EDITOR_SELECTOR,
            validate: function(el) {
                const isRequired = $(el).attr('aria-required') === 'true';
                if (!isRequired) {
                    return;
                }
                return !el.editor || el.editor.getValue().length > 0 ? undefined : Granite.I18n.get(VALIDATION_MESSAGE);
            }
        });
    }

    /**
     * For all the editor hosts existing in the current dialog defines the {@code value} property. This one is used
     * in property accessors, such as {@code DependsOn}'s
     */
    function registerValueHook() {
        $(EDITOR_SELECTOR).each(function() {
            const editorHost = this;
            Object.defineProperty(this, 'value', {
                get() {
                    if (editorHost.editor) {
                        return editorHost.editor.getValue();
                    } else {
                        return editorHost.textContent;
                    }
                },
                set(value) {
                    if (editorHost.editor) {
                        editorHost.editor.setValue(value);
                    } else {
                        editorHost.textContent = value;
                    }
                },
                configurable: true
            });
        });
    }

    /**
     * For every editor hosts existing in the current dialog initializes an instance of Ace Editor and sets initial
     * options
     */
    function appendEditors() {
        $(EDITOR_SELECTOR).each(function() {
            const $this = $(this);
            const $input = $this.siblings('input');

            const editor = ace.edit(this);
            $this.data('theme') && editor.setTheme($this.data('theme'));
            $this.data('mode') && editor.session.setMode($this.data('mode'));

            const options = $this.data('options');
            if (options) {
                Object.keys(options).forEach(key => {
                    if (options[key] === 'Infinity') {
                        options[key] = Infinity;
                    }
                })
                editor.setOptions(options);
            }

            if ($(this).attr('disabled') || $(this).attr('readonly')) {
                editor.setReadOnly(true);
            }

            editor.on('change', function() {
                $input.val(($this.data('prefix') || '') + editor.getValue());
            });

            this.editor = editor;
        });
    }

})(window, document, Granite.$, Granite);
