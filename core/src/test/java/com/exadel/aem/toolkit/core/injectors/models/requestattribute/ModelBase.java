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
package com.exadel.aem.toolkit.core.injectors.models.requestattribute;

import javax.annotation.Resource;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestAttribute;

@Model(adaptables = {SlingHttpServletRequest.class, Resource.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public abstract class ModelBase<T> {

    protected static final String ATTRIBUTE_VALUE = "value";

    @RequestAttribute(name = ATTRIBUTE_VALUE)
    private Object objectValue;

    private final T constructorValue;

    protected ModelBase(T constructorValue) {
        this.constructorValue = constructorValue;
    }

    public T getConstructorValue() {
        return constructorValue;
    }

    public abstract T getValue();

    public Object getObjectValue() {
        return objectValue;
    }

    public abstract ValueSupplier<T> getValueSupplier();
}
