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
package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Orientation;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.SLIDER)
@AnnotationRendering(properties = "all")
public @interface Slider {

    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    long min() default 0;

    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    long max() default 100;

    @ValueRestriction(ValueRestrictions.POSITIVE)
    long step() default 1;

    Orientation orientation() default Orientation.HORIZONTAL;

    boolean filled() default false;

    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    long value() default 0;
}
