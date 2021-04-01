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
import com.exadel.aem.toolkit.api.annotations.main.CommonProperty;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorField;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorVariant;
import com.exadel.aem.toolkit.api.annotations.widgets.color.GenerateColorsState;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

@AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
)
@Dialog
@HtmlTag(
        className = "wrapper",
        tagName = "span"
)
@CommonProperty(name = "test", value = "test")
@CommonProperty(name = "test", value = "test", scope = Scopes.CQ_HTML_TAG)
@CommonProperty(name = "test", value = "test", scope = Scopes.CQ_DIALOG)
@SuppressWarnings("unused")
public class ColorFieldWidget {

    @DialogField(validation = "foundation.jcr.name")
    @ColorField(
            value = "#4488CC",
            emptyText = "test-string",
            variant = ColorVariant.SWATCH,
            autogenerateColors = GenerateColorsState.SHADES,
            showSwatches = false,
            showDefaultColors = false,
            showProperties = false,
            customColors = {"#FF0000", "#00FF00", "#0000FF"}
    )
    String color;
}
