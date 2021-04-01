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
 * Contains common strings used to define {@code variant/statusVariant} property of a dialog widget when referring
 * status of an option or message
 * @deprecated These constants are deprecated in favour of {@link StatusVariant} enum and will be removed
 * in a version after 2.0.2
 */
@Deprecated
@SuppressWarnings({"unused", "squid:S1133"})
public class StatusVariantConstants {
    public static final String ERROR = "error";
    public static final String WARNING = "warning";
    public static final String SUCCESS = "success";
    public static final String HELP = "help";
    public static final String INFO = "info";

    private StatusVariantConstants() {
    }
}
