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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestAttribute;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class StringCollections extends ModelBase<Collection<String>> {

    @RequestAttribute
    private Set<String> value;

    @Self
    private Supplier valueSupplier;

    @Inject
    public StringCollections(@RequestAttribute @Named(ATTRIBUTE_VALUE) List<String> value) {
        super(value);
    }

    @Override
    public ValueSupplier<Collection<String>> getValueSupplier() {
        return valueSupplier;
    }

    @Override
    public Collection<String> getValue() {
        return value;
    }

    @Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public interface Supplier extends ValueSupplier<Collection<String>> {
        @RequestAttribute(name = "value")
        @Override
        List<String> getValue();
    }
}