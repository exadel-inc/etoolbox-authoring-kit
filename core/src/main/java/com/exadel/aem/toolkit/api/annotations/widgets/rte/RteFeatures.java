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
package com.exadel.aem.toolkit.api.annotations.widgets.rte;

/**
 * Contains string constants representing built-in features of {@link RichTextEditor} Granite UI component
 */
@SuppressWarnings({"unused"})
public class RteFeatures {
    public static final String CONTROL_CLOSE = "control#close";
    public static final String CONTROL_SAVE = "control#save";

    public static final String EDIT_CUT = "edit#cut";
    public static final String EDIT_COPY = "edit#copy";
    public static final String EDIT_PASTE_DEFAULT = "edit#paste-default";
    public static final String EDIT_PASTE_PLAINTEXT = "edit#paste-plaintext";
    public static final String EDIT_PASTE_WORDHTML = "edit#paste-wordhtml";

    public static final String FINDREPLACE_FIND = "findreplace#find";
    public static final String FINDREPLACE_REPLACE = "findreplace#replace";

    public static final String FORMAT_BOLD = "format#bold";
    public static final String FORMAT_ITALIC = "format#italic";
    public static final String FORMAT_UNDERLINE = "format#underline";

    public static final String FULLSCREEN_TOGGLE = "fullscreen#toggle";

    public static final String IMAGE_IMAGEPROPS = "image#imageProps";

    public static final String JUSTIFY_LEFT = "justify#justifyleft";
    public static final String JUSTIFY_CENTER = "justify#justifycenter";
    public static final String JUSTIFY_RIGHT = "justify#justifyright";

    public static final String LINKS_MODIFYLINK = "links#modifylink";
    public static final String LINKS_ANCHOR = "links#anchor";
    public static final String LINKS_UNLINK = "links#unlink";

    public static final String LISTS_ORDERED = "lists#ordered";
    public static final String LISTS_UNORDERED = "lists#unordered";
    public static final String LISTS_INDENT = "lists#indent";
    public static final String LISTS_OUTDENT = "lists#outdent";

    public static final String MISCTOOLS_SPECIALCHARS = "misctools#specialchars";
    public static final String MISCTOOLS_SOURCEEDIT = "misctools#sourceedit";

    public static final String SEPARATOR = "-";

    public static final String SUBSUPERSCRIPT_SUBSCRIPT = "subsuperscript#subscript";
    public static final String SUBSUPERSCRIPT_SUPERSCRIPT = "subsuperscript#superscript";

    public static final String TABLE_TABLE = "table#table";

    public static final String SPELLCHECK_CHECKTEXT = "spellcheck#checktext";

    public static final String UNDO_UNDO = "undo#undo";
    public static final String UNDO_REDO = "undo#redo";

    public static final String BEGIN_POPOVER = "[";
    public static final String FEATURE_SEPARATOR = ",";
    public static final String END_POPOVER = "]";

    private RteFeatures() {}

