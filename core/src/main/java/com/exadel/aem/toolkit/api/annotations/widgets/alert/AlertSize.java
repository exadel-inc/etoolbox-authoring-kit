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
package com.exadel.aem.toolkit.api.annotations.widgets.alert;

/**
 * Contains possible values of {@link Alert#size()} property
 * @deprecated This is deprecated in favour of {@link com.exadel.aem.toolkit.api.annotations.widgets.common.Size} enum
 * and will be removed in a version after 2.0.2
 */
@Deprecated
@SuppressWarnings("squid:S1133")
public enum AlertSize {
    SMALL {
        @Override
        public String toString() {
            return "S";
        }
    },
    LARGE {
        @Override
        public String toString() {
            return "L";
        }
    }
}
