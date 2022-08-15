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
import com.exadel.aem.toolkit.api.annotations.widgets.slider.Slider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Orientation;
import com.exadel.aem.toolkit.api.annotations.widgets.slider.SliderItem;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
    path = DEFAULT_COMPONENT_NAME,
    title = "Slider Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class SliderWidget {
    @DialogField(label = "Number")
    @Slider(ranged = true,
        items = {
            @SliderItem(text = "First", value = 1),
            @SliderItem(text = "Second", value = 2),
            @SliderItem(text = "Third", value = 3),
            @SliderItem(text = "Fourth", value = 4),
            @SliderItem(text = "Fifth", value = 5)
        },
        startValue = 4,
        endValue = 10,
        min = 10,
        max = 200,
        step = 2,
        orientation = Orientation.VERTICAL,
        filled = true,
        value = 14
    )
    String number;
}
