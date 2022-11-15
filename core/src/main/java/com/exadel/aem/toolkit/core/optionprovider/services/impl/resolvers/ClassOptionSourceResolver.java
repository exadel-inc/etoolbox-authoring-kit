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
package com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers;

import java.util.Arrays;
import java.util.Objects;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionSourceResolutionResult;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.PathParameters;

class ClassOptionSourceResolver implements OptionSourceResolver {
    private static final Logger LOG = LoggerFactory.getLogger(ClassOptionSourceResolver.class);

    private Class<?> providedClass;

    public ClassOptionSourceResolver() {
    }

    public ClassOptionSourceResolver(Class<?> providedClass) {
        this.providedClass = providedClass;
    }

    @Override
    public OptionSourceResolutionResult resolve(SlingHttpServletRequest request, PathParameters pathParameters, String uri) {
        Class<?> sourceClass = providedClass != null ? providedClass : getClassInstance(uri);
        if (sourceClass == null) {
            LOG.error("Could not retrieve the class by the name \"{}\"", uri);
            return null;
        }
        if (sourceClass.isEnum()) {
            return new OptionSourceResolutionResult(new EnumResolverCore(sourceClass).resolve(request), pathParameters);
        }
        return new OptionSourceResolutionResult(
            new ConstantsClassResolverCore(sourceClass, pathParameters).resolve(request),
            pathParameters.modify(JcrConstants.JCR_TITLE, CoreConstants.PN_VALUE));
    }

    private static Class<?> getClassInstance(String name) {
        BundleContext bundleContext = FrameworkUtil.getBundle(ClassOptionSourceResolver.class).getBundleContext();
        return Arrays.stream(bundleContext.getBundles())
            .map(bundle -> getClassInstance(bundle, name))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private static Class<?> getClassInstance(Bundle bundle, String name) {
        try {
            return bundle.loadClass(name);
        } catch (ClassNotFoundException ignore) {
            return null;
        }
    }
}
