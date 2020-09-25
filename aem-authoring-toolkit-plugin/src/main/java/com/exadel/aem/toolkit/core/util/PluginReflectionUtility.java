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
package com.exadel.aem.toolkit.core.util;

import com.exadel.aem.toolkit.api.annotations.main.ClassField;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.Validator;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.handlers.DialogHandler;
import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.HandlesWidgets;
import com.exadel.aem.toolkit.api.runtime.Injected;
import com.exadel.aem.toolkit.api.runtime.RuntimeContext;
import com.exadel.aem.toolkit.core.exceptions.ExtensionApiException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.maven.PluginRuntimeContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains utility methods for manipulating AEM components Java classes, their fields, and the annotations these fields are marked with
 */
public class PluginReflectionUtility {
    private static final String PACKAGE_BASE_WILDCARD = ".*";

    private String packageBase;

    private org.reflections.Reflections reflections;

    private List<DialogWidgetHandler> customDialogWidgetHandlers;

    private List<DialogHandler> customDialogHandlers;

    private Map<String, Validator> validators;

    private PluginReflectionUtility() {
    }

    /**
     * Used to initialize {@code PluginReflectionUtility} instance based on list of available classpath entries in the
     * scope of this Maven plugin
     *
     * @param elements    List of classpath elements to be used in reflection routines
     * @param packageBase String representing package prefix of processable AEM backend components, like {@code com.acme.aem.components.*}.
     *                    If not specified, all available components will be processed
     * @return {@link PluginReflectionUtility} instance
     */
    public static PluginReflectionUtility fromCodeScope(List<String> elements, String packageBase) {
        URL[] urls = new URL[]{};
        if (elements != null) {
            urls = elements.stream()
                    .map(File::new)
                    .map(File::toURI)
                    .map(PluginReflectionUtility::toUrl)
                    .filter(Objects::nonNull).toArray(URL[]::new);
        }
        Reflections reflections = new org.reflections.Reflections(new ConfigurationBuilder()
                .addClassLoader(new URLClassLoader(urls, PluginReflectionUtility.class.getClassLoader()))
                .setUrls(urls)
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));
        PluginReflectionUtility newInstance = new PluginReflectionUtility();
        newInstance.reflections = reflections;
        newInstance.packageBase = StringUtils.strip(StringUtils.defaultString(packageBase, StringUtils.EMPTY),
                PACKAGE_BASE_WILDCARD);
        return newInstance;
    }

    /**
     * Initializes as necessary and returns collection of {@code CustomDialogComponentHandler}s defined within the Compile
     * scope of the plugin
     *
     * @return {@code List<DialogWidgetHandler>} of instances
     */
    public List<DialogWidgetHandler> getCustomDialogWidgetHandlers() {
        if (customDialogWidgetHandlers != null) {
            return customDialogWidgetHandlers;
        }
        customDialogWidgetHandlers = getHandlers(DialogWidgetHandler.class);
        boolean check = false;
        while (!check) {
            compareNode(customDialogWidgetHandlers);
            check = check(customDialogWidgetHandlers);
        }
        return customDialogWidgetHandlers;
    }

    /**
     * Initializes as necessary and returns collection of {@code CustomDialogComponentHandler}s defined within the Compile
     * scope of the plugin matching the specified widget annotation
     *
     * @param annotationClass {@code Class<?>} reference to pick up handlers for
     * @return {@code List<DialogWidgetHandler>} of instances
     */
    public List<DialogWidgetHandler> getCustomDialogWidgetHandlers(Class<? extends Annotation> annotationClass) {
        return getCustomDialogWidgetHandlers(Collections.singletonList(annotationClass));
    }

    /**
     * Initializes as necessary and returns collection of {@code CustomDialogComponentHandler}s defined within the Compile
     * scope of the plugin matching the specified widget annotation
     *
     * @param annotationClasses List of {@code Class<?>} reference to pick up handlers for
     * @return {@code List<DialogWidgetHandler>} of instances
     */
    public List<DialogWidgetHandler> getCustomDialogWidgetHandlers(List<Class<? extends
            Annotation>> annotationClasses) {
        if (annotationClasses == null) {
            return Collections.emptyList();
        }
        return getCustomDialogWidgetHandlers().stream()
                .filter(handler -> handler.getClass().isAnnotationPresent(HandlesWidgets.class))
                .filter(handler -> {
                    Class<?>[] handled = handler.getClass().getDeclaredAnnotation(HandlesWidgets.class).value();
                    return annotationClasses.stream().anyMatch(annotationClass -> ArrayUtils.contains(handled, annotationClass));
                })
                .collect(Collectors.toList());
    }

    /**
     * Initializes as necessary and returns collection of {@code CustomDialogHandler}s defined within the Compile
     * scope of the AEM Authoring Toolkit Maven plugin
     *
     * @return {@code List<DialogHandler>} of instances
     */
    public List<DialogHandler> getCustomDialogHandlers() {
        if (customDialogHandlers != null) {
            return customDialogHandlers;
        }
        customDialogHandlers = getHandlers(DialogHandler.class);
        return customDialogHandlers;
    }

    /**
     * Initializes as necessary and returns collection of {@code Validator}s defined within the Compile
     * scope of the AEM Authoring Toolkit Maven plugin
     *
     * @return {@code List<Validator>} of instances
     */
    public Map<String, Validator> getValidators() {
        if (validators != null) {
            return validators;
        }
        validators = reflections.getSubTypesOf(Validator.class).stream()
                .map(PluginReflectionUtility::getInstance)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(validator -> validator.getClass().getName(), Function.identity()));
        return validators;
    }

    /**
     * Returns list of {@code @Dialog}-annotated classes within the Compile scope the plugin is operating in, to
     * determine which of the component folders to process.
     * If {@code componentsPath} is set for this instance, classes are tested to be under that path
     *
     * @return {@code List<Class>} of instances
     */
    public List<Class<?>> getComponentClasses() {
        return reflections.getTypesAnnotatedWith(Dialog.class, true).stream()
                .filter(cls -> StringUtils.isEmpty(packageBase) || cls.getName().startsWith(packageBase))
                .collect(Collectors.toList());
    }

    /**
     * Gets generic list of handler instances invoked from all available derivatives of specified handler {@code Class}.
     * Each is supplied with a reference to {@link PluginRuntimeContext} as required
     *
     * @param handlerClass {@code Class} object
     * @param <T>          Expected handler type
     * @return {@link List<T>} of instances
     */
    private <T> List<T> getHandlers(Class<? extends T> handlerClass) {
        return reflections.getSubTypesOf(handlerClass).stream()
                .map(PluginReflectionUtility::getHandlerInstance)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(handler -> handler.getClass().getCanonicalName())) // to provide stable handlers sequence between runs
                .collect(Collectors.toList());
    }

    /**
     * Creates new instance object of a handler {@code Class} and populates {@link RuntimeContext} instance to
     * every field annotated with {@link Injected}
     *
     * @param handlerClass The handler class to instantiate
     * @param <T>          Instance type
     * @return New handler instance
     */
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
     * Creates a new instance object of the specified {@code Class}
     *
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

    /**
     * Used to set a reference to {@link PluginRuntimeContext} to the handler instance
     *
     * @param handler Handler instance
     * @param field   The field of handler to populate
     */
    private static void populateRuntimeContext(Object handler, Field field) {
        field.setAccessible(true);
        try {
            field.set(handler, PluginRuntime.context());
        } catch (IllegalAccessException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ExtensionApiException(handler.getClass(), e));
        }
    }

    /**
     * Retrieves annotations of a {@code Field} instance as a sequential {@code Stream}
     *
     * @param field The field to analyze
     * @return Stream of annotation objects,
     */
    public static Stream<Class<? extends Annotation>> getFieldAnnotations(Field field) {
        return Arrays.stream(field.getDeclaredAnnotations())
                .map(Annotation::annotationType);
    }

    /**
     * Retrieves a complete list of {@code Field}s of a {@code Class}
     *
     * @param targetClass The class to analyze
     * @return List of {@code Field} objects
     */
    public static List<Field> getAllFields(Class<?> targetClass) {
        return getAllFields(targetClass, Collections.emptyList());
    }

    /**
     * Retrieves a sequential list of all {@code Field}s of a certain {@code Class} that match specific criteria
     *
     * @param targetClass The class to analyze
     * @param predicates  List of {@code Predicate<Field>} instances to pick up appropriate fields
     * @return List of {@code Field} objects
     */
    public static List<Field> getAllFields(Class<?> targetClass, List<Predicate<Field>> predicates) {
        List<Field> fields = new LinkedList<>();
        List<ClassField> ignoredFields = new LinkedList<>();

        for (Class<?> classEntry : getClassHierarchy(targetClass)) {
            List<Field> classFields = Arrays.stream(classEntry.getDeclaredFields())
                    .filter(PluginObjectPredicates.getFieldsPredicate(predicates))
                    .collect(Collectors.toList());
            fields.addAll(classFields);
            if (classEntry.getAnnotation(IgnoreFields.class) != null) {
                List<ClassField> processedClassFields = Arrays.stream(classEntry.getAnnotation(IgnoreFields.class).value())
                        .map(classField -> PluginObjectUtility.modifyIfDefault(classField,
                                ClassField.class,
                                DialogConstants.PN_SOURCE_CLASS,
                                targetClass))
                        .collect(Collectors.toList());
                ignoredFields.addAll(processedClassFields);
            }
        }
        return fields.stream()
                .filter(PluginObjectPredicates.getNotIgnoredFieldsPredicate(ignoredFields))
                .sorted(PluginObjectPredicates::compareByRanking)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves type of object(s) returned by the method. If method is designed to provide single object, its type
     * is returned. But if method supplies an array, type of array's element is returned
     *
     * @param method The method to analyze
     * @return Appropriate {@code Class} instance
     */
    public static Class<?> getMethodPlainType(Method method) {
        return method.getReturnType().isArray()
                ? method.getReturnType().getComponentType()
                : method.getReturnType();
    }

    /**
     * Retrieves the sequential list of ancestral of a specific {@code Class}, target class itself included,
     * starting from the "top" of the inheritance tree. {@code Object} class is not added to the hierarchy
     *
     * @param targetClass The class to build the tree upon
     * @return List of {@code Class} objects
     */
    public static List<Class<?>> getClassHierarchy(Class<?> targetClass) {
        return getClassHierarchy(targetClass, true);
    }

    /**
     * Retrieves the sequential list of ancestral classes of a specific {@code Class}, started from the "top" of the inheritance
     * tree. {@code Object} class is not added to the hierarchy
     *
     * @param targetClass   The class to analyze
     * @param includeTarget Whether to include the {@code targetClass} itself to the hierarchy
     * @return List of {@code Class} objects
     */
    public static List<Class<?>> getClassHierarchy(Class<?> targetClass, boolean includeTarget) {
        List<Class<?>> result = new LinkedList<>();
        Class<?> current = targetClass;
        while (current != null && !current.isInterface() && !current.equals(Object.class)) {
            if (!current.equals(targetClass) || includeTarget) {
                result.add(current);
            }
            current = current.getSuperclass();
        }
        Collections.reverse(result);
        return result;
    }


    /**
     * Retrieves list of properties of an {@code Annotation} object to which non-default values have been set
     *
     * @param annotation The annotation instance to analyze
     * @return List of {@code Method} instances that represent properties initialized with non-defaults
     */
    public static List<Method> getAnnotationNonDefaultProperties(Annotation annotation) {
        return Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(method -> annotationPropertyIsNotDefault(annotation, method))
                .collect(Collectors.toList());
    }

    /**
     * Gets whether any of the {@code Annotation}'s properties has a value which is not default
     *
     * @param annotation The annotation to analyze
     * @return True or false
     */
    public static boolean annotationIsNotDefault(Annotation annotation) {
        return Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .anyMatch(method -> annotationPropertyIsNotDefault(annotation, method));
    }

    /**
     * Gets whether an {@code Annotation} property has a value which is not default
     *
     * @param annotation The annotation to analyze
     * @param method     The method representing the property
     * @return True or false
     */
    static boolean annotationPropertyIsNotDefault(Annotation annotation, Method method) {
        try {
            Object defaultValue = method.getDefaultValue();
            if (defaultValue == null) {
                return true;
            }
            Object invocationResult = method.invoke(annotation);
            if (method.getReturnType().isArray() && ArrayUtils.isEmpty((Object[]) invocationResult)) {
                return false;
            }
            return !defaultValue.equals(invocationResult);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return true;
        }
    }

    /**
     * Converts {@link URI} parameter, such as of a classpath element, to an {@link URL} instance used by {@link Reflections}
     *
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

    private static List<DialogWidgetHandler> compareNode(List<DialogWidgetHandler> copy) {
        List<DialogWidgetHandler> list = new ArrayList<>(copy);
        List<String> temp = new ArrayList<>();
        int after;
        int before;
        int i;
        for (DialogWidgetHandler handler : copy) {
            for (DialogWidgetHandler item : list) {
                String[] className = item.getClass().getName().split("\\.");
                temp.add(className[className.length - 1]);
            }
            for (int k = 0; k < list.size(); k++) {
                int current = temp.indexOf(handler.getClass().getName().split("\\.")[handler.getClass().getName().split("\\.").length - 1]);
                after = handler.after() != null ? temp.indexOf(handler.after()) : current;
                before = handler.before() != null ? temp.indexOf(handler.before()) : current;
                if (after > before) {
                    Collections.swap(list, after, before);
                    Collections.swap(temp, after, before);
                    i = after;
                    after = before;
                    before = i;
                }
                if (current > before) {
                    Collections.swap(list, current, before);
                    Collections.swap(temp, current, before);
                    i = current;
                    current = before;
                    before = i;
                }
                if (current < after) {
                    Collections.swap(list, current, after);
                    Collections.swap(temp, current, after);
                    i = current;
                    current = after;
                    after = i;
                }
            }
        }
        return list;
    }

    private boolean check(List<DialogWidgetHandler> list) {
        List<String> temp = new ArrayList<>();
        for (DialogWidgetHandler item : list) {
            String[] className = item.getClass().getName().split("\\.");
            temp.add(className[className.length - 1]);
        }
        for (DialogWidgetHandler item : list) {
            String current = item.getClass().getName().split("\\.")[item.getClass().getName().split("\\.").length - 1];
            if ((temp.indexOf(item.before()) < temp.indexOf(current) || temp.indexOf(item.after()) > temp.indexOf(current)) && !(item.before().equals(item.after()) && !item.before().equals(current))) {
                return false;
            }
        }
        return true;
    }

}
