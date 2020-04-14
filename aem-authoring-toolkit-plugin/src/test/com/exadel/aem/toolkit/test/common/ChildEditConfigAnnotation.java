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

package com.exadel.aem.toolkit.test.common;

import com.exadel.aem.toolkit.api.annotations.editconfig.*;
import com.exadel.aem.toolkit.api.annotations.editconfig.listener.Listener;
import com.exadel.aem.toolkit.api.annotations.editconfig.listener.ListenerConstants;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.test.widget.FileUploadWidget;

import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_NAME;
import static com.exadel.aem.toolkit.api.annotations.editconfig.ActionConstants.*;

@Dialog(
        name = DEFAULT_COMPONENT_NAME,
        title = "FileUpload Widget Dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@ChildEditConfig(
        actions = {
                DELETE,
                COPYMOVE,
                EDIT,
                INSERT,
                "custom-action"
        },
        listeners = @Listener(event = ListenerConstants.EVENT_AFTER_DELETE, action = ListenerConstants.ACTION_REFRESH_PAGE),
        dropTargets = @DropTargetConfig(
                nodeName = "image",
                accept = {"image/.*"},
                groups = {"media"},
                propertyName = "file_image" + "file-reference"
        )
)
public class ChildEditConfigAnnotation extends FileUploadWidget {
}
