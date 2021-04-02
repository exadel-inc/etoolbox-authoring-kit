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

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with values originating from a {@link Source}
 * object that define additional properties of a Granite UI component
 */
public class PropertyAnnotationHandler implements BiConsumer<Source, Target> {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        Arrays.stream(source.adaptTo(Property[].class)).forEach(p -> acceptProperty(p, target));
    }

    /**
     * Called by {@link PropertyAnnotationHandler#accept(Source, Target)} to process particular {@link Property} objects
     * extractable from the given {@code Source}
     * @param property Current {@code Property} object
     * @param target   Resulting {@code Target} object
     */
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
