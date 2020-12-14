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
package com.exadel.aem.toolkit.core.handlers.widget;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.Heading;
import com.exadel.aem.toolkit.api.annotations.widgets.Hidden;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.NumberField;
import com.exadel.aem.toolkit.api.annotations.widgets.Password;
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.annotations.widgets.Switch;
import com.exadel.aem.toolkit.api.annotations.widgets.TagField;
import com.exadel.aem.toolkit.api.annotations.widgets.Text;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.alert.Alert;
import com.exadel.aem.toolkit.api.annotations.widgets.autocomplete.Autocomplete;
import com.exadel.aem.toolkit.api.annotations.widgets.button.Button;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorField;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePicker;
import com.exadel.aem.toolkit.api.annotations.widgets.fileupload.FileUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.widget.rte.RichTextEditorHandler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

/**
 * Enumerates built-in {@link DialogWidget} entities and exposes utility methods to detect whether a {@code DialogWidget}
 * is attached to a particular class field
 */
public enum DialogWidgets implements DialogWidget {
    TEXT_FIELD(TextField.class),
    TAG_FIELD(TagField.class),
    CHECKBOX(Checkbox.class, new CheckboxHandler()),
    SELECT(Select.class, new SelectHandler()),
    PATH_FIELD(PathField.class),
    FIELD_SET(FieldSet.class, new FieldSetHandler()),
    NUMBER_FIELD(NumberField.class),
    RADIO_GROUP(RadioGroup.class, new RadioGroupHandler()),
    MULTI_FIELD(MultiField.class, new MultiFieldHandler()),
    COLOR_FIELD(ColorField.class, new ColorFieldHandler()),
    SWITCH(Switch.class),
    DATE_PICKER(DatePicker.class, new DatePickerHandler()),
    FILE_UPLOAD(FileUpload.class),
    IMAGE_UPLOAD(ImageUpload.class, new ImageUploadHandler()),
    TEXT(Text.class),
    TEXT_AREA(TextArea.class),
    RICH_TEXT_EDITOR(RichTextEditor.class, new RichTextEditorHandler()),
    HIDDEN(Hidden.class),
    AUTOCOMPLETE(Autocomplete.class, new AutocompleteHandler()),
    PASSWORD(Password.class, new PasswordHandler()),
    HEADING(Heading.class),
    ALERT(Alert.class),
    BUTTON(Button.class);

    private static final String NO_COMPONENT_EXCEPTION_MESSAGE_TEMPLATE = "No valid dialog component for field '%s' in class %s";
    private static final BiConsumer<Element, Field> EMPTY_HANDLER = (componentNode, field) -> {
    };

    private Class<? extends Annotation> annotation;
    private BiConsumer<Element, Field> handler;

    DialogWidgets(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    DialogWidgets(Class<? extends Annotation> annotation, BiConsumer<Element, Field> handler) {
        this(annotation);
        this.handler = handler;
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return annotation;
    }

    @Override
    public BiConsumer<Element, Field> getHandler() {
        return handler != null ? handler : EMPTY_HANDLER;
    }

    /**
     * Gets whether the specified {@code Field} has any particular Granite UI widget-defining annotation
     * @param field {@code Field} of a component class
     * @return True or false
     */
    public static boolean isPresent(Field field) {
        return getWidgetAnnotationClass(field) != null;
    }

    /**
     * Gets a {@link DialogWidgets} bound to this {@code Field} of a component class, if any
     * @param field {@code Field} of a component class
     * @return {@code DialogWidget} value, or null
     */
    public static DialogWidget fromField(Field field) {
        Class<? extends Annotation> fieldAnnotationClass = getWidgetAnnotationClass(field);
        if (fieldAnnotationClass == null) {
            return null;
        }
        if (fieldAnnotationClass.isAnnotationPresent(DialogWidgetAnnotation.class)) {
            return new CustomDialogWidget(fieldAnnotationClass);
        }
        DialogWidgets result = Arrays.stream(values())
            .filter(dialogWidget -> fieldAnnotationClass.equals(dialogWidget.getAnnotationClass()))
            .findFirst()
            .orElse(null);
        if (result == null) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(String.format(
                NO_COMPONENT_EXCEPTION_MESSAGE_TEMPLATE,
                field.getName(),
                field.getDeclaringClass())));
        }
        return result;
    }

    /**
     * Gets {@code Class} definition of a {@code DialogComponent}-defining annotation of the current {@code Field}
     * @param field {@code Field} of a component class
     * @return {@code Class} object
     */
    private static Class<? extends Annotation> getWidgetAnnotationClass(Field field) {
        // get first in-built component annotation attached to this field
        Class<? extends Annotation> annotationClass = Arrays.stream(values())
            .filter(dialogComponent -> isAnnotated(field, dialogComponent))
            .map(DialogWidgets::getAnnotationClass)
            .findFirst()
            .orElse(null);
        if (annotationClass != null) {
            return annotationClass;
        }
        // if no such annotation, retrieve first custom DialogComponentAnnotation attached to this field
        return PluginReflectionUtility.getFieldAnnotations(field)
            .filter(DialogWidgets::isCustomDialogWidgetAnnotation)
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets whether this {@code Field} has the particular {@code DialogComponent}-defining annotation
     * @param field  {@code Field} of a component class
     * @param widget {@code DialogComponent} instance
     * @return True or false
     */
    private static boolean isAnnotated(Field field, DialogWidgets widget) {
        return Objects.nonNull(widget.getAnnotationClass())
            && PluginReflectionUtility.getFieldAnnotations(field).anyMatch(fa -> fa.equals(widget.getAnnotationClass()));
    }

    /**
     * Gets whether this {@code Annotation} is a custom dialog annotation
     * @param value {@code Class} definition of the annotation
     * @return True or false
     */
    private static boolean isCustomDialogWidgetAnnotation(Class<? extends Annotation> value) {
        return value.isAnnotationPresent(DialogWidgetAnnotation.class);
    }

    /**
     * Implements {@link DialogWidget} to expose a custom dialog annotation attached to a field with no built-in annotation
     */
    private static class CustomDialogWidget implements DialogWidget {
        private Class<? extends Annotation> annotationClass;

        CustomDialogWidget(Class<? extends Annotation> annotationClass) {
            this.annotationClass = annotationClass;
        }

        @Override
        public Class<? extends Annotation> getAnnotationClass() {
            return annotationClass;
        }

        @Override
        public BiConsumer<Element, Field> getHandler() {
            return EMPTY_HANDLER;
        }
    }
}
