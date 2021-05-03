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
package com.exadel.aem.toolkit.plugin.runtime;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.meta.Validator;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.HandlesWidgets;
import com.exadel.aem.toolkit.api.runtime.Injected;
import com.exadel.aem.toolkit.api.runtime.RuntimeContext;
import com.exadel.aem.toolkit.plugin.exceptions.ExtensionApiException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntimeContext;
import com.exadel.aem.toolkit.plugin.utils.ScopeUtil;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Introspects the classes available in Maven reactor to retrieve and manage Toolkit-related logic
 */
public class ReflectionContextHelper {

    private static final String PACKAGE_BASE_WILDCARD = ".*";

    private String packageBase;

    private org.reflections.Reflections reflections;

    private List<Handler> handlers;

    private List<Validator> validators;

    /**
     * Default (instantiation-restricting) constructor
     */
    private ReflectionContextHelper() {
    }


    /* --------------------------
       Retrieving manages classes
       -------------------------- */

    /**
     * Returns list of {@code AemComponent}-annotated and {@code @Dialog}-annotated classes within the scope the plugin
     * is operating in, to determine which of the component folders to process. If {@code componentsPath} is set for this
     * instance, classes are tested to be under that path
     * @return {@code List} of class references
     */
    public List<Class<?>> getComponentClasses() {
        List<Class<?>> classesAnnotatedWithDialog = reflections.getTypesAnnotatedWith(Dialog.class, true).stream()
            .filter(cls -> StringUtils.isEmpty(packageBase) || cls.getName().startsWith(packageBase))
            .collect(Collectors.toList());
        List<Class<?>> classesAnnotatedWithComponent = reflections.getTypesAnnotatedWith(AemComponent.class, true).stream()
            .filter(cls -> StringUtils.isEmpty(packageBase) || cls.getName().startsWith(packageBase))
            .collect(Collectors.toList());

        List<Class<?>> componentViews = new ArrayList<>();
        classesAnnotatedWithComponent.forEach(cls -> componentViews.addAll(Arrays.asList(cls.getAnnotation(AemComponent.class).views())));
        classesAnnotatedWithComponent.addAll(classesAnnotatedWithDialog.stream().filter(cls -> !componentViews.contains(cls)).collect(Collectors.toList()));

        return classesAnnotatedWithComponent;
    }


    /* -------------------
       Retrieving handlers
       ------------------- */

    /**
     * Retrieves a list of {@link Handler} instances that match the provided annotations and scope. The list is ordered
     * in such a way as to honor the relations set by {@code before} and {@code after} anchors
     * @param scope       Non-null string representing the scope that the handlers must match
     * @param annotations Non-null array of {@code Annotation} objects, usually representing annotations of a method
     *                    or a class
     * @return {@code List} of handler instances, ordered
     */
    public List<Handler> getHandlers(String scope, Annotation[] annotations) {
        List<Handler> result = getHandlers().stream()
            .filter(handler -> isHandlerMatches(handler, scope, annotations))
            .collect(Collectors.toList());
        return OrderingUtil.sortHandlers(result);
    }

    /**
     * Retrieves a list of {@link Handler} instances that match the provided annotation types and scope. The list
     * is ordered in such a way as to honor the relations set by {@code before} and {@code after} anchors
     * @param scope           Non-null string representing the scope that the handlers must match
     * @param annotationTypes Non-null array of {@code Class} objects
     * @return {@code List} of handler instances, ordered
     */
    public List<Handler> getHandlers(String scope, Class<?>... annotationTypes) {
        List<Handler> result = getHandlers().stream()
            .filter(handler -> isHandlerMatches(handler, scope, annotationTypes))
            .collect(Collectors.toList());
        return OrderingUtil.sortHandlers(result);
    }

    /**
     * Retrieves the list of {@code Handler}s defined within the scope the plugin is operating in
     * @return {@code List} of handler instances
     */
    public List<Handler> getHandlers() {
        if (handlers != null) {
            return handlers;
        }
        handlers = reflections.getSubTypesOf(Handler.class).stream()
            .filter(cls -> !cls.isInterface())
            .map(ReflectionContextHelper::getHandlerInstance)
            .filter(Objects::nonNull)
            .sorted(OrderingUtil::compareByOrigin) // to provide stable handlers sequence between runs
            .collect(Collectors.toList());
        return handlers;
    }

    /**
     * Tests whether the given handler fits for the conditions defined by the set of manageable annotations and the
     * {@code Scope} value
     * @param scope       String value representing the scope that the handlers must match
     * @param handler     {@code Handler} instance to test
     * @param annotations Array of {@code Annotation} objects, usually representing annotations of a method or class
     * @return True or false
     */
    private static boolean isHandlerMatches(Handler handler, String scope, Annotation[] annotations) {
        return isHandlerMatches(handler, scope, Arrays.stream(annotations).map(Annotation::annotationType).toArray(Class<?>[]::new));
    }

