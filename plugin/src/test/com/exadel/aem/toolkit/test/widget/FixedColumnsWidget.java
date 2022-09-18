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

import static com.exadel.aem.toolkit.plugin.maven.TestConstants.DEFAULT_COMPONENT_NAME;

import com.exadel.aem.toolkit.api.annotations.layouts.Column;
import com.exadel.aem.toolkit.api.annotations.layouts.FixedColumns;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.NumberField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;

@AemComponent(
    path = DEFAULT_COMPONENT_NAME,
    title = "Fixed Columns Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class FixedColumnsWidget {

    @DialogField(label = "Number")
    @NumberField(min = -10, max = 10)
    @Place(value = "First", before = @ClassMember("text"))
    private String number;

    @FixedColumns(@Column(title = "Third"))
    @Place("Second")
    private byte nestedFixedColumns;

    @FixedColumns({
        @Column(title = "First"),
        @Column(title = "Second")
    })
    private FixedColumnsFieldset fixedColumns;

    @DialogField(
        label = "Text"
    )
    @TextField
    @Place("First")
    private String text;

    @DialogField(
        label = "Description"
    )
    @TextArea
    @Place("Second")
    private String description;

    @DialogField(
        label = "Extra-Nested Text"
    )
    @TextField
    @Place("Second/Third")
    private String extraNestedText;

    private static class FixedColumnsFieldset {
        @DialogField(
            label = "Nested text"
        )
        @TextField
        @Place("First")
        private String nestedText;

        @DialogField(
            label = "Nested description"
        )
        @TextArea
        @Place("Second")
        private String nestedDescription;
    }
}
