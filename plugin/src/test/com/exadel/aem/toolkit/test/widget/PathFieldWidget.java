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
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.annotations.widgets.common.NodeFilter;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

@AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = "PathField Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class PathFieldWidget {

    @DialogField(label = "Edit path")
    @PathField(
        deleteHint = false,
        emptyText = "Path goes here",
        droppable = true,
        filter = NodeFilter.NOSYSTEM,
        multiple = true,
        rootPath = "/content",
        suggestionSrc = "suggestion"
    )
    String path;

    @DialogField(label = "Edit path 2")
    @PathField(
        emptyText = "Path goes here",
        rootPath = "/content/dam",
        forceSelection = true,
        pickerSrc = "picker"
    )
    String path2;
}
