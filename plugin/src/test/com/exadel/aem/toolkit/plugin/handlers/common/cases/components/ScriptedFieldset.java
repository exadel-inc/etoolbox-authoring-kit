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
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;

@Data(name = "key", value = "scripted{@value}")
@SuppressWarnings("unused")
public class ScriptedFieldset {

    @Heading("@{data.hello || 'Hello world'}")
    private String text;

    @DialogField(label = "Foo@{data.bar}")
    @Property(name = "@{data.key}/subnode_1", value = "value")
    @Property(name = "@{data.key}/subnode_2", value = "value@{data.index}2")
    public String getText() {
        return text;
    }
}
