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
package com.exadel.aem.toolkit.core.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;

/**
 * This is a testing scope injector that hooks on {@link RequestProperty} to make it possible to use the same testcase
 * Sling models for different injection scenarios
 */
@Component(
    service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING)
public class DelegateInjector implements Injector {

    static final String NAME = "eak-delegate-injector";

    private final Injector delegate;

    public DelegateInjector(Injector delegate) {
        this.delegate = delegate;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    @CheckForNull
    @Override
    public Object getValue(
        @Nonnull Object adaptable,
        String name,
        @Nonnull Type type,
        @Nonnull AnnotatedElement annotatedElement,
        @Nonnull DisposalCallbackRegistry disposalCallbackRegistry) {

        RequestProperty requestProperty = annotatedElement.getAnnotation(RequestProperty.class);
        String effectiveName = StringUtils.defaultIfBlank(requestProperty.name(), name);

        Injectable value = null;
        if (delegate instanceof RequestAttributeInjector) {
            RequestAttributeInjector requestAttributeInjector = (RequestAttributeInjector) delegate;
            value = Injectable.of(requestAttributeInjector.getValue(
                AdaptationUtil.getRequest(adaptable),
                effectiveName,
                type));

        } else if (delegate instanceof RequestParamInjector) {
            RequestParamInjector requestParamInjector = (RequestParamInjector) delegate;
            value = Injectable.of(requestParamInjector.getValue(
                AdaptationUtil.getRequest(adaptable),
                effectiveName,
                type));

        } else if (delegate instanceof RequestSelectorsInjector) {
            RequestSelectorsInjector requestSelectorsInjector = (RequestSelectorsInjector) delegate;
            value = Injectable.of(requestSelectorsInjector.getValue(
                AdaptationUtil.getRequest(adaptable),
                type));

        } else if (delegate instanceof RequestSuffixInjector) {
            RequestSuffixInjector requestSuffixInjector = (RequestSuffixInjector) delegate;
            value = Injectable.of(requestSuffixInjector.getValue(
                AdaptationUtil.getRequest(adaptable),
                type));

        } else if (delegate instanceof EnumValueInjector) {
            EnumValueInjector enumValueInjector = (EnumValueInjector) delegate;
            value = Injectable.of(enumValueInjector.getValue(
                adaptable,
                effectiveName,
                StringUtils.EMPTY,
                type));
        }
        return BaseInjector.defaultIfEmpty(value, type, annotatedElement);
    }
}
