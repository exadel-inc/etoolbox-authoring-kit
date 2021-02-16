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
package com.exadel.aem.toolkit.api.annotations.layouts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.container.Tab;

/**
 * In multi-tab or multi-panel TouchUI Dialog setup, used to specify on which tab or accordion panel a dialog field is placed
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface Place {
    /**
     * If specified, must contain the string that is equal to a {@link Tab} or an {@link AccordionPanel} title
     * in order to place the current widget in the appropriate container.
     * Skip this value if you don't need any specific placement
     *
     * @return String value
     */
    String in() default "";
}
