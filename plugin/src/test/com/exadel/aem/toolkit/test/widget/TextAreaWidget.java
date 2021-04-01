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
package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextAreaResizeType;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
        path = DEFAULT_COMPONENT_NAME,
        title = "TextArea Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class TextAreaWidget {
    @DialogField(label = "Valid TextArea")
    @TextArea(
        value = "default value",
        emptyText = "empty text",
        autofocus = true,
        rows = 10,
        cols = 50,
        resize = TextAreaResizeType.BOTH
    )
    String text1;
}
