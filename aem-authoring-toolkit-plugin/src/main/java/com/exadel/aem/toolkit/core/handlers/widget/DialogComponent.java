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
import java.util.Optional;
import java.util.function.BiConsumer;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.Hidden;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.NumberField;
import com.exadel.aem.toolkit.api.annotations.widgets.Password;
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.annotations.widgets.Switch;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.autocomplete.Autocomplete;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorField;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePicker;
import com.exadel.aem.toolkit.api.annotations.widgets.fileupload.FileUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.assets.dependson.DependsOnHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.AttributesHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.CustomHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.DialogFieldHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.GenericPropertiesHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.InheritanceHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.PropertyMappingHandler;
import com.exadel.aem.toolkit.core.handlers.widget.rte.RichTextEditorHandler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

/**
 * Generates and triggers the chain of handlers to store XML markup required to implement particular Granite UI widget
 * based on a field of a component class
 */
public enum DialogComponent {
    CUSTOM_FIELD(),
    TEXT_FIELD(TextField.class),
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
    IMAGE_UPLOAD(ImageUpload.class),
    TEXT_AREA(TextArea.class),
    RICH_TEXT_EDITOR(RichTextEditor.class, new RichTextEditorHandler()),
    HIDDEN(Hidden.class),
    AUTOCOMPLETE(Autocomplete.class, new AutocompleteHandler()),
    PASSWORD(Password.class, new PasswordHandler());

    private static final String NO_COMPONENT_EXCEPTION_MESSAGE_TEMPLATE = "No valid dialog component for field '%s' in class %s";

    private Class<? extends Annotation> annotation;
    private BiConsumer<Element, Field> handler;

    DialogComponent() {
        this.handler = (componentNode, field) -> {};
    }
    @SuppressWarnings("unused")
    DialogComponent(Class<? extends Annotation> annotation) {
        this();
        this.annotation = annotation;
    }
    @SuppressWarnings("unused")
    DialogComponent(Class<? extends Annotation> annotation, BiConsumer<Element, Field> handler) {
        this.annotation = annotation;
        this.handler = handler;
    }

    /**
     * Appends Granite UI markup based on the current {@code Field} to the parent XML node with a default name
     * (equal to the field's name)
     * @param parentNode {@code Element} instance
     * @param field Current {@code Field}
     */
    public void append(Element parentNode, Field field) {
        this.append(parentNode, field, null);
    }

    /**
     * Appends Granite UI markup based on the current {@code Field} to the parent XML node with the specified name
     * @param parentNode {@code Element} instance
     * @param field Current {@code Field}
     * @param name The node name to store
     */
    public void append(Element parentNode, Field field, String name) {
        Element componentNode = PluginRuntime.context().getXmlUtility().createNodeElement(name != null ? name : field.getName());
        parentNode.appendChild(componentNode);
        getHandlerChain().accept(componentNode, field);
    }

    /**
     * Gets a {@code Class} definition bound to this instance
     * @return {@code Class} object
     */
    public Class<? extends Annotation> getAnnotationClass() {
        return annotation;
    }

    /**
     * Gets whether the specified {@code Field} has any particular Granite UI widget-defining annotation
     * @param field {@code Field} of a component class
     * @return True or false
     */
    public static boolean isPresent(Field field) {
        return getComponentAnnotationClass(field) != null;
    }

    /**
     * Gets a {@link DialogComponent} bound to this {@code Field} of a component class, if any
     * @param field {@code Field} of a component class
     * @return Optional {@code DialogComponent} value
     */
    public static Optional<DialogComponent> fromField(Field field) {
        Class<? extends Annotation> fieldAnnotationClass = getComponentAnnotationClass(field);
        if (fieldAnnotationClass == null) {
            return Optional.empty();
        }
        if (DialogWidgetAnnotation.class.equals(fieldAnnotationClass)) {
            return Optional.of(DialogComponent.CUSTOM_FIELD);
        }
        Optional<DialogComponent> result = Arrays.stream(values())
                .filter(dialogComponent -> fieldAnnotationClass.equals(dialogComponent.getAnnotationClass()))
                .findFirst();
        if (!result.isPresent()) {
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
    private static Class<? extends Annotation> getComponentAnnotationClass(Field field) {
        // get first in-built component annotation attached to this field
        Class<? extends Annotation> annotationClass = Arrays.stream(values())
                .filter(dialogComponent -> isAnnotated(field, dialogComponent))
                .map(DialogComponent::getAnnotationClass)
                .findFirst()
                .orElse(null);
        if (annotationClass != null) {
            return annotationClass;
        }
        // if no such annotation, retrieve first custom DialogComponentAnnotation attached to this field
        return PluginReflectionUtility.getFieldAnnotations(field)
                .filter(DialogComponent::isCustomDialogComponentAnnotation)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets whether this {@code Field} has the particular {@code DialogComponent}-defining annotation
     * @param field {@code Field} of a component class
     * @param widget {@code DialogComponent} instance
     * @return True or false
     */
    private static boolean isAnnotated(Field field, DialogComponent widget) {
        return Objects.nonNull(widget.getAnnotationClass())
                && PluginReflectionUtility.getFieldAnnotations(field).anyMatch(fa -> fa.equals(widget.getAnnotationClass()));
    }

    /**
     * Gets whether this {@code Annotation} is a custom dialog annotation
     * @param value {@code Class} definition of the annotation
     * @return True or false
     */
    private static boolean isCustomDialogComponentAnnotation(Class<? extends Annotation> value) {
        return value.isAnnotationPresent(DialogWidgetAnnotation.class);
    }

    /**
     * Generates the chain of handlers to store {@code cq:editConfig} XML markup
     * @return {@code BiConsumer<Element, Field>} instance
     */
    private BiConsumer<Element, Field> getHandlerChain() {
        BiConsumer<Element, Field> mainChain = new GenericPropertiesHandler()
                        .andThen(new PropertyMappingHandler())
                        .andThen(new AttributesHandler())
                        .andThen(new DialogFieldHandler())
                        .andThen(this.handler)
                        .andThen(new DependsOnHandler())
                        .andThen(new CustomHandler());
        return new InheritanceHandler(mainChain).andThen(mainChain);
    }
}
