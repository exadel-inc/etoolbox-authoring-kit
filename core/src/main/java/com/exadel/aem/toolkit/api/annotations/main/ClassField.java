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

import com.exadel.aem.toolkit.api.markers._Default;

/**
 * Used to refer to a particular field of a class defined by the class reference and the name
 * @deprecated This is deprecated and will be removed in a version after 2.0.2. Please use {@link ClassMember} instead
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
@SuppressWarnings({"squid:S1133", "DeprecatedIsStillUsed"}) // the processing of ClassField is retained for compatibility
                                                            // and will be removed in a version after 2.0.2
public @interface ClassField {

    /**
     * The Java class possessing the member. If not specified, the class currently processed by the ToolKit Maven plugin
     * will be used
     * @return {@code Class<?>} instance
     */
    Class<?> source() default _Default.class;

    /**
     * Name of the field
     * @return String value, non-blank
     */
    String field();
}
