/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/heading/index.html">
 * Heading element</a> in TouchUI dialog
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.HEADING)
@PropertyMapping
public @interface Heading {
    /**
     * Maps to the 'level' attribute of this TouchUI dialog component's node.
     * Number that corresponds to the header tag level {@code <h1>} to {@code <h6>}
     * @return Long value
     */
    @ValueRestriction(value = ValueRestrictions.POSITIVE)
    long level() default 1;

    /**
     * When set to a non-blank string, maps to the 'text' attribute of this TouchUI dialog component's node.
     * Used to define text within header tag.
     * @return String value, non-blank
     * @deprecated Please use {@link Heading#value()}
     */
    @ValueRestriction(value = ValueRestrictions.NOT_BLANK)
    @SuppressWarnings("squid:S1133")
    @Deprecated
    String text();

    /**
     *  When set to a non-blank string, maps to the 'text' attribute of this TouchUI dialog component's node.
     *  Used to define text within header tag.
     *  @return String value, non-blank
     */
    @ValueRestriction(value = ValueRestrictions.NOT_BLANK)
    String value();
}
