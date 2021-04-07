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
package com.exadel.aem.toolkit.test.dependson;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnTab;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
        path = DEFAULT_COMPONENT_NAME,
        title = "Tabs Test Dialog"
)
@Dialog
@Tabs({
    @Tab(title = "First tab"),
    @Tab(title = "Second tab")

})
@DependsOnTab(query = "test-query", tabTitle = "First tab")
@DependsOnTab(query = "test-query", tabTitle = "Second tab")
@SuppressWarnings("unused")
public class DependsOnTabAnnotation {
    @DialogField(label = "Field on the first tab")
    @TextField
    @Place("First tab")
    String field1;

    @DialogField(label = "Field on the second tab")
    @TextField
    @Place("Second tab")
    String field2;
}
