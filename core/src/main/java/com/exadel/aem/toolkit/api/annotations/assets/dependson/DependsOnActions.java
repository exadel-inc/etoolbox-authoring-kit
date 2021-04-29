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
package com.exadel.aem.toolkit.api.annotations.assets.dependson;

/**
 * Presents the set of pre-defined action values for {@link DependsOn} and {@link DependsOnTab} annotations
 */
@SuppressWarnings("unused")
public class DependsOnActions {

    /**
     * Hides the element if the query result is 'falsy'
     */
    public static final String VISIBILITY = "visibility";

    /**
     * Hides the tab (if applied to the tab) or element's parent tab (if applied to the element)
     * if the query result is 'falsy'
     */
    public static final String TAB_VISIBILITY = "tab-visibility";

    /**
     * Sets the validation state of the field based on the query result
     */
    public static final String VALIDATE = "validate";

    /**
     * Sets the {@code required} marker of the field based on the query result
     */
    public static final String REQUIRED = "required";

    /**
     * Sets the {@code readonly} marker of the field based on the query result
     */
    public static final String READONLY = "readonly";

    /**
     * Sets the field's disabled state based on the query result
     */
    public static final String DISABLED = "disabled";

    /**
     * Sets the query result as field's value (undefined query result is skipped)
     */
    public static final String SET = "set";

    /**
     * Sets the query result as field's value only if the current value is blank (undefined query result is skipped)
     */
    public static final String SET_IF_BLANK = "set-if-blank";

    /**
     * Asynchronous action to set value from request result
     */
    public static final String FETCH = "fetch";

    /**
     * Refreshes the options set of a Coral3 Select widget with an OptionProvider
     */
    public static final String UPDATE_OPTIONS = "update-options";

    private DependsOnActions() {}
}
