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
import com.exadel.aem.toolkit.api.annotations.widgets.codeeditor.CodeEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.codeeditor.CodeEditorOption;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

@AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
)
@Dialog(extraClientlibs = {"eak.library.1", "eak.library.2"})
@SuppressWarnings("unused")
public class CodeEditorWidget {

    @DialogField
    @CodeEditor(
        source = "https://cdnjs.cloudflare.com/ajax/libs/ace/1.10.0/ace.js",
        mode = "js",
        theme = "crimson_editor",
        options = {
            @CodeEditorOption(name = "wrap", value = "true", type = boolean.class),
            @CodeEditorOption(name = "maxLines", value = "Infinity"),
        },
        dataPrefix = "js:"
    )
    String code;
}
