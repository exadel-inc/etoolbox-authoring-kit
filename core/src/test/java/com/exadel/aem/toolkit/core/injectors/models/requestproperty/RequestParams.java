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

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.RequestProperty;
import com.exadel.aem.toolkit.core.injectors.models.ValueSupplier;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class RequestParams {

    @RequestProperty
    private RequestParameter value;

    @RequestProperty(name = CoreConstants.PN_VALUE)
    private List<RequestParameter> collectionValue;

    @RequestProperty(name = CoreConstants.PN_VALUE)
    private Set<RequestParameter> setValue;

    @Self
    private Supplier supplier;

    private final RequestParameter[] constructorValue;

    @Inject
    public RequestParams(@RequestProperty @Named(CoreConstants.PN_VALUE) RequestParameter[] value) {
        this.constructorValue = value;
    }

    @Nullable
    public RequestParameter getValue() {
        return value;
    }

    @Nullable
    public List<RequestParameter> getCollectionValue() {
        return collectionValue;
    }

    @Nullable
    public Set<RequestParameter> getSetValue() {
        return setValue;
    }

    public RequestParameter[] getConstructorValue() {
        return constructorValue;
    }

    public ValueSupplier<RequestParameterMap> getValueSupplier() {
        return supplier;
    }

    @Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public interface Supplier extends ValueSupplier<RequestParameterMap> {
        @RequestProperty(name = CoreConstants.PN_VALUE)
        @Override
        RequestParameterMap getValue();
    }
}
