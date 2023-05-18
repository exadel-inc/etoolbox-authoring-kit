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

import java.net.MalformedURLException;
import java.net.URL;
import javax.lang.model.SourceVersion;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.PathParameters;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

/**
 * Contains methods for resolving provided resource identifiers (representing JCR paths, HTTP endpoints, or class names,
 * etc.) to option data sources
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 * @see OptionProviderService
 * @see OptionSourceResolver
 */
public class OptionSourceResolvers {
    private static final OptionSourceResolver CLASS_RESOLVER = new ClassOptionSourceResolver();
    private static final OptionSourceResolver HTTP_RESOLVER = new HttpOptionSourceResolver();
    private static final OptionSourceResolver INLINE_RESOLVER = new InlineOptionSourceResolver();
    private static final OptionSourceResolver JCR_RESOLVER = new JcrOptionSourceResolver();

    /**
     * Default (instantiation-preventing) constructor
     */
    private OptionSourceResolvers() {
    }

    /* ----------
       Resolution
       ---------- */

    /**
     * Extracts a resource identifier from the provided request and attempts to retrieve an option datasource by picking
     * up one of the available source resolvers
     * @param request {@link SlingHttpServletRequest} instance. May contain a predefined option source resolver as a
     *                request attribute
     * @param params  {@link PathParameters} object containing the path to resolve as well as the values that affect the
     *                resolution routine
     * @return {@link Resource} instance, or else {@code null}
     */
    public static Resource resolve(SlingHttpServletRequest request, PathParameters params) {
        OptionSourceResolver predefinedResolver =
            (OptionSourceResolver) request.getAttribute(OptionSourceResolver.class.getName());
        OptionSourceResolver effectiveResolver = ObjectUtils.firstNonNull(
            predefinedResolver,
            getResolver(params));

        return effectiveResolver.resolve(request, params);
    }

    /* ---------------------
       Picking up a resolver
       --------------------- */

    /**
     * Retrieves an appropriate {@link OptionSourceResolver} based on the {@code params} provided
     * @param params {@link PathParameters} object containing the path to resolve as well as the values that affect the
     *               resolution routine
     * @return {@code OptionSourceResolver} instance
     */
    private static OptionSourceResolver getResolver(PathParameters params) {
        if (isJson(params.getPath())) {
            return INLINE_RESOLVER;
        }
        if (isClassName(params.getPath())) {
            return CLASS_RESOLVER;
        }
        if (isUrl(params.getPath())) {
            return HTTP_RESOLVER;
        }
        return JCR_RESOLVER;
    }

    /**
     * Checks if the provided string is a JSON string
     * @param value A string that might be a JSON
     * @return True or false
     */
    private static boolean isJson(String value) {
        return StringUtils.startsWith(value, CoreConstants.ARRAY_OPENING)
            && StringUtils.endsWith(value, CoreConstants.ARRAY_CLOSING)
            && ObjectConversionUtil.isJson(value);
    }

    /**
     * Checks if the provided string can be considered a URL by trying to parse it into a {@code URL} object
     * @param value A string that might represent a URL
     * @return True or false
     */
    private static boolean isUrl(String value) {
        try {
            new URL(value);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Checks if the provided string is a class name. Supports standalone and nested classes
     * @param value A string that might represent a class name
     * @return True or false
     */
    private static boolean isClassName(String value) {
        return StringUtils.isNotBlank(value) && SourceVersion.isName(value) && !SourceVersion.isKeyword(value);
    }
}
