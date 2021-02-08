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
package com.exadel.aem.toolkit.plugin.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import com.google.common.base.CaseFormat;

import com.exadel.aem.toolkit.api.annotations.meta.EnumValue;
import com.exadel.aem.toolkit.api.annotations.meta.IgnoreValue;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.ReflectionException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.validation.Validation;

/**
 * Helper class for validating and rendering typed attributes to XML nodes
 * @see com.exadel.aem.toolkit.api.runtime.XmlUtility#setAttribute(Element, String, Annotation, BinaryOperator)
 * @param <T> Type of value to be rendered as XML attribute
 */
public class AttributeSettingHelper<T> {
    private static final String TYPE_TOKEN_TEMPLATE = "{%s}%s";
    private static final String TYPE_TOKEN_ARRAY_TEMPLATE = "{%s}[%s]";
    private static final String STRING_ESCAPE = "\\";
    private static final String REFLECTION_EXCEPTION_MESSAGE_TEMPLATE = "Error accessing property '%s' of @%s";

    private final Class<T> valueType;
    private boolean valueTypeIsSupported;

    private Annotation annotation;
    private Method method;
    private String name;
    private String[] ignoredValues;
    private boolean blankValuesAllowed;

    private boolean isEnum;
    private EnumValue enumModifier;

    private Validation validationChecker = Validation.defaultChecker();
    private BinaryOperator<String> valueMerger = PluginXmlUtility.DEFAULT_ATTRIBUTE_MERGER;

    /**
     * Creates XmlAttributeSettingHelper instance parametrized with value type
     * @param valueType Type of value to be rendered as XML attribute
     */
    private AttributeSettingHelper(Class<T> valueType) {
        this.valueType = valueType;
    }

    /**
     * Retrieves XmlAttributeSettingHelper for particular {@code Annotation}'s property
     * @param annotation Target annotation
     * @param method Method representing target annotation's property
     * @return New {@code XmlAttributeSettingHelper} instance
     */
    @SuppressWarnings({"deprecation", "squid:S1874"}) // IgnoreValue processing remains for compatibility reasons until v.2.0.0
    public static AttributeSettingHelper forMethod(Annotation annotation, Method method) {
        AttributeSettingHelper attributeSetter = new AttributeSettingHelper<>(getMethodWrappedType(method));
        if (!fits(method)) {
            return attributeSetter;
        }
        attributeSetter.valueTypeIsSupported = true;
        attributeSetter.method = method;
        attributeSetter.annotation = annotation;
        attributeSetter.name = method.getName();
        attributeSetter.isEnum = method.getReturnType().isEnum()
                || (method.getReturnType().getComponentType() != null
                && method.getReturnType().getComponentType().isEnum());
        if (method.isAnnotationPresent(EnumValue.class)) {
            attributeSetter.enumModifier = method.getDeclaredAnnotation(EnumValue.class);
        }
        if (method.isAnnotationPresent(PropertyRendering.class)) {
            PropertyRendering propertyRendering = method.getAnnotation(PropertyRendering.class);
            attributeSetter.ignoredValues = propertyRendering.ignoreValues();
            attributeSetter.blankValuesAllowed = propertyRendering.allowBlank();
        } else if (method.isAnnotationPresent(IgnoreValue.class)) {
            attributeSetter.ignoredValues = new String[] {method.getAnnotation(IgnoreValue.class).value()};
        }

        if (PluginObjectUtility.propertyIsNotDefault(annotation, method)) {
            attributeSetter.validationChecker = Validation.forMethod(method);
        }
        return attributeSetter;
    }

    /**
     * Retrieves XmlAttributeSettingHelper for specified attribute name and type
     * @param name Target attribute name
     * @param valueType Target value type
     * @return New typed {@code XmlAttributeSettingHelper} instance
     */
    static <T> AttributeSettingHelper<T> forNamedValue(String name, Class<T> valueType) {
        AttributeSettingHelper<T> attributeSetter = new AttributeSettingHelper<>(valueType);
        if (!fits(valueType)) {
            return attributeSetter;
        }
        attributeSetter.valueTypeIsSupported = true;
        attributeSetter.name = name;
        return attributeSetter;
    }