    public static class Popovers {
        public static final String CONTROL_ALL = BEGIN_POPOVER +
                CONTROL_SAVE + FEATURE_SEPARATOR +
                CONTROL_CLOSE + END_POPOVER;
        public static final String EDIT_ALL = BEGIN_POPOVER +
                EDIT_CUT + FEATURE_SEPARATOR +
                EDIT_COPY + FEATURE_SEPARATOR +
                EDIT_PASTE_DEFAULT + FEATURE_SEPARATOR +
                EDIT_PASTE_PLAINTEXT + FEATURE_SEPARATOR +
                EDIT_PASTE_WORDHTML + END_POPOVER;
        public static final String EDIT_CUT_COPY_PASTEDEFAULT = BEGIN_POPOVER +
                EDIT_CUT + FEATURE_SEPARATOR +
                EDIT_COPY + FEATURE_SEPARATOR +
                EDIT_PASTE_DEFAULT + END_POPOVER;
        public static final String EDIT_CUT_COPY_PASTEPLAIN = BEGIN_POPOVER +
                EDIT_CUT + FEATURE_SEPARATOR +
                EDIT_COPY + FEATURE_SEPARATOR +
                EDIT_PASTE_PLAINTEXT + END_POPOVER;
        public static final String EDIT_CUT_COPY_PASTEWORDHTML = BEGIN_POPOVER +
                EDIT_CUT + FEATURE_SEPARATOR +
                EDIT_COPY + FEATURE_SEPARATOR +
                EDIT_PASTE_WORDHTML + END_POPOVER;
        public static final String FINDREPLACE_ALL = BEGIN_POPOVER +
                FINDREPLACE_FIND + FEATURE_SEPARATOR +
                FINDREPLACE_REPLACE + END_POPOVER;
        public static final String FORMAT_ALL = BEGIN_POPOVER +
                FORMAT_BOLD + FEATURE_SEPARATOR +
                FORMAT_ITALIC + FEATURE_SEPARATOR +
                FORMAT_UNDERLINE + END_POPOVER;
        public static final String JUSTIFY_ALL = BEGIN_POPOVER +
                JUSTIFY_LEFT + FEATURE_SEPARATOR +
                JUSTIFY_CENTER + FEATURE_SEPARATOR +
                JUSTIFY_RIGHT + END_POPOVER;
        public static final String LINKS_ALL = BEGIN_POPOVER +
                LINKS_MODIFYLINK + FEATURE_SEPARATOR +
                LINKS_ANCHOR + FEATURE_SEPARATOR +
                LINKS_UNLINK + END_POPOVER;
        public static final String LINKS_MODIFY_DELETE = BEGIN_POPOVER +
                LINKS_MODIFYLINK + FEATURE_SEPARATOR +
                LINKS_UNLINK + END_POPOVER;
        public static final String LISTS_ALL = BEGIN_POPOVER +
                LISTS_ORDERED + FEATURE_SEPARATOR +
                LISTS_UNORDERED + FEATURE_SEPARATOR +
                LISTS_INDENT + FEATURE_SEPARATOR +
                LISTS_OUTDENT + END_POPOVER;
        public static final String LISTS_ORDERED_UNORDERD = BEGIN_POPOVER +
                LISTS_ORDERED + FEATURE_SEPARATOR +
                LISTS_UNORDERED + END_POPOVER;
        public static final String MISCTOOLS_ALL = BEGIN_POPOVER +
                MISCTOOLS_SPECIALCHARS + FEATURE_SEPARATOR +
                MISCTOOLS_SOURCEEDIT + END_POPOVER;
        public static final String PARAFORMAT = BEGIN_POPOVER +
                "paraformat#paraformat:getFormats:paraformat-pulldown" + END_POPOVER;
        public static final String STYLES = BEGIN_POPOVER +
                "styles#styles:getStyles:styles-pulldown" + END_POPOVER;
        public static final String SUBSUPERSCRIPT_ALL = BEGIN_POPOVER +
                SUBSUPERSCRIPT_SUBSCRIPT + FEATURE_SEPARATOR +
                SUBSUPERSCRIPT_SUPERSCRIPT + END_POPOVER;
        private Popovers() {}
    }
    public static class Panels {
        public static class TablePanel {
            public static final String INSERTCOLUMN_BEFORE = "table#insertcolumn-before";
            public static final String INSERTCOLUMN_AFTER = "table#insertcolumn-after";
            public static final String REMOVECOLUMN = "table#removecolumn";
            public static final String INSERTROW_BEFORE = "table#insertrow-before";
            public static final String INSERTROW_AFTER = "table#insertrow-after";
            public static final String REMOVEROW = "table#removerow";
            public static final String MERGECELLS_RIGHT = "table#mergecells-right";
            public static final String MERGECELLS_DOWN = "table#mergecells-down";
            public static final String MERGECELLS = "table#mergecells";
            public static final String SPLITCELL_HORIZONTAL = "table#splitcell-horizontal";
            public static final String SPLITCELL_VERTICAL = "table#splitcell-vertical";
            public static final String MODIFYTABLEANDCELL = "table#modifytableandcell";
            public static final String SELECTROW = "table#selectrow";
            public static final String SELECTCOLUMN = "table#selectcolumn";
            public static final String ENSUREPARAGRAPH = "table#ensureparagraph";
            public static final String REMOVETABLE = "table#removetable";
            public static final String EXITTABLEEDITING = "table#exitTableEditing";
            private TablePanel() {}
        }
        public static final String TABLE = RteFeatures.BEGIN_POPOVER +
                TABLE_TABLE + FEATURE_SEPARATOR +
                TablePanel.MODIFYTABLEANDCELL + FEATURE_SEPARATOR +
                TablePanel.INSERTCOLUMN_BEFORE + FEATURE_SEPARATOR +
                TablePanel.INSERTCOLUMN_AFTER + FEATURE_SEPARATOR +
                TablePanel.REMOVECOLUMN + FEATURE_SEPARATOR +
                TablePanel.INSERTROW_BEFORE + FEATURE_SEPARATOR +
                TablePanel.INSERTROW_AFTER + FEATURE_SEPARATOR +
                TablePanel.REMOVEROW + FEATURE_SEPARATOR +
                TablePanel.MERGECELLS_RIGHT + FEATURE_SEPARATOR +
                TablePanel.MERGECELLS_DOWN + FEATURE_SEPARATOR +
                TablePanel.MERGECELLS + FEATURE_SEPARATOR +
                TablePanel.SPLITCELL_HORIZONTAL + FEATURE_SEPARATOR +
                TablePanel.SPLITCELL_VERTICAL + FEATURE_SEPARATOR +
                TablePanel.SELECTCOLUMN + FEATURE_SEPARATOR +
                TablePanel.SELECTROW + FEATURE_SEPARATOR +
                TablePanel.ENSUREPARAGRAPH + FEATURE_SEPARATOR +
                TablePanel.REMOVETABLE + FEATURE_SEPARATOR +
                TablePanel.EXITTABLEEDITING + END_POPOVER;
        private Panels() {}
    }
}
