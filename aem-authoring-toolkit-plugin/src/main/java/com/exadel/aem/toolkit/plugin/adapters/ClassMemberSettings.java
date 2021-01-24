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
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.api.markers._Super;

public class ClassMemberSettings {

    private ClassMember wrappedClassMember;
    private ClassField wrappedClassField;

    private Class<?> overridingSource;
    private String overridingName;

    public ClassMemberSettings(ClassMember wrappedClassMember) {
        this.wrappedClassMember = wrappedClassMember;
    }

    public ClassMemberSettings(ClassField wrappedClassField) {
        this.wrappedClassField = wrappedClassField;
    }

    public Class<?> source() {
        if (overridingSource != null) {
            return overridingSource;
        }
        if (wrappedClassMember != null) {
            return wrappedClassMember.source();
        }
        return (wrappedClassField != null) ? wrappedClassField.source() : _Default.class;
    }

    public String name() {
        if (StringUtils.isNotBlank(overridingName)) {
            return overridingName;
        }
        if (wrappedClassMember != null) {
            return wrappedClassMember.name();
        }
        return (wrappedClassField != null) ? wrappedClassField.field() : StringUtils.EMPTY;
    }

    public ClassMemberSettings populateDefaults(Class<?> source) {
        return populateDefaults(source, null);
    }

    public ClassMemberSettings populateDefaults(Class<?> source, String name) {
        if (this.overridingSource == null && this.source().equals(_Default.class)) {
            this.overridingSource = source;
        } else if (this.overridingSource == null && this.source().equals(_Super.class)) {
            this.overridingSource = source.getSuperclass() != null ? source.getSuperclass() : source;
        }
        if (StringUtils.isNotBlank(name) && StringUtils.isBlank(this.name())) {
            this.overridingName = name;
        }
        return this;
    }

    public boolean matches(Source source) {
        return source().equals(source.getDeclaringClass())
            && name().equals(source.getName());
    }

}
