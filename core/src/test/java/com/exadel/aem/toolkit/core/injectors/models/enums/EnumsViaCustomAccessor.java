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
public class EnumsViaCustomAccessor extends RequestAdapterBase<Colors> {

    private static final String PN_CUSTOM_STRING = "customString";
    private static final String PN_CUSTOM_INT = "customInt";
    static final String ACCESSOR_HEX_VALUE = "hexValue";
    static final String ACCESSOR_INT_VALUE = "getIntValue";

    @EnumValue(name = PN_CUSTOM_STRING, valueMember = ACCESSOR_HEX_VALUE)
    @Named()
    private Colors value;

    @EnumValue
    @Default(values = ColorConstants.VALUE_RED)
    private Colors defaultValue;

    @Self
    private Supplier supplier;

    private final Colors constructorValue;

    @Inject
    public EnumsViaCustomAccessor(@EnumValue(valueMember = ACCESSOR_INT_VALUE) @Named(PN_CUSTOM_INT) Colors value) {
        this.constructorValue = value;
    }

    @Nullable
    public Colors getValue() {
        return value;
    }

    @Override
    public Colors getDefaultValue() {
        return defaultValue;
    }

    @Nullable
    public Colors getConstructorValue() {
        return constructorValue;
    }

    @Override
    public ValueSupplier<Colors> getValueSupplier() {
        return supplier;
    }

    @Model(
        adaptables = {SlingHttpServletRequest.class, Resource.class},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public interface Supplier extends ValueSupplier<Colors> {
        @EnumValue(valueMember = ACCESSOR_HEX_VALUE)
        @Named(PN_CUSTOM_STRING)
        @Override
        Colors getValue();
    }
}
