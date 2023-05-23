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
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.optionprovider.services.impl.PathParameters;

/**
 * Implements {@link OptionSourceResolver} to extract the content of Java classes into option data sources
 */
class ClassOptionSourceResolver implements OptionSourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ClassOptionSourceResolver.class);

    static final String EXCEPTION_COULD_NOT_INVOKE = "Could not invoke {}#{}";
    private static final String EXCEPTION_CLASS_NOT_FOUND = "Could not retrieve a class by the name {}";

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource resolve(SlingHttpServletRequest request, PathParameters params) {
        BundleContext effectiveBundleContext = request.getAttribute(BundleContext.class.getName()) != null
            ? (BundleContext) request.getAttribute(BundleContext.class.getName())
            : FrameworkUtil.getBundle(ClassOptionSourceResolver.class).getBundleContext();
        Class<?> sourceClass = getClass(effectiveBundleContext, params.getPath());
        if (sourceClass == null) {
            LOG.error(EXCEPTION_CLASS_NOT_FOUND, params.getPath());
            return null;
        }
        if (sourceClass.isEnum()) {
            return new EnumResolverHelper(sourceClass).resolve(request);
        }
        return new ConstantsResolverHelper(sourceClass, params).resolve(request);
    }

    /**
     * Attempts to retrieve a class by name from any of the available OSGi bundles
     * @param context {@link BundleContext} instance
     * @param name    A fully qualified name of the class
     * @return A nullable {@code Class} reference
     */
    private static Class<?> getClass(BundleContext context, String name) {
        Stream<Bundle> bundles = ArrayUtils.isNotEmpty(context.getBundles())
            ? Arrays.stream(context.getBundles())
            : Stream.of(context.getBundle());
        return bundles
            .filter(Objects::nonNull)
            .map(bundle -> getClass(bundle, name))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * Attempts to retrieve a class by name from the given OSGi bundle
     * @param bundle The current {@link Bundle}
     * @param name   A fully qualified name of the class
     * @return A nullable {@code Class} reference
     */
    private static Class<?> getClass(Bundle bundle, String name) {
        try {
            return bundle.loadClass(name);
        } catch (ClassNotFoundException e) {
            return null; // Not an exception because a class with the name can be missing in an arbitrary bundle
        }
    }
}
