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

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.api.annotations.widgets.NumberField;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePicker;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DateTimeValue;
import com.exadel.aem.toolkit.api.annotations.widgets.fileupload.FileUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Characters;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.ParagraphFormat;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.test.custom.annotation.CustomWidgetAnnotationAuto;

import static com.exadel.aem.toolkit.plugin.util.TestConstants.DEFAULT_COMPONENT_NAME;
import static com.exadel.aem.toolkit.plugin.util.TestConstants.DEFAULT_COMPONENT_TITLE;

@SuppressWarnings("unused")
public class ValidationTestCases {
    @Dialog(name = DEFAULT_COMPONENT_NAME, title = " ")
    public static class InvalidTitleDialog {}

    @Dialog(name = DEFAULT_COMPONENT_NAME)
    public static class MissingTitleDialog {}

    @Dialog(name = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    public static class InvalidNumberFieldDialog {
        @NumberField(value = "not-a-number", min = 0, max = 10)
        String number;
    }

    @Dialog(name = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    public static class InvalidImageUploadDialog {
        @ImageUpload(sizeLimit = -99)
        String image;
    }

    @Dialog(name = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    public static class InvalidTextAreaDialog {
        @TextArea(rows = 0, cols = -99)
        String text;
    }

    @Dialog(name = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    public static class InvalidRteCharactersDialog {
        @RichTextEditor(
                specialCharacters = {
                        @Characters(rangeStart = 998, rangeEnd = 1020, name = "Range"),
                        @Characters(rangeStart = 998, name = "invalid"),
                }
        )
        String text;
    }

    @Dialog(name = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    public static class InvalidRteParaformatDialog {
        @RichTextEditor(
                formats = {
                        @ParagraphFormat(tag = "tag", description = "")
                }
        )
        String text;
    }

    @Dialog(name = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    public static class InvalidPathDialog {
        @FileUpload(uploadUrl = "wrong path")
        String file;
    }

    @Dialog(name = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    public static class InvalidDatePickerDialog {
        @DatePicker(minDate = @DateTimeValue(day = 1, month = 13, year = -1))
        String date;
    }

    @Dialog(name = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    public static class InvalidCustomAnnotationDialog {
        @CustomWidgetAnnotationAuto(customColor = "yellow")
        String custom;
    }

    @Dialog(name = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    @HtmlTag(tagName = "", className = "wrapper")
    public static class ComponentWithWrongHtmlTag1 {}

    @Dialog(name = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    @HtmlTag(className = " ")
    public static class ComponentWithWrongHtmlTag2 {}
}
