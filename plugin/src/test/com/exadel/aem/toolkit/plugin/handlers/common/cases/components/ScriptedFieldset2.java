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

import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;

@Data(name = "inheritedLabel", value = "Extension text", persist = false)
@Ignore(members = {
    @ClassMember(value = "heading", source = ScriptedFieldset1.class),
    @ClassMember(value = "getHeading", source = ScriptedFieldset1.class)
})
public class ScriptedFieldset2 extends ScriptedFieldset1 {

    @DialogField(
        label = "Field @{source.name}",
        description = "In class @{source.class.name}"
    )
    @TextField(
        value = "Imported @{source.annotation('@DialogField').label()}",
        emptyText = "@{source.annotations('Data')[0].value()}"
    )
    @Data(name = "emptyText", value = "Hello World")
    private String text;

    @DialogField(
        // First, the value will be set to "@{data.inheritedLabel}" as it is pulled from the superclass
        // Second, the "data.inheritedLabel" will be mapped to the value of the @Data annotation above
        label = "@{source.class.parent.member('text').annotation('DialogField').label()}",
        description = "@{source.class.ancestors().includes('ScriptedFieldsetInterface') " +
            "? 'Has parent interface' : 'Does not have a parent interface'}"
    )
    @TextField(value = "@{source.context.class().annotation('AemComponent').path()}")
    private String extensionText;

    @DialogField(condition = "@{data.renderExtraText}")
    @TextField
    private String extraText;
}

