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

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.anchorbutton.AnchorButton;
import com.exadel.aem.toolkit.api.annotations.widgets.common.LinkCheckerVariant;
import com.exadel.aem.toolkit.plugin.util.TestConstants;

@Dialog(
    name = TestConstants.DEFAULT_COMPONENT_NAME,
    title = TestConstants.DEFAULT_COMPONENT_TITLE,
    layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class AnchorButtonWidget {

    @AnchorButton(
        href = "http://localhost:4502/content/page.html",
        text = "Link Text",
        linkChecker = LinkCheckerVariant.SKIP,
        icon = "search")
    String field;
}
