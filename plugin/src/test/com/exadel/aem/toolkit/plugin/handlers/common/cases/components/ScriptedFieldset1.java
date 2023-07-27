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
import com.exadel.aem.toolkit.api.annotations.widgets.Heading;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;

@Setting(name = "key", value = "scripted{@value}")
@Setting(name = "inheritedValue", value = "From ScriptedFieldset")
@Setting(name = "inheritedAutocomplete", value = "From ScriptedFieldset")
@SuppressWarnings("unused")
public class ScriptedFieldset1 implements ScriptedFieldsetInterface {

    @Heading("${@greeting || 'Hello world'}")
    private String heading;

    @DialogField(label = "${@inheritedLabel} field")
    @TextField
    @Property(name = "${@key1}/subnode_1", value = "value")
    @Property(name = "${@key2}/subnode_2", value = "value${@index}2")
    @Setting(name = "inheritedLabel", value = "Heading")
    private String getHeading() {
        return heading;
    }

    @DialogField(
        label = "${@inheritedLabel}",
        description = "${@inheritedDescription}"
    )
    @TextField(
        value = "${ @inheritedValue }",
        emptyText = "${ @inheritedEmptyText }",
        autocomplete = "${ @inheritedAutocomplete }")
    @Setting(name = "inheritedAutocomplete", value = "From ScriptedFieldset#text")
    private String text;
}

@Setting(name = "inheritedEmptyText", value = "From ScriptedFieldsetParentInterface")
interface ScriptedFieldsetParentInterface {
}
@Setting(name = "inheritedEmptyText", value = "From ScriptedFieldsetInterface")
interface ScriptedFieldsetInterface extends ScriptedFieldsetParentInterface {
}