    /**
     * Sets {@param name} of current instance. Suitable for chained initialization of XmlAttributeSettingHelper
     * @param name Provisional name
     * @return Current {@code XmlAttributeSettingHelper} instance
     */
    public AttributeSettingHelper<T> withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets {@param merger} of current instance. Suitable for chained initialization of XmlAttributeSettingHelper
     * @param merger Function that manages an existing attribute value, and a new one (whether to keep only one of them
     *               or combine/merge)
     * @return Current {@code XmlAttributeSettingHelper} instance
     */
    public AttributeSettingHelper<T> withMerger(BinaryOperator<String> merger) {
        this.valueMerger = merger;
        return this;
    }

    public void setAttribute(Target target) {
        if (!valueTypeIsSupported) {
            return;
        }
        try {
            Object invocationResult = method.invoke(annotation);
            if (method.getReturnType().isArray()) {
                List<T> invocationResultList = Arrays.stream(castToArray(invocationResult))
                        .map(this::cast)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                setAttribute(target, invocationResultList);
            } else {
                setAttribute(target, cast(invocationResult));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ReflectionException(
                    String.format(REFLECTION_EXCEPTION_MESSAGE_TEMPLATE, method.getName(), annotation.annotationType().getSimpleName()),
                    e));
        }
    }

    /**
     * Implements {@code Element}'s attribute rendering logic
     * @param element Element node instance
     */
    void setAttribute(Element element) {
        if (!valueTypeIsSupported) {
            return;
        }
        try {
            Object invocationResult = method.invoke(annotation);
            if (method.getReturnType().isArray()) {
                List<T> invocationResultList = Arrays.stream(castToArray(invocationResult))
                        .map(this::cast)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                setAttribute(element, invocationResultList);
            } else {
                setAttribute(element, cast(invocationResult));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ReflectionException(
                    String.format(REFLECTION_EXCEPTION_MESSAGE_TEMPLATE, method.getName(), annotation.annotationType().getSimpleName()),
                    e));
        }
    }

    /**
     * Implements {@code Element}'s attribute rendering logic
     * @param element Element node instance
     * @param value Particular value to be rendered
     */
    void setAttribute(Target element, T value) {
        if (!valueTypeIsSupported) {
            return;
        }
        String stringifiedValue = value != null ? value.toString() : StringUtils.EMPTY;

        if (!isValid(stringifiedValue)) {
            element.getAttributes().remove(name);
            return;
        }

        setAttribute(element, valueType.equals(String.class)
                ? stringifiedValue
                : String.format(TYPE_TOKEN_TEMPLATE, valueType.getSimpleName(), stringifiedValue));
    }

    /**
     * Implements {@code Element}'s attribute rendering logic
     * @param element Element node instance
     * @param values List of values to be rendered
     */
    void setAttribute(Target element, List<T> values) {
        if (!valueTypeIsSupported || values == null || values.isEmpty()) {
            return;
        }
        String valueString = values.stream()
                .map(Object::toString)
                .filter(this::isValid)
                .map(s -> s.startsWith(RteFeatures.BEGIN_POPOVER) && s.endsWith(RteFeatures.END_POPOVER) ? STRING_ESCAPE + s : s)
                .collect(Collectors.joining(RteFeatures.FEATURE_SEPARATOR));
        if (valueString.isEmpty()) {
            return;
        }
        setAttribute(element, valueType.equals(String.class)
                ? String.format(PluginXmlUtility.ATTRIBUTE_LIST_TEMPLATE, valueString)
                : String.format(TYPE_TOKEN_ARRAY_TEMPLATE, valueType.getSimpleName(), valueString));
    }

    /**
     * Sets String-casted attribute value to an {@code Element} node
     * @param element Element node instance
     * @param value String to store as the attribute
     */
    private void setAttribute(Target element, String value) {
        String oldAttributeValue = element.getAttributes().getOrDefault(name, StringUtils.EMPTY);
        element.attribute(name, valueMerger.apply(oldAttributeValue, value));
    }

    void setAttribute(Element element, T value) {
        if (!valueTypeIsSupported) {
            return;
        }
        String stringifiedValue = value != null ? value.toString() : StringUtils.EMPTY;

        if (!isValid(stringifiedValue)) {
            element.removeAttribute(name);
            return;
        }

        setAttribute(element, valueType.equals(String.class)
            ? stringifiedValue
            : String.format(TYPE_TOKEN_TEMPLATE, valueType.getSimpleName(), stringifiedValue));
    }

