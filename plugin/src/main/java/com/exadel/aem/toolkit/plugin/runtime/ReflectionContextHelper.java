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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
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
import com.exadel.aem.toolkit.plugin.sources.ComponentSource;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.ScopeUtil;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Introspects the classes available in the Maven reactor to retrieve and manage Toolkit-related logic
 */
public class ReflectionContextHelper {

    private Reflections reflections;

    private ClassLoader classLoader;

    private List<ComponentSource> components;

    private List<Handler> handlers;

    private List<Validator> validators;

    /**
     * Default (instantiation-restricting) constructor
     */
    private ReflectionContextHelper() {
    }

    /* ----------------
       Common accessors
       ---------------- */

    /**
     * Retrieves the {@link ClassLoader} that was used to instantiate source Java classes and handlers for the current
     * instance. this classloader can be further used for creating metadata proxies and similar tasks
     * @return {@code ClassLoader} instance
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /* -----------------------------
       Retrieving managed components
       ----------------------------- */

    /**
     * Retrieves a collection of unique {@code ComponentSource} objects that encapsulate {@code AemComponent}-annotated
     * and {@code @Dialog}-annotated classes
     * @param packageBase Restricts the processing to the particular package(-s) in the plugin's settings. Can help to,
     *                    e.g., separate between classes that are matched by component folders in the current content
     *                    package
     * @return A non-null list of {@code ComponentSource} objects; can be empty
     */
    public List<ComponentSource> getComponents(String packageBase) {
        return getComponents()
            .stream()
            .filter(comp -> StringUtils.isEmpty(packageBase) || ClassUtil.matchesReference(comp.adaptTo(Class.class), packageBase))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a collection of unique {@code ComponentSource} objects that encapsulate {@code AemComponent}-annotated
     * and {@code @Dialog}-annotated classes
     * @return A non-null list of {@code ComponentSource} objects; can be empty
     */
    private List<ComponentSource> getComponents() {
        if (components != null) {
            return components;
        }

        Set<Class<?>> classesAnnotatedWithComponent = new HashSet<>(
            reflections.getTypesAnnotatedWith(AemComponent.class, true));
        Set<Class<?>> classesAnnotatedWithDialog = new HashSet<>(
            reflections.getTypesAnnotatedWith(Dialog.class, true));

        Set<Class<?>> componentViews = new HashSet<>();
        classesAnnotatedWithComponent.forEach(cls -> componentViews.addAll(Arrays.asList(cls.getAnnotation(AemComponent.class).views())));
        classesAnnotatedWithComponent.addAll(classesAnnotatedWithDialog
            .stream()
            .filter(cls -> !componentViews.contains(cls))
            .collect(Collectors.toList()));

        components = classesAnnotatedWithComponent.stream().map(Sources::fromComponentClass).collect(Collectors.toList());
        return components;
    }

    /**
     * Retrieves a {@link ComponentSource} that encapsulates the given AEM component's {@code Class}
     * @param componentClass {@code Class} reference; a non-null value is expected
     * @return {@code ComponentSource} value; can be null if there's no match
     */
    public ComponentSource getComponent(Class<?> componentClass) {
        return getComponents()
            .stream()
            .filter(comp -> comp.matches(componentClass))
            .findFirst()
            .orElse(null);
    }

    /**
     * Retrieves a {@link ComponentSource} that matches the given {@code path} (either an absolute one or a chunk)
     * @param path {@code String} value; a non-blank string is expected
     * @return {@code ComponentSource} value; can be null if there's no match
     */
    public ComponentSource getComponent(String path) {
        return getComponents()
            .stream()
            .filter(comp -> comp.matches(path))
            .findFirst()
            .orElse(null);
    }

    /* -------------------
       Retrieving handlers
       ------------------- */

    /**
     * Gets whether the given annotation has a managed handler or a meta-annotation. This method is useful for
     * distinguishing between ToolKit-relevant annotations (including custom ones that reside in the user's code
     * namespace) and "foreign" annotations
     * @param annotation {@link Annotation} object
     * @return True or false
     */
    public boolean isHandled(Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(AnnotationRendering.class)
            || getHandlers()
            .stream()
            .anyMatch(handler -> isHandlerMatches(handler, null, new Class<?>[]{annotation.annotationType()}));
    }

    /**
     * Retrieves a list of {@link Handler} instances that match the provided annotations and scope. The list is ordered
     * in such a way as to honor the relations set by {@code before} and {@code after} anchors
     * @param scope       A non-null string representing the scope that the handlers must match
     * @param annotations A non-null array of {@code Annotation} objects, usually representing annotations of a method
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
     * Retrieves a list of {@link Handler} instances that match the provided annotation types and scope. The list is
     * ordered in such a way as to honor the relations set by {@code before} and {@code after} anchors
     * @param scope           A non-null string representing the scope that the handlers must match
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
     * Tests whether the given handler is suitable for the conditions defined by the set of manageable annotations and
     * the {@code Scope} value
     * @param scope       String value representing the scope that the handlers must match
     * @param handler     {@code Handler} instance to test
     * @param annotations An array of {@code Annotation} objects, usually representing annotations of a method or class
     * @return True or false
     */
    private static boolean isHandlerMatches(Handler handler, String scope, Annotation[] annotations) {
        return isHandlerMatches(handler, scope, Arrays.stream(annotations).map(Annotation::annotationType).toArray(Class<?>[]::new));
    }

    /**
     * Tests whether the given handler is suitable for the conditions defined by the set of manageable annotations and
     * the {@code Scope} value
     * @param handler         {@code Handler} instance to test
     * @param scope           String value representing the scope that the handlers must match
     * @param annotationTypes An array of {@code Class} references, usually representing types of annotations of a
     *                        method or a class
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
        // (so that if it handles, e.g., {@code @ChildEditConfig}, the scope for the handler is exactly ChildEditConfig)
        if (handles != null && handlerScopes.length == 1 && handlerScopes[0].equals(Scopes.DEFAULT)) {
            handlerScopes = ScopeUtil.designate(handles.value());
        }
        // If still no particular scopes, try to guess by the mere annotations added to the current class
        // (so that if there is, e.g., {@code @Dialog}, and the handler has no particular scope, it is assumed that
        // the handler is also for the dialog)
        if (handlerScopes.length == 1 && handlerScopes[0].equals(Scopes.DEFAULT)) {
            handlerScopes = ScopeUtil.designate(annotationTypes);
        }
        boolean isMatchByScope = scope == null || ScopeUtil.fits(scope, handlerScopes);

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
     * Initializes as necessary and returns a collection of {@code Validator}s defined within the execution scope of the
     * ToolKit Maven plugin
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
            | NoSuchMethodException ex) {
            PluginRuntime.context().getExceptionHandler().handle(new ExtensionApiException(instanceClass, ex));
        }
        return null;
    }

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Used to initialize a {@code PluginReflectionUtility} instance based on the list of available classpath entries in
     * the scope of this Maven plugin
     * @param elements List of classpath elements to be used in reflection routines
     * @return {@link ReflectionContextHelper} instance
     */
    public static ReflectionContextHelper fromCodeScope(List<String> elements) {
        URL[] urls = new URL[0];
        if (elements != null) {
            urls = elements.stream()
                .map(File::new)
                .map(File::toURI)
                .map(ReflectionContextHelper::toUrl)
                .filter(Objects::nonNull)
                .toArray(URL[]::new);
        }
        URLClassLoader classLoader = new URLClassLoader(urls, ReflectionContextHelper.class.getClassLoader());
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .addClassLoader(classLoader)
            .setUrls(urls)
            .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));
        ReflectionContextHelper newInstance = new ReflectionContextHelper();
        newInstance.classLoader = classLoader;
        newInstance.reflections = reflections;
        return newInstance;
    }

    /**
     * Converts {@link URI} parameter, such as of a classpath element, to an {@link URL} instance used by {@link
     * Reflections}
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
