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

import java.util.Calendar;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.RequestProperty;
import com.exadel.aem.toolkit.core.injectors.models.RequestAdapterBase;
import com.exadel.aem.toolkit.core.injectors.models.ValueSupplier;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CalendarArrays extends RequestAdapterBase<Calendar[]> {

    @RequestProperty
    private Cloneable[] value;

    @Self
    private Supplier supplier;

    @Inject
    public CalendarArrays(@RequestProperty @Named(CoreConstants.PN_VALUE) Calendar[] value) {
        super(value);
    }

    @Override
    public Calendar[] getValue() {
        assert value != null;
        Calendar[] result = new Calendar[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = (Calendar) value[i];
        }
        return result;
    }

    @Override
    public Calendar[] getDefaultValue() {
        return null;
    }

    @Override
    public ValueSupplier<Calendar[]> getValueSupplier() {
        return supplier;
    }

    @Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public interface Supplier extends ValueSupplier<Calendar[]> {
        @RequestProperty(name = CoreConstants.PN_VALUE)
        @Override
        Calendar[] getValue();
    }
}
