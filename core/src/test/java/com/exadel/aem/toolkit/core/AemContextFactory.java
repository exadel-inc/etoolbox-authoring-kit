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
package com.exadel.aem.toolkit.core;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;

import com.exadel.aem.toolkit.core.injectors.DelegateInjector;
import com.exadel.aem.toolkit.core.injectors.EnumValueInjector;

public class AemContextFactory {

    public static AemContext newInstance() {
        return newInstance(ResourceResolverType.RESOURCERESOLVER_MOCK);
    }

    public static AemContext newInstance(ResourceResolverType resourceResolverType) {
        return new AemContextBuilder()
            .resourceResolverType(resourceResolverType)
            .beforeSetUp(ctx -> {
                ctx.registerInjectActivateService(new DelegateInjector(null));
                ctx.registerInjectActivateService(new EnumValueInjector());
            })
            .build();
    }
}
