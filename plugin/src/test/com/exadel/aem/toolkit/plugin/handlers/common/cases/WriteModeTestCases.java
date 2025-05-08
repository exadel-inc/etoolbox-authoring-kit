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
package com.exadel.aem.toolkit.plugin.handlers.common.cases;

import static com.exadel.aem.toolkit.api.annotations.editconfig.ActionConstants.COPYMOVE;
import static com.exadel.aem.toolkit.api.annotations.editconfig.ActionConstants.DELETE;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.listener.Listener;
import com.exadel.aem.toolkit.api.annotations.editconfig.listener.ListenerConstants;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.WriteMode;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

@SuppressWarnings("unused")
public class WriteModeTestCases {

    private WriteModeTestCases() {
    }

    @AemComponent(
        path = TestConstants.NONEXISTENT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE,
        writeMode = WriteMode.CREATE
    )
    @Dialog
    public static class FolderCreatingComponent {

        @DialogField(label = "Text input")
        @TextField
        private String text;
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class FolderCleaningComponent {

        @DialogField(label = "Text input")
        @TextField
        private String text;
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE,
        writeMode = WriteMode.MERGE
    )
    @Dialog(helpPath = "https://www.acme.com")
    @EditConfig(
        actions = { DELETE, COPYMOVE, "custom-action" },
        dropTargets = @DropTargetConfig(
            nodeName = "image",
            accept = {"image/.*"},
            groups = {"media"},
            propertyName = "file_image"
        )
    )
    @ChildEditConfig(
        listeners = @Listener(event = ListenerConstants.EVENT_AFTER_DELETE, action = ListenerConstants.ACTION_REFRESH_PAGE)
    )
    public static class MergingComponent {

        @DialogField(label = "Text input")
        @TextField
        private String text;
    }

    @AemComponent(
        path = TestConstants.NONEXISTENT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class FolderMissingComponent {

        @DialogField(label = "Text input")
        @TextField
        private String text;
    }
}
