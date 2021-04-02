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

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.api.annotations.widgets.FileUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.NumberField;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePicker;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DateTimeValue;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Characters;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.ParagraphFormat;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.test.custom.annotation.CustomWidgetAnnotationAuto;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_TITLE;

@SuppressWarnings("unused")
public class ValidationTestCases {
    @AemComponent(path = DEFAULT_COMPONENT_NAME, title = " ")
    @Dialog
    public static class InvalidTitleDialog {}

    @Dialog(name = DEFAULT_COMPONENT_NAME) // legacy "name" param is for compatibility testing
    @SuppressWarnings("deprecation")
    public static class MissingTitleDialog {}

    @AemComponent(path = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    @Dialog
    public static class InvalidNumberFieldDialog {
        @NumberField(value = "not-a-number", min = 0, max = 10)
        String number;
    }

    @Dialog(name = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE) // legacy "name" param is for compatibility testing
    @SuppressWarnings("deprecation")
    public static class InvalidImageUploadDialog {
        @ImageUpload(sizeLimit = -99)
        String image;
    }

    @AemComponent(path = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    @Dialog
    public static class InvalidTextAreaDialog {
        @TextArea(rows = 0, cols = -99)
        String text;
    }

    @AemComponent(path = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    @Dialog
    public static class InvalidRteCharactersDialog {
        @RichTextEditor(
                specialCharacters = {
                        @Characters(rangeStart = 998, rangeEnd = 1020, name = "Range"),
                        @Characters(rangeStart = 998, name = "invalid"),
                }
        )
        String text;
    }

    @AemComponent(path = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    @Dialog
    public static class InvalidRteParaformatDialog {
        @RichTextEditor(
                formats = {
                        @ParagraphFormat(tag = "tag", description = "")
                }
        )
        String text;
    }

    @AemComponent(path = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    @Dialog
    public static class InvalidPathDialog {
        @FileUpload(uploadUrl = "wrong path")
        String file;
    }

    @AemComponent(path = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    @Dialog
    public static class InvalidDatePickerDialog {
        @DatePicker(minDate = @DateTimeValue(day = 1, month = 13, year = -1))
        String date;
    }

    @AemComponent(path = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    @Dialog
    public static class InvalidCustomAnnotationDialog {
        @CustomWidgetAnnotationAuto(customColor = "yellow")
        String custom;
    }

    @AemComponent(path = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    @Dialog
    @HtmlTag(tagName = "", className = "wrapper")
    public static class ComponentWithWrongHtmlTag1 {}

    @AemComponent(path = DEFAULT_COMPONENT_NAME, title = DEFAULT_COMPONENT_TITLE)
    @Dialog
    @HtmlTag(className = " ")
    public static class ComponentWithWrongHtmlTag2 {}
}
