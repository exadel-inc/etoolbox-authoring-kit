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
import com.exadel.aem.toolkit.api.annotations.widgets.rte.AllowElement;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Characters;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.HtmlLinkRules;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.HtmlPasteRules;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.IconMapping;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.LinkTarget;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.ParagraphFormat;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.PasteMode;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Style;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
    path = DEFAULT_COMPONENT_NAME,
    title = "Rich Text Editor Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class RichTextEditorWidget {
    @DialogField(label = "Rich Text Editor")
    @RichTextEditor(
            features = {
                    RteFeatures.Popovers.CONTROL_ALL,
                    RteFeatures.UNDO_UNDO,
                    RteFeatures.UNDO_REDO,
                    RteFeatures.SEPARATOR,
                    RteFeatures.Popovers.EDIT_ALL,
                    RteFeatures.Popovers.FINDREPLACE_ALL,
                    RteFeatures.SEPARATOR,
                    RteFeatures.Popovers.FORMAT_ALL,
                    RteFeatures.Popovers.SUBSUPERSCRIPT_ALL,
                    RteFeatures.Popovers.STYLES,
                    RteFeatures.Popovers.PARAFORMAT,
                    RteFeatures.Popovers.JUSTIFY_ALL,
                    RteFeatures.Popovers.LISTS_ALL,
                    RteFeatures.Popovers.LINKS_MODIFY_DELETE,
                    RteFeatures.SEPARATOR,
                    RteFeatures.Panels.TABLE,
                    RteFeatures.SPELLCHECK_CHECKTEXT,
                    RteFeatures.Popovers.MISCTOOLS_ALL,
                    RteFeatures.FULLSCREEN_TOGGLE,
            },
            icons = {
                    @IconMapping(command = "#edit", icon = "copy"),
                    @IconMapping(command = "#findreplace", icon = "search"),
                    @IconMapping(command = "#links", icon = "link"),
                    @IconMapping(command = "#table", icon = "table"),
                    @IconMapping(command = "#subsuperscript", icon = "textSuperscript"),
                    @IconMapping(command = "#control", icon = "check"),
                    @IconMapping(command = "#misctools", icon = "fileCode")
            },
            specialCharacters = {
                    @Characters(name = "Complex entity", entity = "<input type=&quot;button&quot; value=&quot;OK&quot;/>"),
                    @Characters(name = "Copyright", entity = "&copy"),
                    @Characters(name = "Euro sign", entity = "&#x20AC"),
                    @Characters(name = "Registered", entity = "&#x00AE"),
                    @Characters(name = "Trademark", entity = "&#x2122"),
                    @Characters(rangeStart = 998, rangeEnd = 1020)
            },
            htmlPasteRules = @HtmlPasteRules(
                    allowBold = false,
                    allowItalic = false,
                    allowUnderline = false,
                    allowAnchors = false,
                    allowImages = false,
                    allowLists = AllowElement.ALLOW,
                    allowTables = AllowElement.REPLACE_WITH_PARAGRAPHS,
                    allowedBlockTags = "p",
                    fallbackBlockTag = "p"
            ),
            defaultPasteMode = PasteMode.WORDHTML,
            externalStyleSheets = {
                    "/etc/clientlibs/myLib/style1.css",
                    "/etc/clientlibs/myLib/style2.css"
            },
            styles = @Style(cssName = "italic", text = "Italic"),
            formats = {
                    @ParagraphFormat(tag="p", description = "Ordinary paragraph"),
                    @ParagraphFormat(tag="h3", description = "H3"),
                    @ParagraphFormat(tag = "h4", description = "H4 tagged paragraph")
            },
            tabSize = 8,
            indentSize = 1,
            maxUndoSteps = 25,
            htmlLinkRules = @HtmlLinkRules(
                    targetInternal = LinkTarget.MANUAL,
                    targetExternal = LinkTarget.BLANK,
                    protocols = {"http:", "https:"},
                    defaultProtocol = "http:"
            ),
            useFixedInlineToolbar = false
    )
    private String text;
}


