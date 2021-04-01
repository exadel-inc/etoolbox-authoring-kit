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

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.listener.Listener;
import com.exadel.aem.toolkit.api.annotations.editconfig.listener.ListenerConstants;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.test.custom.annotation.CustomChildEditConfigAnnotationAuto;
import com.exadel.aem.toolkit.test.widget.FileUploadWidget;

import static com.exadel.aem.toolkit.api.annotations.editconfig.ActionConstants.COPYMOVE;
import static com.exadel.aem.toolkit.api.annotations.editconfig.ActionConstants.DELETE;
import static com.exadel.aem.toolkit.api.annotations.editconfig.ActionConstants.EDIT;
import static com.exadel.aem.toolkit.api.annotations.editconfig.ActionConstants.INSERT;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
    path = DEFAULT_COMPONENT_NAME,
    title = "FileUpload Widget Dialog"
)
@Dialog
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
@CustomChildEditConfigAnnotationAuto(autoField1 = "value1")
public class ChildEditConfigAnnotation extends FileUploadWidget {
}
