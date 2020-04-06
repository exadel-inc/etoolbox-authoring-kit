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

import com.exadel.aem.toolkit.api.annotations.main.CommonProperties;
import com.exadel.aem.toolkit.api.annotations.main.CommonProperty;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorField;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorValue;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorVariant;
import com.exadel.aem.toolkit.api.annotations.widgets.color.GenerateColorsState;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@HtmlTag(
        className = "wrapper",
        tagName = "span"
)
@CommonProperties(value = {
        @CommonProperty(name = "test", value = "test", scope = XmlScope.CQ_HTML_TAG)
})
@SuppressWarnings("unused")
public class ColorFieldWidget {

    @ColorField(
            value = ColorValue.HEX,
            emptyText = "test-string",
            variant = ColorVariant.SWATCH,
            autogenerateColors = GenerateColorsState.SHADES,
            showSwatches = false,
            showDefaultColors = false,
            validation = "foundation.jcr.name"
    )
    String color;
}
