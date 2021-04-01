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
package com.exadel.aem.toolkit.test.component;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

@SuppressWarnings("unused")
class SampleFieldsetBase3 {
    private static final String FIELD =  "Content";
    private static final String LABEL = "Content label";
    private static final String DESCRIPTION = "Content description";

    @DialogField(
            name = FIELD,
            label = LABEL,
            description = DESCRIPTION
    )
    @TextField
    private String content;

    @SuppressWarnings("unused")
    private SampleInnerClass innerClass;

    private static class SampleInnerClass {
        @DialogField(
                name = "Inner class",
                label = "Inner class label",
                description = "Inner class description"
        )
        @TextField
        private String innerClassField;
    }
}
