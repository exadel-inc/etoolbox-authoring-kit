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
package com.exadel.aem.toolkit.plugin.handlers.widgets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.DataSource;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionSource;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.utils.PatternUtil;
import com.exadel.aem.toolkit.plugin.exceptions.ExtensionApiException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.metadata.Metadata;
import com.exadel.aem.toolkit.plugin.metadata.RenderingFilter;
import com.exadel.aem.toolkit.plugin.utils.ArrayUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.StringUtil;

/**
 * Represents a common ancestor for handlers that render the option-providing widgets such as
 * {@link com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup} or
 * {@link com.exadel.aem.toolkit.api.annotations.widgets.select.Select}
 */
abstract class OptionProviderHandler {

    private static final String RESOURCE_TYPE_PREFIX = "/apps/";
    private static final String PN_FALLBACK = "fallback";
    private static final String PROPERTY_IS_FALLBACK = "isFallback";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /* ---------------
       Classic options
       --------------- */

    /**
     * Appends data structure related to {@link DataSource} value to the provided {@link Target}
     * @param dataSource Values provided by a {@code DataSource} annotation
     * @param target     {@code Target} instance to store data in
     */
    void appendDataSourceData(DataSource dataSource, Target target) {
        if (StringUtils.isAnyBlank(dataSource.path(), dataSource.resourceType())) {
            return;
        }
        Target datasourceElement = target.getOrCreateTarget(CoreConstants.NN_DATASOURCE)
            .attribute(CoreConstants.PN_PATH, dataSource.path())
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, dataSource.resourceType());
        Arrays.stream(dataSource.properties())
            .forEach(property -> datasourceElement.attribute(property.name(), property.value()));
    }

    /**
     * Adds a particular option to the given {@code Target}
     * @param option        {@code Annotation} object representing a selection option
     * @param optionValue   A string that exposes the value of the option
     * @param parentElement {@code Target} instance to store the option in
     */
    void appendOption(Annotation option, String optionValue, Target parentElement) {
        Object label = Metadata.from(option).hasProperty(CoreConstants.PN_TEXT)
            ? Metadata.from(option).getValue(CoreConstants.PN_TEXT)
            : null;
        if (label != null && label.toString().isEmpty()) {
            return;
        }
        List<Target> existing = parentElement.findChildren(t -> t.getAttribute(CoreConstants.PN_VALUE).equals(optionValue));
        Target item = existing.isEmpty()
            ? parentElement.createTarget(DialogConstants.DOUBLE_QUOTE + optionValue + DialogConstants.DOUBLE_QUOTE)
            : parentElement.getTarget(DialogConstants.DOUBLE_QUOTE + optionValue + DialogConstants.DOUBLE_QUOTE);
        item.attributes(option, new RenderingFilter(option));
    }

    /* ----------------------
       OptionProvider options
       ---------------------- */

    /**
     * Gets whether the given {@link OptionProvider} contains one or more path settings to be rendered
     * @param optionProvider Values provided by a {@code OptionProvider} annotation
     * @return True or false
     */
    boolean hasProvidedOptions(OptionProvider optionProvider) {
        boolean hasExternalOptions = ArrayUtils.isNotEmpty(optionProvider.value())
            && Arrays.stream(optionProvider.value()).anyMatch(optionSource ->
            StringUtils.isNotBlank(optionSource.value()) || !optionSource.enumeration().equals(_Default.class));
        boolean hasPrependedOptions = ArrayUtils.isNotEmpty(optionProvider.prepend())
            && Arrays.stream(optionProvider.prepend()).anyMatch(StringUtils::isNotEmpty);
        boolean hasAppendedOptions = ArrayUtils.isNotEmpty(optionProvider.append())
            && Arrays.stream(optionProvider.append()).anyMatch(StringUtils::isNotEmpty);
        return hasExternalOptions || hasPrependedOptions || hasAppendedOptions;
    }

    /**
     * Appends data structure related to the {@link OptionProvider} value to the provided {@link Target}
     * @param optionProvider Values provided by the {@code OptionProvider} annotation
     * @param target         The {@code Target} instance to store data in
     */
    void appendOptionProvider(OptionProvider optionProvider, Target target) {
        Target datasourceElement = target
            .getOrCreateTarget(CoreConstants.NN_DATASOURCE)
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, RESOURCE_TYPE_PREFIX + ResourceTypes.OPTION_PROVIDER)
            .attributes(optionProvider, new RenderingFilter(optionProvider));
        if (ArrayUtils.isNotEmpty(optionProvider.prepend())) {
            datasourceElement.attribute(CoreConstants.PN_PREPEND, ArrayUtil.flatten(optionProvider.prepend()));
        }
        if (ArrayUtils.isNotEmpty(optionProvider.append())) {
            datasourceElement.attribute(CoreConstants.PN_APPEND, ArrayUtil.flatten(optionProvider.append()));
        }

        int pathItemOrdinal = 1;
        List<OptionSource> effectiveSources = Arrays.stream(optionProvider.value())
            .flatMap(OptionProviderHandler::splitMainAndFallback)
            .collect(Collectors.toList());
        for (OptionSource item : effectiveSources) {
            String pathPostfix = optionProvider.value().length > 1
                ? Integer.toString(pathItemOrdinal++)
                : StringUtils.EMPTY;
            populateOptionSourceAttributes(item, datasourceElement, pathPostfix);
        }
    }

    /**
     * Called by {@link OptionProviderHandler#appendOptionProvider(OptionProvider, Target)} to facilitate a new approach
     * to fallback option sources (i.e. every option source must have exactly one path). This method will become
     * obsolete as {@link OptionSource#fallback()} is removed
     * @param original {@code OptionSource} instance
     * @return A stream consisting of either one option source (if the provided original does not have a fallback
     * property) or two sources: for the main path and fallback path respectively
     */
    @SuppressWarnings("deprecation")
    // The {@code fallback} property is deprecated and will be removed in a version after 2.3.0
    private static Stream<OptionSource> splitMainAndFallback(OptionSource original) {
        if (StringUtils.isBlank(original.fallback())) {
            return Stream.of(original);
        }
        Metadata newInstance = Metadata.from(original);
        newInstance.putValue(CoreConstants.PN_VALUE, original.fallback());
        newInstance.putValue(PROPERTY_IS_FALLBACK, true);
        return Stream.of(original, (OptionSource) newInstance);
    }

    /**
     * Called by {@link OptionProviderHandler#appendOptionProvider(OptionProvider, Target)} to store options related to
     * the particular option path into the given datasource {@code Target}
     * @param optionSource Values provided by an {@code OptionSource} member of an {@code OptionProvider} annotation
     * @param element      {@code Target} instance to store data in
     * @param postfix      A value that we append to every attribute name to distinguish it from the others
     */
    private static void populateOptionSourceAttributes(
        OptionSource optionSource,
        Target element,
        String postfix) {

        if (StringUtils.isNotBlank(optionSource.value())) {
            element.attribute(
                CoreConstants.PN_PATH + postfix,
                optionSource.value());
        } else if (!optionSource.enumeration().equals(_Default.class)) {
            element.attribute(
                CoreConstants.PN_PATH + postfix,
                extractOptions(optionSource.enumeration(), optionSource));
        }

        setAttribute(
            StringUtils.isNotBlank(optionSource.textMember()),
            element,
            DialogConstants.PN_TEXT_MEMBER + postfix,
            optionSource.textMember());

        setAttribute(
            StringUtils.isNotBlank(optionSource.valueMember()),
            element,
            DialogConstants.PN_VALUE_MEMBER + postfix,
            optionSource.valueMember());

        setAttribute(
            ArrayUtils.isNotEmpty(optionSource.attributeMembers()),
            element,
            DialogConstants.PN_ATTRIBUTE_MEMBERS + postfix,
            StringUtil.format(optionSource.attributeMembers(), String.class));

        setAttribute(
            ArrayUtils.isNotEmpty(optionSource.attributes()),
            element,
            DialogConstants.PN_ATTRIBUTES + postfix,
            optionSource.attributes());

        setAttribute(
            !optionSource.textTransform().equals(StringTransformation.NONE),
            element,
            DialogConstants.PN_TEXT_TRANSFORM + postfix,
            optionSource.textTransform().toString().toLowerCase());

        setAttribute(
            !optionSource.valueTransform().equals(StringTransformation.NONE),
            element,
            DialogConstants.PN_VALUE_TRANSFORM + postfix,
            optionSource.valueTransform().toString().toLowerCase());

        if (optionSource.isFallback()) {
            element.attribute(PN_FALLBACK + postfix, true);
        }
    }

    /**
     * Called by {@link OptionProviderHandler#populateOptionSourceAttributes(OptionSource, Target, String)} to set
     * attributes conditionally. This method is designed to provide better readability of code
     * @param condition Whether to set the attribute value to the given element
     * @param element   {@code Target} instance to store data in
     * @param name      A string representing the name of the stored attribute
     * @param value     A string representing the value of the attribute
     */
    private static void setAttribute(
        boolean condition,
        Target element,
        String name,
        String value) {
        if (condition) {
            element.attribute(name, value);
        }
    }

    /**
     * Called by {@link OptionProviderHandler#populateOptionSourceAttributes(OptionSource, Target, String)} to set
     * attributes conditionally. This method is designed to provide better readability of code
     * @param condition Whether to set the attribute value to the given element
     * @param element   {@code Target} instance to store data in
     * @param name      A string representing the name of the stored attribute
     * @param value     An array of strings representing the value of the attribute
     */
    private static void setAttribute(
        boolean condition,
        Target element,
        String name,
        String[] value) {
        if (condition) {
            element.attribute(name, value);
        }
    }

    /* ------------------------
       Extracting by reflection
       ------------------------ */

    /**
     * Called by {@link OptionProviderHandler#populateOptionSourceAttributes(OptionSource, Target, String)} to
     * facilitate extracting option values via Java class reflection and storing them into a JSON array structure
     * @param type         {@code Class} object that we use as the source of options
     * @param optionSource {@link OptionSource} object that contains info on {@code textMember} and {@code valueMember}
     * @return A non-null string representing a JSON array
     */
    private static String extractOptions(Class<?> type, OptionSource optionSource) {
        if (type.isEnum()) {
            //noinspection unchecked
            return extractOptionsForEnum((Class<? extends Enum<?>>) type, optionSource);
        }
        return extractOptionsForConstantsClass(type, optionSource);
    }

    /**
     * Called by {@link OptionProviderHandler#extractOptions(Class, OptionSource)} as a variant of extraction for
     * {@code Enum} classes
     * @param type         {@code Class} object that we use as the source of options
     * @param optionSource {@link OptionSource} object that contains info on {@code textMember} and {@code valueMember}
     * @return A non-null string representing a JSON array
     */
    private static String extractOptionsForEnum(Class<? extends Enum<?>> type, OptionSource optionSource) {
        String textMember = StringUtils.defaultIfBlank(optionSource.textMember(), CoreConstants.PN_NAME);
        ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
        for (Enum<?> enumConstant : type.getEnumConstants()) {
            Object text = invokeMethodOrInstanceField(enumConstant, textMember);
            if (text == null) {
                continue;
            }
            Object value = StringUtils.isNotBlank(optionSource.valueMember())
                && !DialogConstants.METHOD_TO_STRING.equals(optionSource.valueMember())
                ? invokeMethodOrInstanceField(enumConstant, optionSource.valueMember())
                : enumConstant.toString();
            ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
            objectNode
                .put(
                    StringUtils.defaultIfEmpty(optionSource.textMember(), DialogConstants.PN_JCR_TITLE),
                    text.toString())
                .put(
                    StringUtils.defaultIfEmpty(optionSource.valueMember(), CoreConstants.PN_VALUE),
                    value != null ? value.toString() : StringUtils.EMPTY);
            for (String attributeMember : optionSource.attributeMembers()) {
                Object attributeValue = invokeMethodOrInstanceField(enumConstant, attributeMember);
                if (attributeValue != null) {
                    objectNode.put(
                        attributeMember,
                        attributeValue.toString());
                }
            }
            arrayNode.add(objectNode);
        }
        return StringUtil.escapeArray(arrayNode.toString());
    }

    /**
     * Called by {@link OptionProviderHandler#extractOptions(Class, OptionSource)} as a variant of extraction for
     * regular Java classes that may have constants
     * @param type         {@code Class} object that we use as the source of options
     * @param optionSource {@link OptionSource} object that contains info on {@code textMember} and {@code valueMember}
     * @return A non-null string representing a JSON array
     */
    private static String extractOptionsForConstantsClass(Class<?> type, OptionSource optionSource) {
        Map<String, String> fieldValues = new LinkedHashMap<>();
        for (Field field : type.getDeclaredFields()) {
            boolean isPublicStatic = Modifier.isStatic(field.getModifiers())
                && Modifier.isPublic(field.getModifiers());
            if (!isPublicStatic) {
                continue;
            }
            try {
                Object value = field.get(type);
                fieldValues.put(field.getName(), value != null ? value.toString() : StringUtils.EMPTY);
            } catch (IllegalAccessException e) {
                PluginRuntime.context().getExceptionHandler().handle(new ExtensionApiException(type, e));
            }
        }
        if (!PatternUtil.isPattern(optionSource.textMember())
            || !PatternUtil.isPattern(optionSource.valueMember())) {
            return getPlainOptionsForConstantsClass(fieldValues, optionSource);
        }
        return getMergedOptionsForConstantsClass(fieldValues, optionSource);
    }

    /**
     * Called by {@link OptionProviderHandler#extractOptionsForConstantsClass(Class, OptionSource)} as a variant of
     * extraction for constants that cannot be merged into key-value pairs
     * @param fieldValues  A map containing the relevant constants' names and the respective values
     * @param optionSource {@link OptionSource} object that contains info on {@code textMember} and {@code valueMember}
     * @return A non-null string representing a JSON array
     */
    private static String getPlainOptionsForConstantsClass(
        Map<String, String> fieldValues,
        OptionSource optionSource) {

        ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
        fieldValues
            .entrySet()
            .stream()
            .map(entry -> OBJECT_MAPPER.createObjectNode()
                .put(
                    StringUtils.defaultIfEmpty(optionSource.textMember(), DialogConstants.PN_JCR_TITLE),
                    entry.getKey())
                .put(
                    StringUtils.defaultIfEmpty(optionSource.valueMember(), CoreConstants.PN_VALUE),
                    entry.getValue()))
            .forEach(arrayNode::add);
        return StringUtil.escapeArray(arrayNode.toString());
    }

    /**
     * Called by {@link OptionProviderHandler#extractOptionsForConstantsClass(Class, OptionSource)} as a variant of
     * extraction for constants that allow merging into key-value pairs
     * @param fieldValues  A map containing the relevant constants' names and the respective values
     * @param optionSource {@link OptionSource} object that contains info on {@code textMember} and {@code valueMember}
     * @return A non-null string representing a JSON array
     */
    private static String getMergedOptionsForConstantsClass(
        Map<String, String> fieldValues,
        OptionSource optionSource) {

        Map<String, Pair<String, String>> textValues = fieldValues
            .entrySet()
            .stream()
            .filter(entry -> PatternUtil.isMatch(entry.getKey(), optionSource.textMember()))
            .collect(Collectors.toMap(
                entry -> PatternUtil.strip(entry.getKey(), optionSource.textMember()),
                entry -> Pair.of(entry.getKey(), entry.getValue()),
                (first, second) -> second,
                LinkedHashMap::new));

        fieldValues
            .entrySet()
            .stream()
            .filter(entry -> PatternUtil.isMatch(entry.getKey(), optionSource.valueMember()))
            .forEach(entry -> {
                String valueName = PatternUtil.strip(entry.getKey(), optionSource.valueMember());
                if (textValues.containsKey(valueName)) {
                    textValues.put(
                        valueName,
                        Pair.of(
                            textValues.get(valueName).getRight(),
                            entry.getValue()));
                } else {
                    textValues.put(valueName, Pair.of(entry.getKey(), entry.getValue()));
                }
            });

        ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
        textValues
            .values()
            .stream()
            .map(pair -> OBJECT_MAPPER
                .createObjectNode()
                .put(
                    optionSource.textMember(),
                    pair.getKey())
                .put(
                    optionSource.valueMember(),
                    pair.getValue()))
            .forEach(arrayNode::add);
        return StringUtil.escapeArray(arrayNode.toString());
    }

    /**
     * Retrieves a value from a Java class instance by either invoking a method or accessing a public field with the
     * given name
     * @param value Arbitrary object
     * @param name  Name of the Java class member
     * @return A nullable value
     */
    private static Object invokeMethodOrInstanceField(Object value, String name) {
        Object result = invokeMethod(value, name);
        if (result != null) {
            return result;
        }
        return invokeInstanceField(value, name);
    }

    /**
     * Retrieves a value from a Java class instance by invoking a named method
     * @param value Arbitrary object
     * @param name  Name of the method
     * @return A nullable value
     */
    private static Object invokeMethod(Object value, String name) {
        try {
            return value.getClass().getMethod(name).invoke(value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ExtensionApiException(value.getClass(), e));
            return null;
        } catch (NoSuchMethodException e) {
            // This exception is well possible: no action is needed
            return null;
        }
    }

    /**
     * Retrieves a value from a Java class instance by accessing a public field
     * @param value Arbitrary object
     * @param name  Name of the field
     * @return A nullable value
     */
    private static Object invokeInstanceField(Object value, String name) {
        if (value == null || StringUtils.isBlank(name)) {
            return null;
        }
        Field targetField = Arrays.stream(value.getClass().getDeclaredFields())
            .filter(field -> Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()))
            .filter(field -> StringUtils.equals(field.getName(), name))
            .findFirst()
            .orElse(null);
        if (targetField == null) {
            return null;
        }
        try {
            return targetField.get(value);
        } catch (IllegalAccessException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ExtensionApiException(value.getClass(), e));
        }
        return null;
    }
}
