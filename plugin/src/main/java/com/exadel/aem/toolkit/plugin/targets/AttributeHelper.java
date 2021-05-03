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
package com.exadel.aem.toolkit.plugin.targets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
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

import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.utils.MemberUtil;
import com.exadel.aem.toolkit.plugin.utils.StringUtil;
import com.exadel.aem.toolkit.plugin.validators.Validation;

/**
 * Helper class for storing authored attributes to entities, such as {@link Target} or an XML element
 * @param <T> Type of managed entity
 * @param <V> Type of value to set
 */
public class AttributeHelper<T, V> {

    /* ------------------------------------------
       Instance fields and constructors/modifiers
       ------------------------------------------ */

    private final Class<T> holderType;
    private final Class<V> valueType;
    private boolean valueTypeIsSupported;

    private Annotation annotation;
    private Method method;
    private String name;
    private String[] ignoredValues;
    private boolean blankValuesAllowed;
    private Class<?> typeHintValueType;

    private boolean isEnum;
    private StringTransformation transformation;

    private Validation validationChecker = Validation.defaultChecker();
    private BinaryOperator<String> valueMerger = TargetImpl.DEFAULT_ATTRIBUTE_MERGER;

    /**
     * Creates a new {@code AttributeSettingHelper} instance parametrized with holder type and value type
     * @param holderType Type of attribute holder
     * @param valueType  Type of value to be stored
     */
    private AttributeHelper(Class<T> holderType, Class<V> valueType) {
        this.holderType = holderType;
        this.valueType = valueType;
    }

    /**
     * Sets name of current instance. Suitable for chained initialization of XmlAttributeSettingHelper
     * @param name Provisional name
     * @return Current {@code XmlAttributeSettingHelper} instance
     */
    public AttributeHelper<T,V> withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets a merger for the current instance. Suitable for chained initialization of XmlAttributeSettingHelper
     * @param merger Function that manages an existing attribute value, and a new one (whether to keep only one of them
     *               or combine/merge)
     * @return Current {@code XmlAttributeSettingHelper} instance
     */
    public AttributeHelper<T,V> withMerger(BinaryOperator<String> merger) {
        this.valueMerger = merger;
        return this;
    }


    /* --------------
       Values setting
       -------------- */


    /**
     * Stores the attribute value defined by the wrapped {@code Annotation} property to the given target
     * @param target Value holder instance
     */
    public void setTo(T target) {
        if (!valueTypeIsSupported) {
            return;
        }
        Object invocationResult = AnnotationUtil.getProperty(annotation, method);
        if (invocationResult == null) {
            return;
        }
        if (method.getReturnType().isArray()) {
            List<V> invocationResultList = Arrays.stream(castToArray(invocationResult))
                .map(this::cast)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            setValue(invocationResultList, target);
        } else {
            setValue(cast(invocationResult), target);
        }
    }

    /**
     * Stores the provided attribute value to the given target
     * @param value  Value to store
     * @param target Value holder instance
     */
    public void setValue(V value, T target) {
        if (!valueTypeIsSupported) {
            return;
        }
        String valueString = value != null ? value.toString() : StringUtils.EMPTY;

        if (!isValid(valueString)) {
            removeAttributeFrom(target);
            return;
        }

        setValue(StringUtil.format(value, getTypeHintValueType()), target);
    }

    /**
     * Stores the provided attribute values collection to the given target
     * @param value  Value to store
     * @param target Value holder instance
     */
    public void setValue(List<V> value, T target) {
        if (!valueTypeIsSupported || value == null || value.isEmpty()) {
            return;
        }

        List<V> validValues = value.stream()
            .filter(Objects::nonNull)
            .filter(obj -> isValid(obj.toString()))
            .collect(Collectors.toList());
        if (validValues.isEmpty()) {
            return;
        }
        setValue(StringUtil.format(validValues, getTypeHintValueType()), target);
    }


    /**
     * Assigns a {@code String}-casted attribute value to the given target
     * @param value  String to store as the attribute
     * @param target Value holder instance
     */
    private void setValue(String value, T target) {
        if (Element.class.equals(holderType)) {
            Element element = (Element) target;
            String oldAttributeValue = element.hasAttribute(name)
                ? element.getAttribute(name)
                : StringUtils.EMPTY;
            element.setAttribute(name, valueMerger.apply(oldAttributeValue, value));
        } else if (Target.class.equals(holderType)) {
            Target castedTarget = (Target) target;
            String oldAttributeValue = castedTarget.getAttributes().getOrDefault(name, StringUtils.EMPTY);
            castedTarget.attribute(name, valueMerger.apply(oldAttributeValue, value));
        }
    }

