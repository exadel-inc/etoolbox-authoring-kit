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
package com.exadel.aem.toolkit.test.component;

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;

@SuppressWarnings("unused")
class SampleFieldsetBase1 {

    private static final String LABEL_CAPTION = "Caption";
    private static final String DESCRIPTION_CAPTION = "Provide the text to be shown as the Caption.";

    @DialogField(
            name = "Title",
            label = "Label title's",
            description = "Title's description"
    )
    @TextField
    @Place("Main tab")
    private String title;

    @DialogField(
            name = "Description",
            label = "Description's label",
            description = "description"
    )
    @RichTextEditor(
            features = {
                    RteFeatures.LINKS_MODIFYLINK,
                    RteFeatures.LINKS_UNLINK
            }
    )
    @Place("Main tab")
    private String description;

    @DialogField(
            name = LABEL_CAPTION,
            label = LABEL_CAPTION,
            description = DESCRIPTION_CAPTION
    )
    @TextField
    @Place("Main tab")
    private String caption;

    @DialogField(
            name = "Label's name",
            label = "Label",
            description = "description",
            required = true
    )
    @TextField
    @Place("Labels' tab")
    private String label;

    @DialogField(
            name = "FieldName",
            label = "Field label",
            description = "Field's description",
            required = true
    )
    @PathField(
            rootPath = "root/path"
    )
    @Place("URL's tab")
    private String url;

    @DialogField(
            name = "ImageText",
            label = "Text on image",
            description = "Description for text on image"
    )
    @TextField
    @Place("Main tab")
    private String imageText;
}
