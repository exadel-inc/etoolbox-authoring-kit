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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.DataSource;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionSource;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.StringUtil;

/**
 * Represents a common ancestor for handlers that render rendering the option-providing widgets
 * such as {@link com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup} or {@link com.exadel.aem.toolkit.api.annotations.widgets.select.Select}
 */
abstract class OptionProviderHandler {

    private static final String RESOURCE_TYPE_PREFIX = "/apps/";

    /**
     * Gets whether the given {@link OptionProvider} contains one or more path settings to be rendered
     * @param optionProvider Values provided by a {@code OptionProvider} annotation
     * @return True or false
     */
    boolean hasProvidedOptions(OptionProvider optionProvider) {
        return ArrayUtils.isNotEmpty(optionProvider.value())
            && Arrays.stream(optionProvider.value()).anyMatch(source -> StringUtils.isNotBlank(source.value()));
    }

    /**
     * Adds a particular option represented by an annotation in a {@code Select}'s or {@code RadioGroup}'s option set
     * to the given {@code Target}
     * @param option        {@code Annotation} object representing a selection option
     * @param optionValue   String that exposes value of the option
     * @param parentElement {@code Target} instance to store the option in
     */
    void appendOption(Annotation option, String optionValue, Target parentElement) {
        List<Target> existing = parentElement.findChildren(t -> t.getAttribute(CoreConstants.PN_VALUE).equals(optionValue));
        Target item = existing.isEmpty()
            ? parentElement.createTarget(DialogConstants.DOUBLE_QUOTE + optionValue + DialogConstants.DOUBLE_QUOTE)
            : parentElement.getTarget(DialogConstants.DOUBLE_QUOTE + optionValue + DialogConstants.DOUBLE_QUOTE);
        item.attributes(option, AnnotationUtil.getPropertyMappingFilter(option));
    }

    /**
     * Appends data structure related to {@link OptionProvider} value to the provided {@link Target}
     * @param optionProvider Values provided by a {@code OptionProvider} annotation
     * @param target         {@code Target} instance to store data in
     */
    void appendOptionProvider(OptionProvider optionProvider, Target target) {
        if (!hasProvidedOptions(optionProvider)) {
            return;
        }
        Target datasourceElement = target
            .getOrCreateTarget(DialogConstants.NN_DATASOURCE)
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, RESOURCE_TYPE_PREFIX + ResourceTypes.OPTION_PROVIDER)
            .attributes(optionProvider, AnnotationUtil.getPropertyMappingFilter(optionProvider));

        int pathItemOrdinal = 1;
        for (OptionSource item : optionProvider.value()) {
            String pathPostfix = optionProvider.value().length > 1
                ? Integer.toString(pathItemOrdinal++)
                : StringUtils.EMPTY;
            populateSourceAttributes(item, datasourceElement, pathPostfix);
        }
    }

    /**
     * Appends data structure related to {@link DataSource} value to the provided {@link Target}
     * @param dataSource Values provided by a {@code DataSource} annotation
     * @param target     {@code Target} instance to store data in
     */
    void appendDataSource(DataSource dataSource, Target target) {
        if (StringUtils.isAnyBlank(dataSource.path(), dataSource.resourceType())) {
            return;
        }
        Target datasourceElement = target.getOrCreateTarget(DialogConstants.NN_DATASOURCE)
            .attribute(CoreConstants.PN_PATH, dataSource.path())
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, dataSource.resourceType());
        Arrays.stream(dataSource.properties())
            .forEach(property -> datasourceElement.attribute(property.name(), property.value()));
    }

    /**
     * Called by {@link OptionProviderHandler#appendOptionProvider(OptionProvider, Target)} to store options related
     * to the particular option path into the given datasource {@code Target}
     * @param optionSource      Values provided by an {@code OptionSource} member of an {@code OptionProvider} annotation
     * @param datasourceElement {@code Target} instance to store data in
     * @param postfix           Special key added to every attribute name to distinguish it from the others
     */
    private static void populateSourceAttributes(OptionSource optionSource, Target datasourceElement, String postfix) {
        datasourceElement.attribute(CoreConstants.PN_PATH + postfix, optionSource.value());
        if (StringUtils.isNotBlank(optionSource.fallback())) {
            datasourceElement.attribute(DialogConstants.PN_FALLBACK_PATH + postfix, optionSource.fallback());
        }
        if (StringUtils.isNotBlank(optionSource.textMember())) {
            datasourceElement.attribute(DialogConstants.PN_TEXT_MEMBER + postfix, optionSource.textMember());
        }
        if (StringUtils.isNotBlank(optionSource.valueMember())) {
            datasourceElement.attribute(DialogConstants.PN_VALUE_MEMBER + postfix, optionSource.valueMember());
        }
        if (ArrayUtils.isNotEmpty(optionSource.attributeMembers())) {
            datasourceElement.attribute(
                DialogConstants.PN_ATTRIBUTE_MEMBERS + postfix,
                StringUtil.format(optionSource.attributeMembers(), String.class));
        }
        if (ArrayUtils.isNotEmpty(optionSource.attributes())) {
            datasourceElement.attribute(DialogConstants.PN_ATTRIBUTES + postfix, optionSource.attributes());
        }
        if (!optionSource.textTransform().equals(StringTransformation.NONE)) {
            datasourceElement.attribute(
                DialogConstants.PN_TEXT_TRANSFORM + postfix,
                optionSource.textTransform().toString().toLowerCase());
        }
        if (!optionSource.valueTransform().equals(StringTransformation.NONE)) {
            datasourceElement.attribute(
                DialogConstants.PN_VALUE_TRANSFORM + postfix,
                optionSource.valueTransform().toString().toLowerCase());
        }
    }
}
