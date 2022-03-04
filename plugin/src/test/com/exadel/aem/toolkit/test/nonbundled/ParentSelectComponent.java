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
package com.exadel.aem.toolkit.test.nonbundled;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;

@Dialog(
        name = "content/parent-select-component",
        title = "Parent Select Component",
        tabs = {
                @Tab(title = ParentSelectComponent.TAB_MAIN)
        }
)
public class ParentSelectComponent {

    static final String TAB_MAIN = "Main";

    private static final String LABEL_DUNGEONS_SELECT = "Dungeons select";

    private static final String DEFAULT_SELECT_TEXT = "nothing is selected";

    @DialogField(label = LABEL_DUNGEONS_SELECT)
    @Select(options = {
            @Option(text = "A", value = "a"),
            @Option(text = "B", value = "b")
    })
    private String dungeon;

    public String getParentSelect() {
        return StringUtils.defaultIfBlank(dungeon, DEFAULT_SELECT_TEXT);
    }
}
