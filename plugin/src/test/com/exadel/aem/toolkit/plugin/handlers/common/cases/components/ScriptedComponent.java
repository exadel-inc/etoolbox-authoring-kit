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

import static com.exadel.aem.toolkit.plugin.maven.TestConstants.DEFAULT_COMPONENT_NAME;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionSource;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Characters;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.HtmlLinkRules;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.IconMapping;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.LinkTarget;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.PasteMode;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.plugin.annotations.cases.NestedAnnotations;

@AemComponent(
    path = DEFAULT_COMPONENT_NAME,
    title = "Scripted Component"
)
@Dialog(title = "Scripted Component Dialog")
@Ignore(members = @ClassMember("moreOptions"))
public class ScriptedComponent {
    @DialogField(label = "@{textFieldTitle || 'Default title'}")
    @RichTextEditor(
            features = {
                    RteFeatures.Popovers.CONTROL_ALL,
                    "@{!rteFeatures || rteFeatures.includes('undo') ? 'undo#undo' : ''}",
                    "@{rteFeatures.contains('redo') ? 'undo#redo' : ''}",
                    RteFeatures.SEPARATOR,
                    RteFeatures.Popovers.EDIT_ALL,
                    RteFeatures.Popovers.FINDREPLACE_ALL,
            },
            icons = {
                    @IconMapping(command = "#edit", icon = "copy"),
                    @IconMapping(command = "#findreplace", icon = "search"),
                    @IconMapping(command = "#links", icon = "link"),
            },
            specialCharacters = {
                    @Characters(name = "Copyright", entity = "&copy"),
                    @Characters(name = "@{'Eu' + 'ro'} sign", entity = "&#x20AC"),
            },
            defaultPasteMode = PasteMode.WORDHTML,
            externalStyleSheets = {
                    "@{styleAddress}",
                    "/etc/clientlibs/myLib/@{styleName}.css"
            },
            maxUndoSteps = 25,
            htmlLinkRules = @HtmlLinkRules(
                    targetInternal = LinkTarget.MANUAL,
                    targetExternal = LinkTarget.BLANK,
                    protocols = {"http:", "https:"},
                    defaultProtocol = "http:"
            ),
            useFixedInlineToolbar = false
    )
    @Attribute(data = {
        @Data(name = "rteFeatures", value = "[undo]")
    })
    private String text;

    @Select(
        options = {
            @Option(text = "First", value = "1"),
            @Option(text = "Second", value = "2")
        }
    )
    @Property(name = "items/item@{selectedOption}/selected", value = "true")
    @Attribute(data = {
        @Data(name = "selectedOption", value = "2")
    })
    private String options;

    @Select(
        optionProvider = @OptionProvider(
            value = {
                @OptionSource(value = "source1", attributes = {"source1_1", "source1_2"}),
                @OptionSource(value = "source2", attributes = {"source2_1", "source2_2"}),
                @OptionSource(value = "source3", attributes = {"source3_1", "source3_2"})
            },
            prepend = {"prepend1_1", "prepend1_2"},
            append = {"append1_1", "append1_2"}
        )
    )
    private String moreOptions;

    @NestedAnnotations.Level0({
        @NestedAnnotations.Level1({
            @NestedAnnotations.Level2(numbers = {0, 1, 2}),
            @NestedAnnotations.Level2(numbers = {3, 4, 5}),
        }),
        @NestedAnnotations.Level1({
            @NestedAnnotations.Level2(numbers = {6, 7, 8}),
            @NestedAnnotations.Level2(numbers = {9, 10, 11}),
        }),
        @NestedAnnotations.Level1({
            @NestedAnnotations.Level2(numbers = {12, 13, 14}),
        })
    })
    private int numbers;
}


