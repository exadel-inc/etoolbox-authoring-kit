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
package com.exadel.aem.toolkit.api.annotations.widgets.common;

/**
 * Contains common string constants used for size setup
 * @deprecated These constants are deprecated in favor of {@link Size} enum and will be removed in a version after 2.0.2
 */
@SuppressWarnings({"unused", "squid:S1133"})
@Deprecated
public class ElementSizeConstants {
    public static final String EXTRA_SMALL = "XS";
    public static final String SMALL = "S";
    public static final String MEDIUM = "M";
    public static final String LARGE = "L";

    private ElementSizeConstants() {
    }
}
