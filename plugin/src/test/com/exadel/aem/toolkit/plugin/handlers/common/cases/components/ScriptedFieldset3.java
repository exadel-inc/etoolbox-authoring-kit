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

import com.exadel.aem.toolkit.api.annotations.main.Setting;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

public class ScriptedFieldset3 {

    @DialogField
    @TextField(value = "${ @inheritedValue }")
    private String text;

    @FieldSet
    @Setting(name = "inheritedLabel", value = "Description")
    private NestedFieldset nestedFieldset;
}

class NestedFieldset {
    @DialogField(label = "Title")
    @TextField(
        value = "${ @inheritedValue }",
        emptyText = "${ @inheritedEmptyText }")
    private String nestedTitle;

    @DialogField(label = "@{ @inheritedLabel }")
    @TextField(
        value = "${ @inheritedDescription }",
        emptyText = "${ @inheritedEmptyText }")
    private String nestedDescription;
}
