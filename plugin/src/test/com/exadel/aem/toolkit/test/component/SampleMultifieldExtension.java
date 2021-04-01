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
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;

@SuppressWarnings("unused")
class SampleMultifieldExtension extends SampleMultifieldBase {
    @DialogField(
            name = "additional label",
            label = "Additional info",
            description = "Sample multifield - additional info"
    )
    @TextField
    private String additionalLabel;

    @DialogField(
            name = "additional info",
            label = "Additional info",
            description = "Sample multifield - additional info"
    )
    @TextArea(rows = 15)
    private String additionalInfo;

}
