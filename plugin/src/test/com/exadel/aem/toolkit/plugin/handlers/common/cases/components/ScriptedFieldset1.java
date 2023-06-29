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

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Heading;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;

@Data(name = "key", value = "scripted{@value}")
@Data(name = "inheritedValue", value = "From ScriptedFieldset", persist = false)
@Data(name = "inheritedAutocomplete", value = "From ScriptedFieldset", persist = false)
@SuppressWarnings("unused")
public class ScriptedFieldset1 implements ScriptedFieldsetInterface {

    @Heading("@{data.greeting || 'Hello world'}")
    private String heading;

    @DialogField(label = "@{data.inheritedLabel} field")
    @TextField
    @Property(name = "@{data.key1}/subnode_1", value = "value")
    @Property(name = "@{data.key2}/subnode_2", value = "value@{data.index}2")
    @Data(name = "inheritedLabel", value = "Heading", persist = false)
    private String getHeading() {
        return heading;
    }

    @DialogField(
        label = "@{data.inheritedLabel}",
        description = "@{data.inheritedDescription}"
    )
    @TextField(
        value = "@{data.inheritedValue}",
        emptyText = "@{data.inheritedEmptyText}",
        autocomplete = "@{data.inheritedAutocomplete}")
    @Data(name = "inheritedAutocomplete", value = "From ScriptedFieldset#text", persist = false)
    private String text;
}

@Data(name = "inheritedEmptyText", value = "From ScriptedFieldsetParentInterface", persist = false)
interface ScriptedFieldsetParentInterface {
}
@Data(name = "inheritedEmptyText", value = "From ScriptedFieldsetInterface", persist = false)
interface ScriptedFieldsetInterface extends ScriptedFieldsetParentInterface {
}
