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
package com.exadel.aem.toolkit.api.annotations.widgets.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Represents a name-value string pair used to populate {@code granite:data} subnode of the dialog field and,
 * accordingly, to create {@code data-} attributes in dialog markup, or else to preserve variables for the scripting
 * support
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DataCollection.class)
public @interface Data {

    /**
     * Name of the data entry
     * @return String value, non-blank
     */
    @ValueRestriction(ValueRestrictions.NOT_BLANK)
    String name();

    /**
     * Attribute value
     * @return String value
     */
    String value();

    /**
     * If set to {@code true}, the value is the {@code granite:data} subnode of the dialog field and renders to a {@code
     * data-} attribute of the correspondent HTML tag. If set to {@code false}, the value is only effective for the
     * scripting support
     * @return True or false
     */
    boolean persist() default true;
}
