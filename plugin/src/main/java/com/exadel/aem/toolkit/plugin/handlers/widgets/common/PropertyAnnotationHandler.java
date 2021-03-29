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
package com.exadel.aem.toolkit.plugin.handlers.widgets.common;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;

public class PropertyAnnotationHandler implements BiConsumer<Source, Target> {

    @Override
    public void accept(Source source, Target target) {
        Arrays.stream(source.adaptTo(Property[].class))
            .forEach(p -> acceptProperty(p, target));
    }

    private void acceptProperty(Property property, Target target) {
        String propertyName;
        String propertyPath;
        if (property.name().contains(CoreConstants.SEPARATOR_SLASH)) {
            propertyName = StringUtils.substringAfterLast(property.name(), CoreConstants.SEPARATOR_SLASH);
            propertyPath = StringUtils.substringBeforeLast(property.name(), CoreConstants.SEPARATOR_SLASH);
        } else {
            propertyName = property.name();
            propertyPath = null;
        }
        Target effectiveTarget = StringUtils.isNotBlank(propertyPath) ? target.getOrCreateTarget(propertyPath) : target;
        effectiveTarget.attribute(NamingUtil.getValidFieldName(propertyName), property.value());
    }
}
