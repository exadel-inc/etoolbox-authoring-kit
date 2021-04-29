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
package com.exadel.aem.toolkit.api.handlers;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;

/**
 * Extends the {@code Source} interface with methods specific for sources based on Java classes' fields and methods
 * @see Source
 */
public interface MemberSource extends Source {

    /**
     * Retrieves the class the underlying Java field or method is declared in
     * @return Non-null {@code Class} reference
     */
    Class<?> getDeclaringClass();

    /**
     * Retrieves the class the underlying Java field or method is received from. This value can be similar to that of
     * {@link MemberSource#getDeclaringClass()}. Or else it can differ when the underlying member is declared in a
     * superclass or in an interface the current class is implementing
     * @return Non-null {@code Class} reference
     */
    Class<?> getReportingClass();

    /**
     * Retrieves the return type of the underlying Java class member (field or method). If the class member returns
     * an array value or a collection, the type of array/collection element is returned.
     * <p>If the class member has an attached annotation that can present an alternative return type for the need
     * of proper Granite UI component rendering (such as {@link MultiField} or {@link FieldSet}), its value is respected</p>
     * @return Non-null {@code Class} reference
     */
    Class<?> getValueType();
}
