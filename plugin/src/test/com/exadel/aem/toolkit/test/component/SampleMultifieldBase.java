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

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Properties;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;

@SuppressWarnings("unused")
class SampleMultifieldBase {
    @DialogField(
            name = "Label's name",
            label = "Label",
            description = "description",
            required = true
    )
    @TextField
    @Place("Main tab")
    private String label;

    @DialogField(
            name = "FieldName",
            label = "Field label",
            description = "Field's description",
            required = true
    )
    @PathField(
            rootPath = "root/path"
    )
    @Place("Main tab")
    private String url;

    @DialogField(
            name = "CheckboxName",
            label = "Checkbox label",
            description = "Checkbox description"
    )
    @Checkbox
    @Place("Main tab")
    private boolean checkbox;

    @DialogField(
            label = "Icon label",
            description = "Icon description",
            required = true
    )
    @PathField(rootPath = "icons/folder/path")
    @Properties(@Property(name = "attribute", value = "attribute_value"))
    private String iconName;
}
