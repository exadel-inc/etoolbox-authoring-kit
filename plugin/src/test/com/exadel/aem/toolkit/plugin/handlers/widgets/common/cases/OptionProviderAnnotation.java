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
package com.exadel.aem.toolkit.plugin.handlers.widgets.common.cases;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.buttongroup.ButtonGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionSource;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

@SuppressWarnings("unused")
@AemComponent(
    path = TestConstants.DEFAULT_COMPONENT_NAME,
    title = TestConstants.DEFAULT_COMPONENT_TITLE
)
@Dialog
public class OptionProviderAnnotation {

    @DialogField(label = "Enum based options list")
    @ButtonGroup(
        itemProvider = @OptionProvider(
            value = {
                @OptionSource(
                    enumeration = Colors.class,
                    textMember = "name", // This value can be skipped because is equal to the default
                    valueMember = "toString", // This value can be skipped because is equal to the default
                    attributeMembers = "getValue"
                ),
                @OptionSource(
                    enumeration = Colors.class,
                    valueMember = "getValue",
                    fallback = "/content/lists/fallback-colors"
                ),
                @OptionSource(enumeration = java.nio.file.AccessMode.class),
                @OptionSource(
                    value = "/content/lists/additional-colors",
                    attributeMembers = {"attr_1", "attr_2"})
            }
        )
    )
    @SuppressWarnings("deprecation")
    // The "fallback" property is deprecated and will be removed in a version after 2.3.0
    String buttons;

    @DialogField(label = "Constants based options list")
    @RadioGroup(buttonProvider = @OptionProvider(
        value = {
            @OptionSource(enumeration = ColorConstants.class),
            @OptionSource(enumeration = java.nio.charset.StandardCharsets.class),
            @OptionSource(
                enumeration = ColorConstants.class,
                textMember = "LABEL_*",
                valueMember = "VALUE_*"),
            @OptionSource(
                enumeration = ColorConstants.class,
                textMember = "BACKGROUND_LABEL_*",
                valueMember = "BACKGROUND_VALUE_*")
        }
    ))
    String radio;

    @DialogField(label = "HTTP-based options list")
    @Select(
        optionProvider = @OptionProvider(
            value = @OptionSource("http://localhost/colors.json/path"),
            append = "All:all",
            exclude = "red",
            prepend = "None:none",
            sorted = true
        )
    )
    String selectOptions;

    public enum Colors {
        RED(ColorConstants.VALUE_RED),
        ORANGE(null),
        YELLOW(null),
        GREEN(ColorConstants.VALUE_GREEN),
        BLUE(ColorConstants.VALUE_BLUE),
        INDIGO(null),
        VIOLET(null);

        private final String value;

        Colors(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public static class ColorConstants {
        public static final String LABEL_RED = "Red";
        public static final String VALUE_RED = "#ff0000";
        public static final String LABEL_GREEN = "Green";
        public static final String VALUE_GREEN = "#00ff00";
        public static final String LABEL_BLUE = "Blue";
        public static final String VALUE_BLUE = "#0000ff";
        public static final String LABEL_NONE = "None";
        public static final String BACKGROUND_LABEL_WHITE = "White";
        public static final String BACKGROUND_VALUE_WHITE = "#ffffff";
    }
}
