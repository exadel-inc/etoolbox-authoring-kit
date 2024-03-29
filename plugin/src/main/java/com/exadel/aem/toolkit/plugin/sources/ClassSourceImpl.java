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

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.main.Setting;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.metadata.scripting.DataStack;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;

/**
 * Initializes a {@link Source} instance referencing the managed Java class
 */
class ClassSourceImpl extends SourceImpl {

    private final Class<?> value;

    /**
     * Initializes a class instance storing a reference to the {@code Class} that serves as the metadata source
     * @param value The metadata source
     */
    ClassSourceImpl(Class<?> value) {
        super(value);
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return isValid() ? value.getName() : StringUtils.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return value != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T adaptTo(Class<T> type) {
        if (Class.class.equals(type)) {
            return type.cast(value);
        }
        return super.adaptTo(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    <T extends Annotation> T getAnnotation(Class<T> type) {
        return value.getDeclaredAnnotation(type);
    }

    /**
     * {@inheritDoc}
     * This implementation honors the {@link Setting} entries attached to superclasses and interfaces of the current class
     */
    @Override
    DataStack getDataStack() {
        DataStack result = new DataStack();
        for (Class<?> ancestor : ClassUtil.getInheritanceTree(value, false)) {
            result.append(ancestor.getAnnotationsByType(Setting.class));
        }
        result.append(adaptTo(Setting[].class));
        return result;
    }
}
