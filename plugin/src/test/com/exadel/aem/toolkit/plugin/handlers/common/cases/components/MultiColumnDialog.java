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
package com.exadel.aem.toolkit.plugin.handlers.common.cases.components;

import com.exadel.aem.toolkit.api.annotations.layouts.Column;
import com.exadel.aem.toolkit.api.annotations.layouts.FixedColumns;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.NumberField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

@AemComponent(
    path = TestConstants.DEFAULT_COMPONENT_NAME,
    title = "Multi-Column Dialog Component"
)
@Dialog
@FixedColumns(
    value = {
        @Column(title = "First column"),
        @Column(title = "Second column")
    },
    margin = true, // These two properties will not be rendered because apply only to widgets
    maximized = true
)
@Ignore(sections = "Default")
@SuppressWarnings("unused")
public class MultiColumnDialog extends MultiColumnDialogBase {

    @DialogField(label = "Text")
    @TextField
    @Place("First column")
    private String text;

    @DialogField(label = "Description")
    @TextArea
    @Place("Second column")
    private String description;

    @DialogField(label = "Number")
    @NumberField(min = -10, max = 10)
    @Place(value = "First column", before = @ClassMember("text"))
    private String number;
}
