package com.exadel.aem.toolkit.core.maven;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.NumberField;
import com.exadel.aem.toolkit.api.annotations.widgets.fileupload.FileUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Characters;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.ParagraphFormat;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.core.exceptions.ValidationException;
import com.exadel.aem.toolkit.test.custom.CustomAnnotationAutomapping;

public class ValidationsTest extends ExceptionTestBase {
    private static final String COMPONENT_NAME_REQUISITE = "component-name";
    private static final String COMPONENT_TITLE_REQUISITE = "component-title";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testNonBlankValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("'' provided");
        testComponent(InvalidTitleDialog.class);
    }

    @Test
    public void testAllNotBlankValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("string properties must not be blank");
        testComponent(InvalidRteParaformatDialog.class);
    }

    @Test
    public void testNumberValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("'not-a-number' provided");
        testComponent(InvalidNumberFieldDialog.class);
    }

    @Test
    public void testNonNegativeNumberValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("'-99' provided");
        testComponent(InvalidImageUploadDialog.class);
    }

    @Test
    public void testPositiveNumberValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("'0' provided");
        testComponent(InvalidTextAreaDialog.class);
    }


    @Test
    public void testCharactersValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("a character range (start < end) or entity definition must be set");
        testComponent(InvalidRteCharactersDialog.class);
    }

    @Test
    public void testJcrPathValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("'wrong path' provided");
        testComponent(InvalidPathDialog.class);
    }


    @Test
    public void testCustomValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("one of 'red', 'green', or 'blue' must be provided");
        testComponent(InvalidCustomAnnotationDialog.class);
    }


    @Dialog(name = COMPONENT_NAME_REQUISITE, title = "")
    private static class InvalidTitleDialog {}


    @Dialog(name = COMPONENT_NAME_REQUISITE, title = COMPONENT_TITLE_REQUISITE)
    @SuppressWarnings("unused")
    private static class InvalidNumberFieldDialog {
        @NumberField(value = "not-a-number", min = 0, max = 10)
        String number;
    }

    @Dialog(name = COMPONENT_NAME_REQUISITE, title = COMPONENT_TITLE_REQUISITE)
    @SuppressWarnings("unused")
    private static class InvalidImageUploadDialog {
        @ImageUpload(
                title="Invalid Image Upload",
                sizeLimit = -99
        )
        String image;
    }

    @Dialog(name = COMPONENT_NAME_REQUISITE, title = COMPONENT_TITLE_REQUISITE)
    @SuppressWarnings("unused")
    private static class InvalidTextAreaDialog {
        @TextArea(rows = 0, cols = -99)
        String text;
    }

    @Dialog(name = COMPONENT_NAME_REQUISITE, title = COMPONENT_TITLE_REQUISITE)
    @SuppressWarnings("unused")
    private static class InvalidRteCharactersDialog {
        @RichTextEditor(
                specialCharacters = {
                        @Characters(rangeStart = 998, rangeEnd = 1020, name = "Range"),
                        @Characters(rangeStart = 998, name = "invalid"),
                }
        )
        String text;
    }

    @Dialog(name = COMPONENT_NAME_REQUISITE, title = COMPONENT_TITLE_REQUISITE)
    @SuppressWarnings("unused")
    private static class InvalidRteParaformatDialog {
        @RichTextEditor(
                formats = {
                        @ParagraphFormat(tag = "tag", description = "")
                }
        )
        String text;
    }

    @Dialog(name = COMPONENT_NAME_REQUISITE, title = COMPONENT_TITLE_REQUISITE)
    @SuppressWarnings("unused")
    private static class InvalidPathDialog {
        @FileUpload(uploadUrl = "wrong path")
        String file;
    }

    @Dialog(name = COMPONENT_NAME_REQUISITE, title = COMPONENT_TITLE_REQUISITE)
    @SuppressWarnings("unused")
    private static class InvalidCustomAnnotationDialog {
        @CustomAnnotationAutomapping(customColor = "yellow")
        String custom;
    }
}