    void setAttribute(Element element, List<T> values) {
        if (!valueTypeIsSupported || values == null || values.isEmpty()) {
            return;
        }
        String valueString = values.stream()
            .map(Object::toString)
            .filter(this::isValid)
            .map(s -> s.startsWith(RteFeatures.BEGIN_POPOVER) && s.endsWith(RteFeatures.END_POPOVER) ? STRING_ESCAPE + s : s)
            .collect(Collectors.joining(RteFeatures.FEATURE_SEPARATOR));
        if (valueString.isEmpty()) {
            return;
        }
        setAttribute(element, valueType.equals(String.class)
            ? String.format(PluginXmlUtility.ATTRIBUTE_LIST_TEMPLATE, valueString)
            : String.format(TYPE_TOKEN_ARRAY_TEMPLATE, valueType.getSimpleName(), valueString));
    }

    /**
     * Sets String-casted attribute value to an {@code Element} node
     * @param element Element node instance
     * @param value String to store as the attribute
     */
    private void setAttribute(Element element, String value) {
        String oldAttributeValue = element.hasAttribute(name)
            ? element.getAttribute(name)
            : "";
        element.setAttribute(name, valueMerger.apply(oldAttributeValue, value));
    }

    /**
     * Gets whether this string, as an arbitrary user-set value representation, is valid to be stored as an XML attribute
     * @param value String representing a user-set value
     * @return True or false
     */
    private boolean isValid(String value) {
        if (StringUtils.isBlank(value) && !blankValuesAllowed) {
            return false;
        }
        return !ArrayUtils.contains(ignoredValues, value);
    }

    /**
     * Tries to cast generic value to current instance's type
     * @param value Raw value
     * @return Type-casted value, or null
     */
    private T cast(Object value) {
        if (!validationChecker.test(value)) {
            return null;
        }
        if (enumModifier != null) {
            return valueType.cast(transform(value.toString(), enumModifier.transformation()));
        }
        if (isEnum) {
            return valueType.cast(value.toString());
        }
        return value != null ? valueType.cast(value) : null;
    }

    /**
     * Casts generic value to the array possessing entries of current instance's type
     * @param value Raw value
     * @return Array of type-casted values
     */
    private Object[] castToArray(Object value) {
        Object[] result = new Object[Array.getLength(value)];
        for (int i = 0; i < result.length; i++) {
            result[i] = Array.get(value, i);
        }
        return result;
    }

    /**
     * Gets whether specific annotation property/method can be rendered to XML
     * @param method {@code Method} instance representing an annotation property
     * @return True or false
     */
    private static boolean fits(Method method) {
        return fits(ClassUtils.primitiveToWrapper(getMethodWrappedType(method)));
    }

    /**
     * Gets whether value of specific type can be rendered to XML
     * @param valueType Annotation's property {@code Class}
     * @return True or false
     */
    private static boolean fits(Class<?> valueType) {
        return valueType.equals(String.class)
                || valueType.equals(Long.class)
                || valueType.equals(Double.class)
                || valueType.equals(Boolean.class);
    }

    /**
     * Retrieves an eligible object type for a method ({@code Enum}s being casted to Strings)
     * @param method {@code Method} instance representing an annotation property
     * @return Object type
     */
    private static Class<?> getMethodWrappedType(Method method) {
        Class<?> effectiveType = PluginReflectionUtility.getPlainType(method);
        if (effectiveType.isEnum()) {
            return String.class;
        }
        return ClassUtils.primitiveToWrapper(effectiveType);
    }

    /**
     * Utility method to transform a stringified enum value to uppercase, lowercase or camel-case
     * depending on particular AEM component's specification
     * @param value Raw string value
     * @param transformation {@link StringTransformation} variant
     * @return Transformed string value
     */
    private static String transform(String value, StringTransformation transformation) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        switch (transformation) {
            case LOWERCASE:
                return value.toLowerCase();
            case UPPERCASE:
                return value.toUpperCase();
            case CAMELCASE:
                return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, value.toLowerCase());
            default:
                return value;
        }
    }
}
