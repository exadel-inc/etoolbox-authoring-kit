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
package com.exadel.aem.toolkit.api.annotations.main;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;
import com.exadel.aem.toolkit.api.markers._Default;

/**
 * Used to refer a particular member (field or method) of a class defined by the class reference and name
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassMember {

    /**
     * The Java class possessing the member. If not specified, the class currently processed by the ToolKit's Maven
     * plugin will be used
     * @return {@code Class<?>} instance
     */
    Class<?> source() default _Default.class;

    /**
     * When set to a non-blank String, defines the name of the member, must refer to an actual field or method name.
     * Otherwise, name of the current field will be used
     * @return String value
     */
    @ValueRestriction(ValueRestrictions.NOT_BLANK)
    String value() default "";
}
