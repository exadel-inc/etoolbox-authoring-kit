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
package com.exadel.aem.toolkit.plugin.sources;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.plugin.annotations.Metadata;

/**
 * Presents an abstract implementation of {@link Source} that exposes the metadata that is specific for the underlying
 * class member
 */
abstract class MemberSourceImpl extends SourceImpl implements ModifiableMemberSource {

    private Class<?> reportingClass;

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getReportingClass() {
        return this.reportingClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReportingClass(Class<?> value) {
        this.reportingClass = value;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation") // Usage of Multifield#field is retained for compatibility and will be removed after 2.0.2
    @Override
    public Class<?> getValueType() {
        // Retrieve the "immediate" return type
        Class<?> result = getPlainReturnType();
        // Then switch to directly specified type, if any
        if (getDeclaredAnnotation(MultiField.class) != null
            && getDeclaredAnnotation(MultiField.class).value() != _Default.class) {
            result = getDeclaredAnnotation(MultiField.class).value();
        } else if (getDeclaredAnnotation(MultiField.class) != null
            && getDeclaredAnnotation(MultiField.class).field() != _Default.class) {
            result = getDeclaredAnnotation(MultiField.class).field();
        } else if (getDeclaredAnnotation(FieldSet.class) != null
            && getDeclaredAnnotation(FieldSet.class).value() != _Default.class) {
            result = getDeclaredAnnotation(FieldSet.class).value();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSame(Source other) {
        if (!(other instanceof MemberSource)) {
            return super.isSame(other);
        }
        return getDeclaringClass().equals(((MemberSource) other).getDeclaringClass())
            && StringUtils.equals(getName(), other.getName());
    }

    /**
     * Retrieves the return type of the underlying Java class member (field or method). If the class member returns
     * an array value, or a collection, the type of array/collection element is returned.
     * @return Non-null {@code Class} reference
     */
    abstract Class<?> getPlainReturnType();

    /**
     * Gets whether the current class member has a widget annotation - the one with {@code sling:resourceType} specified
     * @return True or false
     */
    boolean isWidgetAnnotationPresent() {
        return Arrays.stream(adaptTo(Annotation[].class))
            .anyMatch(annotation -> {
                Metadata metadata = Metadata.from(annotation);
                ResourceType resourceType = metadata.getAnnotation(ResourceType.class);
                return resourceType != null && StringUtils.isNotBlank(resourceType.value());
            });
    }
}
