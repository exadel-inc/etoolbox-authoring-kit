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
package com.exadel.aem.toolkit.api.annotations.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;

/**
 * Used to override name of annotation property which is equal to {@code Field} name by default, and/or name prefix
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface PropertyName {
    /**
     * Maps to the 'name' attribute of a field node
     * @return String value, non-blank
     */
    String value();

    /**
     * When set to true, allows to override prefix set for this field name
     * (in. e.g. a {@link FieldSet} annotation)
     * @return True or false
     */
    boolean ignorePrefix() default false;
}
