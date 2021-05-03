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
package com.exadel.aem.toolkit.plugin.adapters;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.main.ClassField;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.handlers.Adapts;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.api.markers._Super;

/**
 * Implements {@link Adapts} to manage class membership data derived from source annotations
 */
public class ClassMemberSetting {

    private ClassMember wrappedClassMember;
    @SuppressWarnings("deprecation") // ClassField processing is retained for compatibility and will be removed
                                     // in a version after 2.0.2
    private ClassField wrappedClassField;

    private Class<?> overridingSource;
    private String overridingName;

    /**
     * Initialized an instance of {@code ClassMemberSetting} with a reference to a {@link ClassMember} object
     * @param value Non-null {@code ClassMember} object
     */
    public ClassMemberSetting(ClassMember value) {
        this.wrappedClassMember = value;
    }

    /**
     * Initialized an instance of {@code ClassMemberSetting} with a reference to a {@link ClassField} object
     * @param value Non-null {@code ClassField} object
     */
    @SuppressWarnings("deprecation") // ClassField processing is retained for compatibility and will be removed
                                     // in a version after 2.0.2
    public ClassMemberSetting(ClassField value) {
        this.wrappedClassField = value;
    }

    /**
     * Retrieves the {@code source} value associated with this instance. Points to the {@code Class} to which
     * the underlying member belongs
     * @return {@code Class} reference, defaults to the placeholder {@link _Default} class
     */
    public Class<?> getSource() {
        if (overridingSource != null) {
            return overridingSource;
        }
        if (wrappedClassMember != null) {
            return wrappedClassMember.source();
        }
        return (wrappedClassField != null) ? wrappedClassField.source() : _Default.class;
    }

    /**
     * Retrieves the {@code name} value associated with this instance. Exposes the class member name if the instance is
     * properly initialized
     * @return String value; defaults to the empty string
     */
    public String getName() {
        if (StringUtils.isNotBlank(overridingName)) {
            return overridingName;
        }
        if (wrappedClassMember != null) {
            return wrappedClassMember.value();
        }
        return (wrappedClassField != null) ? wrappedClassField.field() : StringUtils.EMPTY;
    }

    /**
     * Used to swap the fallback value received upon the instance initialization with the provided specific value. Calls
     * to this method can be chained
     * @param source Non-null {@code Class} reference
     * @return The current instance
     */
    public ClassMemberSetting populateDefaults(Class<?> source) {
        return populateDefaults(source, null);
    }

    /**
     * Used to swap the fallback values received upon the instance initialization with the provided specific values. Calls
     * to this method can be chained
     * @param source Non-null {@code Class} reference
     * @param name   Member name, non-blank string
     * @return The current instance
     */
    public ClassMemberSetting populateDefaults(Class<?> source, String name) {
        if (this.overridingSource == null && this.getSource().equals(_Default.class)) {
            this.overridingSource = source;
        } else if (this.overridingSource == null && this.getSource().equals(_Super.class)) {
            this.overridingSource = source.getSuperclass() != null ? source.getSuperclass() : source;
        }
        if (StringUtils.isNotBlank(name) && StringUtils.isBlank(this.getName())) {
            this.overridingName = name;
        }
        return this;
    }

    /**
     * Gets whether the current instance corresponds to the given source because exposing the same class reference
     * and member name
     * @param source Non-null {@code Source} reference
     * @return True or false
     */
    public boolean matches(Source source) {
        return getSource().equals(source.adaptTo(MemberSource.class).getDeclaringClass())
            && getName().equals(source.getName());
    }

}
