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

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.adapters.AdaptationBase;

/**
 * Implements {@link Source} to expose the metadata that is specific for the underlying annotation
 */
public class AnnotationSourceImpl extends AdaptationBase<Source> implements Source {
    private final Annotation value;

    /**
     * Initializes a class instance storing a reference to the {@code Annotation} object that serves as the metadata source
     */
    public AnnotationSourceImpl(Annotation value) {
        super(Source.class);
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return value != null ? value.annotationType().getName() : StringUtils.EMPTY;
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
    public <A> A adaptTo(Class<A> adaptation) {
        if (adaptation == null) {
            return null;
        }
        if (adaptation.isAnnotation() && value != null && adaptation.equals(value.annotationType())) {
            return adaptation.cast(value);
        }
        return super.adaptTo(adaptation);
    }
}
