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


import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.core.util.TestConstants;
import com.exadel.aem.toolkit.test.component.SampleFieldsetBase2;

import static com.exadel.aem.toolkit.core.util.TestConstants.*;

@Dialog(
        name = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE,
        description = "test component",
        componentGroup = TestConstants.DEFAULT_COMPONENT_GROUP,
        tabs = {
                @Tab(title = LABEL_TAB_1),
                @Tab(title = LABEL_TAB_2)
        }
)
@SuppressWarnings("unused")
public class FieldSetWidget {

    private static final String PREFIX_FIRST_PRIMARY_DIALOG = "primary1";
    private static final String PREFIX_SECOND_PRIMARY_DIALOG = "primary2";

    @DialogField
    @FieldSet(namePrefix = PREFIX_FIRST_PRIMARY_DIALOG, unwrap = true)
    @PlaceOnTab(LABEL_TAB_1)
    private SampleFieldsetBase2 firstPrimaryDialog;

    @DialogField
    @FieldSet(namePrefix = PREFIX_SECOND_PRIMARY_DIALOG)
    @PlaceOnTab(LABEL_TAB_2)
    private SampleFieldsetBase2 secondPrimaryDialog;
}
