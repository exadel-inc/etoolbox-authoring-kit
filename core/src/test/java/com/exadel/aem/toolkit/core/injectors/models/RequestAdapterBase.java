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
package com.exadel.aem.toolkit.core.injectors.models;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.RequestProperty;

public abstract class RequestAdapterBase<T> {

    @RequestProperty(name = CoreConstants.PN_VALUE)
    // Note: this value is not used in models for {@code EToolboxListInjector}
    private Object objectValue;

    @RequestProperty(name = CoreConstants.PN_VALUE)
    private String stringValue;

    private final T constructorValue;

    protected RequestAdapterBase(T constructorValue) {
        this.constructorValue = constructorValue;
    }

    public RequestAdapterBase() {
        this(null);
    }

    public T getConstructorValue() {
        return constructorValue;
    }

    public abstract T getValue();

    // Note: this method is not used in models covering complex reference types such as {@link Calendar},
    // {@link SimpleListItem}, or a {@link Map}
    public abstract T getDefaultValue();

    @Nullable
    public Object getObjectValue() {
        return objectValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    // Note: this method is only tested in a selected of descendants
    public String getDefaultStringValue() {
        return StringUtils.EMPTY;
    }

    public abstract ValueSupplier<T> getValueSupplier();
}