    /**
     * Tests whether the given handler fits for the conditions defined by the set of manageable annotations and the
     * {@code Scope} value
     * @param handler         {@code Handler} instance to test
     * @param scope           String value representing the scope that the handlers must match
     * @param annotationTypes Array of {@code Class} references, usually representing types of annotations of a method
     *                        or a class
     * @return True or false
     */
    @SuppressWarnings("deprecation") // HandlesWidgets processing is retained for compatibility and will be removed
    // in a version after 2.0.2
    private static boolean isHandlerMatches(Handler handler, String scope, Class<?>[] annotationTypes) {
        Handles handles = handler.getClass().getDeclaredAnnotation(Handles.class);
        HandlesWidgets handlesWidgets = handler.getClass().getDeclaredAnnotation(HandlesWidgets.class);
        if (handles == null && handlesWidgets == null) {
            return false;
        }
        Class<? extends Annotation>[] handledAnnotationTypes = handles != null
            ? handles.value()
            : handlesWidgets.value();
        boolean isMatchByType = Arrays.stream(handledAnnotationTypes)
            .anyMatch(annotationType -> Arrays.asList(annotationTypes).contains(annotationType));

        String[] handlerScopes = handles != null ? handles.scope() : new String[]{Scopes.DEFAULT};
        // Try to guess appropriate scopes for the handler judging by the annotations it handles
        // (so that if it handles e.g. @ChildEditConfig, the scope for the handler is exactly ChildEditConfig)
        if (handles != null && handlerScopes.length == 1 && handlerScopes[0].equals(Scopes.DEFAULT)) {
            handlerScopes = ScopeUtil.designate(handles.value());
        }
        // If still no particular scopes, try to guess by the mere annotations added to the current class
        // (so that if there's e.g. @Dialog, and the handler has no particular scope, it is considered the handler
        // is also for the dialog)
        if (handlerScopes.length == 1 && handlerScopes[0].equals(Scopes.DEFAULT)) {
            handlerScopes = ScopeUtil.designate(annotationTypes);
        }
        boolean isMatchByScope = ScopeUtil.fits(scope, handlerScopes);

        return isMatchByType && isMatchByScope;
    }

    /**
     * Creates a new instance of a handler by {@code Class} reference and populates the runtime context
     * @param handlerClass The handler class to instantiate
     * @param <T>          Handler type
     * @return New handler instance
     */
    @SuppressWarnings("deprecation") // RuntimeContext and @Injected are processed for compatibility, to be removed in
    // a version after 2.0.2
    private static <T> T getHandlerInstance(Class<? extends T> handlerClass) {
        T instance = getInstance(handlerClass);
        if (instance != null) {
            Arrays.stream(handlerClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Injected.class)
                    && ClassUtils.isAssignable(field.getType(), RuntimeContext.class))
                .forEach(field -> populateRuntimeContext(instance, field));
        }
        return instance;
    }

    /**
     * Used to set a reference to {@link PluginRuntimeContext} to the handler instance
     * @param handler Handler instance
     * @param field   The field of the handler to populate
     */
    @SuppressWarnings("squid:S3011")
    // Access elevation is preserved for compatibility until context injection is retired
    private static void populateRuntimeContext(Object handler, Field field) {
        field.setAccessible(true);
        try {
            field.set(handler, PluginRuntime.context());
        } catch (IllegalAccessException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ExtensionApiException(handler.getClass(), e));
        }
    }


    /* ---------------------
       Retrieving validators
       --------------------- */

    /**
     * Initializes as necessary and returns collection of {@code Validator}s defined within the execution scope
     * of the ToolKit Maven plugin
     * @return {@code List} of instances
     */
    public List<Validator> getValidators() {
        if (validators != null) {
            return validators;
        }
        validators = reflections.getSubTypesOf(Validator.class).stream()
            .map(ReflectionContextHelper::getInstance)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return validators;
    }


    /* ----------------
       Common utilities
       ---------------- */

    /**
     * Creates a new instance object of the specified {@code Class}
     * @param instanceClass The class to instantiate
     * @param <T>           Instance type
     * @return New object instance
     */
    private static <T> T getInstance(Class<? extends T> instanceClass) {
        try {
            return instanceClass.getConstructor().newInstance();
        } catch (InstantiationException
            | IllegalAccessException
            | InvocationTargetException
            | NoSuchMethodException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ExtensionApiException(instanceClass, e));
        }
        return null;
    }


    /* ---------------
       Factory methods
       --------------- */

    /**
     * Used to initialize a {@code PluginReflectionUtility} instance based on the list of available classpath entries
     * in the scope of this Maven plugin
     * @param elements    List of classpath elements to be used in reflection routines
     * @param packageBase String representing package prefix of processable AEM backend components
     *                    like {@code com.acme.aem.components.*}. If not specified, all available components will be processed
     * @return {@link ReflectionContextHelper} instance
     */
    public static ReflectionContextHelper fromCodeScope(List<String> elements, String packageBase) {
        URL[] urls = new URL[]{};
        if (elements != null) {
            urls = elements.stream()
                .map(File::new)
                .map(File::toURI)
                .map(ReflectionContextHelper::toUrl)
                .filter(Objects::nonNull).toArray(URL[]::new);
        }
        Reflections reflections = new org.reflections.Reflections(new ConfigurationBuilder()
            .addClassLoader(new URLClassLoader(urls, ReflectionContextHelper.class.getClassLoader()))
            .setUrls(urls)
            .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));
        ReflectionContextHelper newInstance = new ReflectionContextHelper();
        newInstance.reflections = reflections;
        newInstance.packageBase = StringUtils.strip(StringUtils.defaultString(packageBase, StringUtils.EMPTY),
            PACKAGE_BASE_WILDCARD);
        return newInstance;
    }

    /**
     * Converts {@link URI} parameter, such as of a classpath element, to an {@link URL} instance used by {@link Reflections}
     * @param uri {@code URI} value
     * @return {@code URL} value
     */
    private static URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
        return null;
    }
}
