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
package com.exadel.aem.toolkit.plugin.utils;

/**
 * Contains common string constants used for AEM Granite UI component and dialog rendering within the ToolKit Maven plugin
 * workflow
 * @see com.exadel.aem.toolkit.core.CoreConstants
 * @see <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/user-guide.html?topic=/experience-manager/6-5/sites/developing/morehelp/components.ug.js">Adobe documentation</a>
 */
public class DialogConstants {
    public static final String DOUBLE_QUOTE = "\"";
    public static final String NEGATION = "!";
    public static final String WILDCARD = "*";

    public static final String SEPARATOR_DOT = ".";
    public static final String SEPARATOR_SEMICOLON = ";";

    public static final String RELATIVE_PATH_PREFIX = "./";
    public static final String PARENT_PATH_PREFIX = "../";
    public static final String PARENT_PATH_INDICATOR = "..";

    public static final String NN_ACCORDION = "accordion";
    public static final String NN_ALLOW_BASICS = "allowBasics";
    public static final String NN_CHARS = "chars";
    public static final String NN_CHILD_EDITORS = "cq:childEditors";
    public static final String NN_COLUMN = "column";
    public static final String NN_CONFIG = "config";
    public static final String NN_CONTENT = "content";
    public static final String NN_CUI = "cui";
    public static final String NN_DATASOURCE = "datasource";
    public static final String NN_DIALOG_FULL_SCREEN = "dialogFullScreen";
    public static final String NN_DROP_TARGETS = "cq:dropTargets";
    public static final String NN_EDIT = "edit";
    public static final String NN_FORM_PARAMETERS = "cq:formParameters";
    public static final String NN_GRANITE_DATA = "granite:data";
    public static final String NN_HTML_PASTE_RULES = "htmlPasteRules";
    public static final String NN_HTML_RULES = "htmlRules";
    public static final String NN_ICONS = "icons";
    public static final String NN_INLINE = "inline";
    public static final String NN_INPLACE_EDITING = "cq:inplaceEditing";
    public static final String NN_ITEM = "item";
    public static final String NN_ITEMS = "items";
    public static final String NN_FIELD = "field";
    public static final String NN_FORMATS = "formats";
    public static final String NN_FULLSCREEN = "fullscreen";
    public static final String NN_KEYS = "keys";
    public static final String NN_LAYOUT = "layout";
    public static final String NN_LAYOUT_CONFIG = "layoutConfig";
    public static final String NN_LINKS = "links";
    public static final String NN_LIST = "list";
    public static final String NN_LISTENERS = "cq:listeners";
    public static final String NN_LISTS = "lists";
    public static final String NN_MISCTOOLS = "misctools";
    public static final String NN_OPTIONS = "options";
    public static final String NN_PARAFORMAT = "paraformat";
    public static final String NN_PARENT_CONFIG = "parentConfig";
    public static final String NN_POPOVERS = "popovers";
    public static final String NN_ROOT = "jcr:root";
    public static final String NN_RTE_PLUGINS = "rtePlugins";
    public static final String NN_SPECIAL_CHARS_CONFIG = "specialCharsConfig";
    public static final String NN_STYLES = "styles";
    public static final String NN_SUBLIST = "sublist";
    public static final String NN_TABS = "tabs";
    public static final String NN_TAB = "tab";
    public static final String NN_TABLE = "table";
    public static final String NN_TABLE_EDIT_OPTIONS = "tableEditOptions";
    public static final String NN_TARGET_CONFIG = "targetConfig";
    public static final String NN_UI_SETTINGS = "uiSettings";
    public static final String NN_UNDO = "undo";
    public static final String NN_VALUES = "values";

    public static final String NT_CHILD_EDITORS_CONFIG = "cq:ChildEditorConfig";
    public static final String NT_COMPONENT = "cq:Component";
    public static final String NT_DROP_TARGET_CONFIG = "cq:DropTargetConfig";
    public static final String NT_EDIT_CONFIG = "cq:EditConfig";
    public static final String NT_INPLACE_EDITING_CONFIG = "cq:InplaceEditingConfig";
    public static final String NT_LISTENERS = "cq:EditListenersConfig";
    public static final String NT_UNSTRUCTURED = "nt:unstructured";
    public static final String NT_WIDGET_COLLECTION = "cq:WidgetCollection";

