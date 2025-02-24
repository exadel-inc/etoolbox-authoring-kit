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
package com.exadel.aem.toolkit.api.annotations.widgets.rte;

/**
 * Enumerates values of {@link HtmlLinkRules#targetExternal()} and {@link HtmlLinkRules#targetInternal()} properties.
 * These properties define the value of `target` attribute for an anchor tag
 */
public class LinkTarget {

    public static final String AUTO = "";

    public static final String BLANK = "_blank";

    @Deprecated
    public static final String MANUAL = "manual";

    public static final String PARENT = "_parent";

    public static final String SELF = AUTO;

    public static final String TOP = "_top";

    /**
     * Default (instantiation-preventing) constructor
     */
    private LinkTarget() {
    }
}
