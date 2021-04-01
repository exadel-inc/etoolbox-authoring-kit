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

import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePicker;

/**
 * Contains possible values of {@link DatePicker#typeHint()} property and similar properties of other Granite UI components
 */
public enum TypeHint {
    NONE,
    BOOLEAN {
        @Override
        public String toString() {
            return "Boolean";
        }
    },
    BOOLEAN_ARRAY {
        @Override
        public String toString() {
            return "Boolean[]";
        }
    },
    DATE {
        @Override
        public String toString() {
            return "Date";
        }
    },
    DATE_ARRAY {
        @Override
        public String toString() {
            return "Date[]";
        }
    },
    DECIMAL {
        @Override
        public String toString() {
            return "Decimal";
        }
    },
    DECIMAL_ARRAY {
        @Override
        public String toString() {
            return "Decimal[]";
        }
    },
    DOUBLE {
        @Override
        public String toString() {
            return "Double";
        }
    },
    DOUBLE_ARRAY {
        @Override
        public String toString() {
            return "Double[]";
        }
    },
    LONG {
        @Override
        public String toString() {
            return "Long";
        }
    },
    LONG_ARRAY {
        @Override
        public String toString() {
            return "Long[]";
        }
    },
    NAME {
        @Override
        public String toString() {
            return "Name";
        }
    },
    NAME_ARRAY {
        @Override
        public String toString() {
            return "Name[]";
        }
    },
    PATH {
        @Override
        public String toString() {
            return "Path";
        }
    },
    PATH_ARRAY {
        @Override
        public String toString() {
            return "Path[]";
        }
    },
    STRING {
        @Override
        public String toString() {
            return "String";
        }
    },
    STRING_ARRAY {
        @Override
        public String toString() {
            return "String[]";
        }
    },
    URI {
        @Override
        public String toString() {
            return "URI";
        }
    },
    URI_ARRAY {
        @Override
        public String toString() {
            return "URI[]";
        }
    }
}
