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
package com.exadel.aem.toolkit.core.injectors.models.enums;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.api.annotations.injectors.EnumValue;
import com.exadel.aem.toolkit.core.injectors.models.RequestAdapterBase;
import com.exadel.aem.toolkit.core.injectors.models.ValueSupplier;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class EnumCollections extends RequestAdapterBase<Collection<Colors>> {

    static final String PN_ARRAY_VALUE = "arrayValue";

    @EnumValue
    private Collection<Colors> arrayValue;

    @EnumValue
    @Default(values = {ColorConstants.VALUE_YELLOW, ColorConstants.VALUE_ORANGE})
    private Collection<Colors> defaultValue;

    @Self
    private Supplier supplier;

    private final Set<Colors> constructorValue;

    @Inject
    public EnumCollections(@EnumValue @Named(PN_ARRAY_VALUE) Set<Colors> arrayValue) {
        this.constructorValue = arrayValue;
    }

    @Override
    @Nullable
    public Collection<Colors> getValue() {
        return arrayValue;
    }

    @Override
    public Collection<Colors> getDefaultValue() {
        return defaultValue;
    }

    @Nullable
    public Collection<Colors> getConstructorValue() {
        return constructorValue;
    }

    @Override
    public ValueSupplier<Collection<Colors>> getValueSupplier() {
        return supplier;
    }

    @Model(
        adaptables = {SlingHttpServletRequest.class, Resource.class},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public interface Supplier extends ValueSupplier<Collection<Colors>> {
        @EnumValue(name = PN_ARRAY_VALUE)
        @Override
        List<Colors> getValue();
    }
}
