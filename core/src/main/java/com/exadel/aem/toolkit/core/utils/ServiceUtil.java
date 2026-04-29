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
package com.exadel.aem.toolkit.core.utils;

import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods for working with OSGi services outside the usual component injection context
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
@SuppressWarnings("LoggingSimilarMessage")
public class ServiceUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceUtil.class);

    private static final String ERROR_CONTEXT = "Could not obtain OSGi bundle context for {}";
    private static final String ERROR_HANDLING = "Error handling a service reference for {}";
    private static final String ERROR_RETRIEVAL = "Could not retrieve instance of {}";

    /**
     * Default (instantiation-blocking) constructor
     */
    private ServiceUtil() {
    }

    /**
     * Executes the provided routine with an instance of the specified service class, if available. The service is
     * automatically obtained and released after the consumer is executed
     * @param serviceClass The class of the service to be used
     * @param consumer     The routine to be executed with the service instance
     * @param <T>          The type of the service
     */
    public static <T> void withService(
        @Nonnull Class<T> serviceClass,
        @Nonnull Consumer<T> consumer) {
        Bundle bundle = FrameworkUtil.getBundle(serviceClass);
        BundleContext context = bundle != null ? bundle.getBundleContext() : null;
        if (context == null) {
            LOG.error(ERROR_CONTEXT, serviceClass.getName());
            return;
        }
        withService(serviceClass, context, consumer);
    }

    /**
     * Executes the provided routine with an instance of the specified service class to get a result, if available. The
     * service is automatically obtained and released after the processor is executed
     * @param serviceClass The class of the service to be used
     * @param processor    The routine to compute the result with the service instance
     * @param defaultValue The value to be returned if the service is not available or an error occurs
     * @param <T>          The type of the service
     * @param <U>          The type of the result
     * @return The result of the processor execution, or the default value
     */
    public static <T, U> U withService(
        @Nonnull Class<T> serviceClass,
        @Nonnull Function<T, U> processor,
        U defaultValue) {
        Bundle bundle = FrameworkUtil.getBundle(serviceClass);
        BundleContext context = bundle != null ? bundle.getBundleContext() : null;
        if (context == null) {
            LOG.error(ERROR_CONTEXT, serviceClass.getName());
            return defaultValue;
        }
        return withService(serviceClass, context, processor, defaultValue);
    }

    /**
     * Executes the provided routine with an instance of the specified service class, if available. The service is
     * automatically obtained and released after the consumer is executed
     * @param serviceClass The class of the service to be used
     * @param context      The OSGi bundle context to be used for service retrieval
     * @param consumer     The routine to be executed with the service instance
     * @param <T>          The type of the service
     */
    public static <T> void withService(
        @Nonnull Class<T> serviceClass,
        @Nonnull BundleContext context,
        @Nonnull Consumer<T> consumer) {

        ServiceReference<T> reference = null;
        try {
            reference = context.getServiceReference(serviceClass);
            T service = reference != null ? context.getService(reference) : null;
            if (service == null) {
                LOG.error(ERROR_RETRIEVAL, serviceClass.getName());
                return;
            }
            consumer.accept(service);
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.error(ERROR_HANDLING, serviceClass.getName(), e);
        } finally {
            ungetService(context, reference);
        }
    }

    /**
     * Executes the provided routine with an instance of the specified service class to get a result, if available. The
     * service is automatically obtained and released after the processor is executed
     * @param serviceClass The class of the service to be used
     * @param context      The OSGi bundle context to be used for service retrieval
     * @param processor    The routine to compute the result with the service instance
     * @param defaultValue The value to be returned if the service is not available or an error occurs
     * @param <T>          The type of the service
     * @param <U>          The type of the result
     * @return The result of the processor execution, or the default value
     */
    public static <T, U> U withService(
        @Nonnull Class<T> serviceClass,
        @Nonnull BundleContext context,
        @Nonnull Function<T, U> processor,
        U defaultValue) {

        U result = defaultValue;
        ServiceReference<T> reference = null;
        try {
            reference = context.getServiceReference(serviceClass);
            T service = reference != null ? context.getService(reference) : null;
            if (service == null) {
                LOG.error(ERROR_RETRIEVAL, serviceClass.getName());
                return defaultValue;
            }
            result = processor.apply(service);
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.error(ERROR_HANDLING, serviceClass.getName(), e);
        } finally {
            ungetService(context, reference);
        }
        return result;
    }

    /**
     * Safely ungets the provided service reference from the context, suppressing any exceptions that might occur
     * @param context   The OSGi bundle context to be used for service ungetting
     * @param reference The service reference to be ungotten
     */
    private static void ungetService(BundleContext context, ServiceReference<?> reference) {
        if (reference == null) {
            return;
        }
        try {
            context.ungetService(reference);
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.warn(ERROR_HANDLING, reference, e);
        }
    }
}
