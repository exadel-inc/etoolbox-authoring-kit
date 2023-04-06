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
package com.exadel.aem.toolkit.core.injectors.models.lists;

import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.api.annotations.injectors.EToolboxList;
import com.exadel.aem.toolkit.core.injectors.EToolboxListInjectorTest;
import com.exadel.aem.toolkit.core.injectors.models.RequestAdapterBase;
import com.exadel.aem.toolkit.core.injectors.models.ValueSupplier;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ResourcesMap extends RequestAdapterBase<Map<String, Resource>> {

    @EToolboxList(EToolboxListInjectorTest.SIMPLE_LIST_PATH)
    private Map<String, Resource> value;

    @EToolboxList(EToolboxListInjectorTest.SIMPLE_LIST_PATH)
    private Map<Object, Object> genericValue;

    @Self
    private Supplier supplier;

    @Inject
    public ResourcesMap(@EToolboxList(EToolboxListInjectorTest.SIMPLE_LIST_PATH) @Named Map<String, Resource> constructorValue) {
        super(constructorValue);
    }

    @Override
    @Nullable
    public Map<String, Resource> getValue() {
        return value;
    }

    @Nullable
    public Map<Object, Object> getGenericValue() {
        return genericValue;
    }

    @Override
    public ValueSupplier<Map<String, Resource>> getValueSupplier() {
        return supplier;
    }

    @Model(
        adaptables = {SlingHttpServletRequest.class, Resource.class},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public interface Supplier extends ValueSupplier<Map<String, Resource>> {
        @EToolboxList(EToolboxListInjectorTest.SIMPLE_LIST_PATH)
        @Override
        Map<String, Resource> getValue();
    }
}
