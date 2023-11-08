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
package com.exadel.aem.toolkit.core.injectors.models.requestproperty;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.RequestProperty;
import com.exadel.aem.toolkit.core.injectors.models.RequestAdapterBase;
import com.exadel.aem.toolkit.core.injectors.models.ValueSupplier;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class LongArrays extends RequestAdapterBase<Long[]> {

    @RequestProperty
    private long[] value;

    @RequestProperty
    @Default(longValues = {10L, 11L, 12L})
    private long[] defaultValue;

    @Self
    private Supplier supplier;

    @Inject
    public LongArrays(@RequestProperty @Named(CoreConstants.PN_VALUE) Long[] value) {
        super(value);
    }

    public Long[] getValue() {
        return ArrayUtils.toObject(value);
    }

    public long[] getDefaultValue() {
        return defaultValue;
    }

    @Override
    public ValueSupplier<Long[]> getValueSupplier() {
        return supplier;
    }

    @Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public interface Supplier extends ValueSupplier<Long[]> {
        @RequestProperty(name = CoreConstants.PN_VALUE)
        @Override
        Long[] getValue();
    }
}
