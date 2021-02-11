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

package com.exadel.aem.toolkit.plugin.handlers.widget;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionSource;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginAnnotationUtility;

abstract class OptionProviderHandler {

    private static final String RESOURCE_TYPE_PREFIX = "/apps/";

    void appendOptionProvider(OptionProvider optionProvider, Target target) {
        if (!hasProvidedOptions(optionProvider)) {
            return;
        }
        Target datasource = target
            .getOrCreateTarget(DialogConstants.NN_DATASOURCE)
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, RESOURCE_TYPE_PREFIX + ResourceTypes.OPTION_PROVIDER)
            .attributes(optionProvider, PluginAnnotationUtility.getPropertyMappingFilter(optionProvider));

        int pathItemOrdinal = 1;
        for (OptionSource item : optionProvider.sources()) {
            String pathPostfix = optionProvider.sources().length > 1
                ? Integer.toString(pathItemOrdinal++)
                : StringUtils.EMPTY;
            populateSourceAttributes(datasource, item, pathPostfix);
        }
    }

    static boolean hasProvidedOptions(OptionProvider optionProvider) {
        return ArrayUtils.isNotEmpty(optionProvider.sources())
            && Arrays.stream(optionProvider.sources()).anyMatch(source -> StringUtils.isNotBlank(source.path()));
    }

    private static void populateSourceAttributes(Target datasource, OptionSource optionSource, String postfix) {
        datasource.attribute(DialogConstants.PN_PATH + postfix, optionSource.path());
        if (StringUtils.isNotBlank(optionSource.fallbackPath())) {
            datasource.attribute(DialogConstants.PN_FALLBACK_PATH + postfix, optionSource.fallbackPath());
        }
        if (StringUtils.isNotBlank(optionSource.textMember())) {
            datasource.attribute(DialogConstants.PN_TEXT_MEMBER + postfix, optionSource.textMember());
        }
        if (StringUtils.isNotBlank(optionSource.valueMember())) {
            datasource.attribute(DialogConstants.PN_VALUE_MEMBER + postfix, optionSource.valueMember());
        }
        if (ArrayUtils.isNotEmpty(optionSource.attributes())) {
            datasource.attribute(DialogConstants.PN_ATTRIBUTES + postfix, optionSource.attributes());
        }
        if (StringUtils.isNotBlank(optionSource.textTransform())) {
            datasource.attribute(DialogConstants.PN_TEXT_TRANSFORM + postfix, optionSource.textTransform());
        }
        if (StringUtils.isNotBlank(optionSource.valueTransform())) {
            datasource.attribute(DialogConstants.PN_VALUE_TRANSFORM + postfix, optionSource.valueTransform());
        }
    }
}
