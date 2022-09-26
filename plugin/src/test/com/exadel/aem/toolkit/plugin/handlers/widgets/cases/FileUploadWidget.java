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
package com.exadel.aem.toolkit.plugin.handlers.widgets.cases;

import static com.exadel.aem.toolkit.plugin.maven.TestConstants.DEFAULT_COMPONENT_NAME;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FileUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.common.ElementVariant;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Size;

@AemComponent(
        path = DEFAULT_COMPONENT_NAME,
        title = "FileUpload Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class FileUploadWidget {
    @DialogField
    @FileUpload(
            emptyText = "empty text",
            async = false,
            autoStart = false,
            uploadUrl = "/content/dam/acme/uploads",
            mimeTypes = {".png", ".jpg"},
            variant = ElementVariant.MINIMAL,
            size = Size.LARGE,
            sizeLimit = 10000,
            text = "Upload File"
    )
    String file1;
}
