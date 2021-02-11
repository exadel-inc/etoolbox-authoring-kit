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
            String pathSuffix = optionProvider.sources().length > 1 ? Integer.toString(pathItemOrdinal++) : StringUtils.EMPTY;
            datasource.attribute(DialogConstants.PN_PATH + pathSuffix, item.path());
            if (StringUtils.isNotBlank(item.fallbackPath())) {
                datasource.attribute(DialogConstants.PN_FALLBACK_PATH + pathSuffix, item.fallbackPath());
            }
            if (StringUtils.isNotBlank(item.textMember())) {
                datasource.attribute(DialogConstants.PN_TEXT_MEMBER + pathSuffix, item.textMember());
            }
            if (StringUtils.isNotBlank(item.valueMember())) {
                datasource.attribute(DialogConstants.PN_VALUE_MEMBER + pathSuffix, item.valueMember());
            }
            if (ArrayUtils.isNotEmpty(item.attributes())) {
                datasource.attribute(DialogConstants.PN_ATTRIBUTES + pathSuffix, item.attributes());
            }
            if (StringUtils.isNotBlank(item.textTransform())) {
                datasource.attribute(DialogConstants.PN_TEXT_TRANSFORM + pathSuffix, item.textTransform());
            }
            if (StringUtils.isNotBlank(item.valueTransform())) {
                datasource.attribute(DialogConstants.PN_VALUE_TRANSFORM + pathSuffix, item.valueTransform());
            }
        }
    }

    static boolean hasProvidedOptions(OptionProvider optionProvider) {
        return ArrayUtils.isNotEmpty(optionProvider.sources())
            && Arrays.stream(optionProvider.sources()).anyMatch(source -> StringUtils.isNotBlank(source.path()));
    }
}