    public static final String PN_ACCEPT = "accept";
    public static final String PN_ACTIVE = "active";
    public static final String PN_ALLOW = "allow";
    public static final String PN_ALLOW_BLOCK_TAGS = "allowBlockTags";
    public static final String PN_ATTRIBUTE_MEMBERS = "attributeMembers";
    public static final String PN_ATTRIBUTES = "attributes";
    public static final String PN_COMPOSITE = "composite";
    public static final String PN_CSS_EXTERNAL = "cssExternal";
    public static final String PN_CSS_INTERNAL = "cssInternal";
    public static final String PN_DEFAULT_PASTE_MODE = "defaultPasteMode";
    public static final String PN_DEFAULT_PROTOCOL = "defaultProtocol";
    public static final String PN_DEPENDS_ON = "dependsOn";
    public static final String PN_DEPENDS_ON_ACTION = "dependsOnAction";
    public static final String PN_DEPENDS_ON_REF = "dependsOnRef";
    public static final String PN_DEPENDS_ON_REFTYPE = "dependsOnRefType";
    public static final String PN_DEPENDS_ON_REFLAZY = "dependsOnRefLazy";
    public static final String PN_DISABLED = "disabled";
    public static final String PN_DISCONNECTED = "disconnected";
    public static final String PN_EDIT_ELEMENT_QUERY = "editElementQuery";
    public static final String PN_EDITOR_TYPE = "editorType";
    public static final String PN_EXTERNAL_STYLESHEETS = "externalStyleSheets";
    public static final String PN_FALLBACK_BLOCK_TAG = "fallbackBlockTag";
    public static final String PN_FALLBACK_PATH = "fallbackPath";
    public static final String PN_FEATURES = "features";
    public static final String PN_FILE_REFERENCE_PARAMETER = "fileReferenceParameter";
    public static final String PN_GROUPS = "groups";
    public static final String PN_IGNORE_MODE = "ignoreMode";
    public static final String PN_INDENT_SIZE = "indentSize";
    public static final String PN_JCR_TITLE = "jcr:title";
    public static final String PN_MAX_DATE = "maxDate";
    public static final String PN_MAX_UNDO_STEPS = "maxUndoSteps";
    public static final String PN_MIN_DATE = "minDate";
    public static final String PN_MODE = "mode";
    public static final String PN_NAME = "name";
    public static final String PN_PADDING = "padding";
    public static final String PN_PRIMARY_TYPE = "jcr:primaryType";
    public static final String PN_PROPERTY_NAME = "propertyName";
    public static final String PN_PROTOCOLS = "protocols";
    public static final String PN_REF = "ref";
    public static final String PN_RENDER_HIDDEN = "renderHidden";
    public static final String PN_REQUIRED = "required";
    public static final String PN_RETYPE = "retype";
    public static final String PN_SLING_RESOURCE_TYPE = "sling:resourceType";
    public static final String PN_TAB_SIZE = "tabSize";
    public static final String PN_TARGET_EXTERNAL = "targetExternal";
    public static final String PN_TARGET_INTERNAL = "targetInternal";
    public static final String PN_TEXT_MEMBER = "textMember";
    public static final String PN_TEXT_PROPERTY_NAME = "textPropertyName";
    public static final String PN_TEXT_TRANSFORM = "textTransform";
    public static final String PN_TITLE = "title";
    public static final String PN_TOOLBAR = "toolbar";
    public static final String PN_TRACKING_ELEMENT = "trackingElement";
    public static final String PN_TYPE = "type";
    public static final String PN_TYPE_HINT = "typeHint";
    public static final String PN_USE_FIXED_INLINE_TOOLBAR = "useFixedInlineToolbar";
    public static final String PN_VALUE_MEMBER = "valueMember";
    public static final String PN_VALUE_TRANSFORM = "valueTransform";

    public static final String VALUE_ALL = "all";
    public static final String VALUE_NONE = "none";

    private DialogConstants() {}
}
