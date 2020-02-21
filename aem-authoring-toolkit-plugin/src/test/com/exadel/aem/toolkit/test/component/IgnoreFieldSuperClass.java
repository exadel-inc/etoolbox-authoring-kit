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

package com.exadel.aem.toolkit.test.component;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.ClassField;
import com.exadel.aem.toolkit.api.annotations.widgets.Ignore;
import com.exadel.aem.toolkit.test.widget.Tabs;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.TABS,
        tabs = {
                @Tab(title = "First tab"),
        }
)
@Ignore(fields = {
        @ClassField(value = Tabs.class, field = "field2")
})
@SuppressWarnings("unused")
public class IgnoreFieldSuperClass extends IgnoreTabsWidgetField {

}
