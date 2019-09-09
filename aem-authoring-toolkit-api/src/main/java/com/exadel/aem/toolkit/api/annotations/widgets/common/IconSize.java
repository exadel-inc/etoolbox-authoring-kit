/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import com.exadel.aem.toolkit.api.annotations.widgets.fileupload.FileUpload;

/**
 * Contains possible values of {@link FileUpload#iconSize()} property and similarly formatted properties of other TouchUI dialog components
 */
@SuppressWarnings("unused")
public enum IconSize {
    EXTRA_SMALL {
        @Override
        public String toString() {
            return "XS";
        }
    },
    SMALL {
        @Override
        public String toString() {
            return "S";
        }
    },
    MEDIUM {
        @Override
        public String toString() {
            return "M";
        }
    },
    LARGE {
        @Override
        public String toString() {
            return "L";
        }
    }
}