    /**
     * Removes the attribute known by {@code name} from the media provided
     * @param target Value holder instance
     */
    private void removeAttributeFrom(T target) {
        if (Element.class.equals(holderType)) {
            ((Element) target).removeAttribute(name);
        } else if (Target.class.equals(holderType)){
            ((Target) target).getAttributes().remove(name);
        }
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
     * Gets the {@code Class} object used for type hinting when rendering a value as a JCR string
     * @return {@code Class<?>} reference
     */
    private Class<?> getTypeHintValueType() {
        return typeHintValueType != null
            ? typeHintValueType
            : valueType;
    }


    /* --------------
       Values casting
       -------------- */


    /**
     * Tries to cast a generic value to the current instance's type
     * @param value Raw value
     * @return Type-casted value, or null
     */
    private V cast(Object value) {
        if (!validationChecker.test(value) || value == null) {
            return null;
        }
        if (isEnum) {
            return valueType.cast(transform(transformation, value.toString()));
        } else if (valueType.equals(String.class)) {
            String result = transform(transformation, valueType.cast(value).toString());
            return valueType.cast(result);
        }
        return valueType.cast(value);
    }

    /**
     * Casts a generic value to the array possessing entries of current instance's type
     * @param value Raw value
     * @return Array of type-casted values
     */
    private static Object[] castToArray(Object value) {
        Object[] result = new Object[Array.getLength(value)];
        for (int i = 0; i < result.length; i++) {
            result[i] = Array.get(value, i);
        }
        return result;
    }

    /**
     * Applies an optional user-set transformation to the provided attribute value casted to string
     * @param transformation {@link StringTransformation} variant
     * @param value          Raw string value
     * @return Transformed string value
     */
    private static String transform(StringTransformation transformation, String value) {
        if (transformation == null) {
            return value;
        }
        return transformation.apply(value);
    }


    /* -------------
       Factory logic
       ------------- */


    /**
     * Retrieves an {@link AttributeHelper.Builder} aimed at creating helper object for manipulation with XML
     * elements. This is mainly to be used with notation such as
     * {@code AttributeSettingHelper.forXmlTarget().forAnnotationProperty(...)}
     * @return {@link AttributeHelper.Builder} instance
     */
    public static Builder<Element> forXmlTarget() {
        return new Builder<>(Element.class);
    }

    /**
     * Retrieves an AttributeSettingHelper instance for a particular {@code Annotation}'s property
     * @param annotation {@code Annotation} object to handle
     * @param property   {@code Method} that represents the annotation's property
     * @return New {@code AttributeSettingHelper} instance
     */
    public static AttributeHelper<Target, Object> forAnnotationProperty(Annotation annotation, Method property) {
        return new Builder<>(Target.class).forAnnotationProperty(annotation, property);
    }

    /**
     * Builds instances of {@link AttributeHelper} for a particular attribute-storing media type
     */
    public static class Builder<T> {
        private final Class<T> holderType;

        /**
         * Creates a new {@code Builder} instance
         * @param holderType {@code Class<?>} reference representing the type of media where attributes values are stored
         */
        private Builder(Class<T> holderType) {
            this.holderType = holderType;
        }

        /**
         * Retrieves an AttributeSettingHelper instance for a particular {@code Annotation}'s property
         * @param annotation {@code Annotation} object to handle
         * @param property   {@code Method} that represents the annotation's property
         * @return New {@code AttributeSettingHelper} instance
         */
        @SuppressWarnings("unchecked")
        public AttributeHelper<T, Object> forAnnotationProperty(Annotation annotation, Method property) {
            AttributeHelper<T, ?> attributeSetter = new AttributeHelper<>(holderType, getMethodWrappedType(property));
            if (!fits(property)) {
                return (AttributeHelper<T, Object>) attributeSetter;
            }
            attributeSetter.valueTypeIsSupported = true;
            attributeSetter.method = property;
            attributeSetter.annotation = annotation;
            attributeSetter.name = property.getName();

            attributeSetter.isEnum = property.getReturnType().isEnum()
                || (property.getReturnType().getComponentType() != null
                && property.getReturnType().getComponentType().isEnum());
            if (property.isAnnotationPresent(PropertyRendering.class)) {
                PropertyRendering propertyRendering = property.getAnnotation(PropertyRendering.class);
                attributeSetter.ignoredValues = propertyRendering.ignoreValues();
                attributeSetter.blankValuesAllowed = propertyRendering.allowBlank();
                attributeSetter.transformation = propertyRendering.transform();
                if (!propertyRendering.valueType().equals(_Default.class)) {
                    attributeSetter.typeHintValueType = propertyRendering.valueType();
                }
            }
            if (AnnotationUtil.propertyIsNotDefault(annotation, property)) {
                attributeSetter.validationChecker = Validation.forMethod(property);
            }
            return (AttributeHelper<T, Object>) attributeSetter;
        }

        /**
         * Retrieves an AttributeSettingHelper instance for a specified attribute name and value type
         * @param name      Target attribute name
         * @param valueType Target value type
         * @return New {@code AttributeSettingHelper} instance
         */
        public <V> AttributeHelper<T, V> forNamedValue(String name, Class<V> valueType) {
            AttributeHelper<T,V> attributeSetter = new AttributeHelper<>(holderType, valueType);
            if (!fits(valueType)) {
                return attributeSetter;
            }
            attributeSetter.valueTypeIsSupported = true;
            attributeSetter.name = name;
            return attributeSetter;
        }

        /**
         * Gets whether a specific annotation property/method can be rendered to XML
         * @param method {@code Method} instance representing an annotation property
         * @return True or false
         */
        private static boolean fits(Method method) {
            return fits(ClassUtils.primitiveToWrapper(getMethodWrappedType(method)));
        }

        /**
         * Retrieves an eligible object type for a method ({@code Enum}s being cast to {@code String}s)
         * @param method {@code Method} instance representing an annotation property
         * @return Object type
         */
        private static Class<?> getMethodWrappedType(Method method) {
            Class<?> effectiveType = MemberUtil.getPlainType(method);
            if (effectiveType.isEnum()) {
                return String.class;
            }
            return ClassUtils.primitiveToWrapper(effectiveType);
        }

        /**
         * Gets whether a value of specific type can be rendered to a Granite-compliant entity
         * @param valueType Annotation's property {@code Class}
         * @return True or false
         */
        private static boolean fits(Class<?> valueType) {
            return valueType.equals(String.class)
                || valueType.equals(Long.class)
                || valueType.equals(Double.class)
                || valueType.equals(Boolean.class);
        }
    }
}
