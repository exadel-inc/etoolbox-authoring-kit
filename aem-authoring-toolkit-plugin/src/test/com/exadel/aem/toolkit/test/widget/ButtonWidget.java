/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.button.Button;
import com.exadel.aem.toolkit.api.annotations.widgets.button.ButtonType;
import com.exadel.aem.toolkit.api.annotations.widgets.button.ButtonVariant;
import com.exadel.aem.toolkit.api.annotations.widgets.fileupload.ButtonSize;
import com.exadel.aem.toolkit.api.annotations.widgets.fileupload.IconSize;

import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_NAME;

@Dialog(
        name = DEFAULT_COMPONENT_NAME,
        title = "Button Widget Dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class ButtonWidget {

    @Button(
            buttonType = ButtonType.SUBMIT,
            autocomplete = "on",
            formId = "test-form",
            buttonText = "test-text",
            textComment = "test-comment",
            hideButtonText = true,
            active = true,
            icon = "search",
            iconSize = IconSize.LARGE,
            buttonSize = ButtonSize.LARGE,
            block = true,
            command = "shift+s",
            trackingElement = "test-element",
            trackingFeature = "test-feature"
    )
    String buttonField1;

    @Button(
            buttonType = ButtonType.RESET,
            buttonText = "test-text",
            icon = "delete",
            buttonVariant = ButtonVariant.PRIMARY,
            command = "backspace"
    )
    String buttonField2;
}
