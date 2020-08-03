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

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import com.exadel.aem.toolkit.api.annotations.main.ClassField;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.Validator;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Replace;
import com.exadel.aem.toolkit.api.handlers.DialogHandler;
import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.runtime.Injected;
import com.exadel.aem.toolkit.api.runtime.RuntimeContext;
import com.exadel.aem.toolkit.core.exceptions.ExtensionApiException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.maven.PluginRuntimeContext;

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
     * @param elements List of classpath elements to be used in reflection routines
     * @param packageBase String representing package prefix of processable AEM backend components, like {@code com.acme.aem.components.*}.
     *                      If not specified, all available components will be processed
     * @return {@link PluginReflectionUtility} instance
     */
    public static PluginReflectionUtility fromCodeScope(List<String> elements, String packageBase) {
        URL[] urls = new URL[] {};
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
     * scope the plugin is operating in
     * @return {@code List<DialogWidgetHandler>} of instances
     */
    public List<DialogWidgetHandler> getCustomDialogWidgetHandlers() {
        if (customDialogWidgetHandlers != null) {
            return customDialogWidgetHandlers;
        }
        customDialogWidgetHandlers = getHandlers(DialogWidgetHandler.class);
        return customDialogWidgetHandlers;
    }

    /**
     * Initializes as necessary and returns collection of {@code CustomDialogHandler}s defined within the Compile
     * scope of the AEM Authoring Toolkit Maven plugin
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
     * @param handlerClass {@code Class} object
     * @param <T> Expected handler type
     * @return {@link List<T>} of instances
     */
    private <T> List<T> getHandlers(Class<? extends T> handlerClass) {
        return reflections.getSubTypesOf(handlerClass).stream()
                .map(PluginReflectionUtility::getHandlerInstance)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Creates new instance object of a handler {@code Class} and populates {@link RuntimeContext} instance to
     * every field annotated with {@link Injected}
     * @param handlerClass The handler class to instantiate
     * @param <T> Instance type
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
     * @param instanceClass The class to instantiate
     * @param <T> Instance type
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
     * @param handler Handler instance
     * @param field The field of handler to populate
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
     * @param field The field to analyze
     * @return Stream of annotation objects,
     */
    public static Stream<Class<? extends Annotation>> getFieldAnnotations(Field field){
        return Arrays.stream(field.getDeclaredAnnotations())
                .map(Annotation::annotationType);
    }

    /**
     * Retrieves a complete list of {@code Field}s of a {@code Class}
     * @param targetClass The class to analyze
     * @return List of {@code Field} objects
     */
    public static List<Field> getAllFields(Class<?> targetClass) {
        return getAllFields(targetClass, Collections.emptyList());
    }

    /**
     * Retrieves a sequential list of all {@code Field}s of a certain {@code Class} that match specific criteria
     * @param targetClass The class to analyze
     * @param predicates List of {@code Predicate<Field>} instances to pick up appropriate fields
     * @return List of {@code Field} objects
     */
    public static List<Field> getAllFields(Class<?> targetClass, List<Predicate<Field>> predicates) {
        List<Class<?>> classHierarchy = getClassHierarchy(targetClass);
        List<Field> allFields = classHierarchy.stream()
                .flatMap(classEntry -> Arrays.stream(classEntry.getDeclaredFields()).filter(Predicates.getCombinedPredicate(predicates)))
                .collect(Collectors.toList());

        List<Field> ignoredFields = classHierarchy.stream()
                .filter(classEntry -> classEntry.getAnnotation(IgnoreFields.class) != null)
                .flatMap(classEntry -> Arrays.stream(classEntry.getAnnotation(IgnoreFields.class).value()).map(classFieldEntry -> Pair.of(classFieldEntry, classEntry)))
                .filter(classFieldClassPair -> StringUtils.isNotEmpty(classFieldClassPair.getKey().field()))
                .map(classFieldClassPair -> PluginClassFieldUtility.populateDefaults(classFieldClassPair.getKey(), classFieldClassPair.getValue()))
                .flatMap(classFieldEntry -> allFields.stream().filter(field -> Predicates.isMatchByClassField(field, classFieldEntry)))
                .collect(Collectors.toList());
        Predicate<Field> ignoredFieldsPredicate = Predicates.getNotIgnoredFieldsPredicate(ignoredFields);

        return processFieldsOverrides(allFields.stream().filter(ignoredFieldsPredicate).sorted(Predicates::compareByRanking).collect(Collectors.toList()));
    }

    /**
     * Retrieves type of object(s) returned by the method. If method is designed to provide single object, its type
     * is returned. But if method supplies an array, type of array's element is returned
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
     * @param targetClass The class to build the tree upon
     * @return List of {@code Class} objects
     */
    public static List<Class<?>> getClassHierarchy(Class<?> targetClass) {
        return getClassHierarchy(targetClass, true);
    }

    /**
     * Retrieves the sequential list of ancestral classes of a specific {@code Class}, started from the "top" of the inheritance
     * tree. {@code Object} class is not added to the hierarchy
     * @param targetClass The class to analyze
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
     * Retrieves list of properties of an {@code Annotation} object for which non-default values have been set
     * @param annotation The annotation instance to analyze
     * @return List of {@code Method} instances that represent properties initialized with non-defaults
     */
    public static List<Method> getAnnotationNonDefaultProperties(Annotation annotation) {
        return Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(method -> !annotationPropertyIsDefault(annotation, method))
                .collect(Collectors.toList());
    }

    /**
     * Gets whether all the {@code Annotation}'s properties have default values
     * @param annotation The annotation to analyze
     * @return True or false
     */
    public static boolean annotationIsDefault(Annotation annotation) {
        return Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .allMatch(method -> annotationPropertyIsDefault(annotation, method));
    }

    /**
     * Gets whether an {@code Annotation} property has a value which is not default
     * @param annotation The annotation to analyze
     * @param method The method representing the property
     * @return True or false
     */
    static boolean annotationPropertyIsDefault(Annotation annotation, Method method) {
        try {
            Object defaultValue = method.getDefaultValue();
            if (defaultValue == null) {
                return false;
            }
            Object invocationResult = method.invoke(annotation);
            if (method.getReturnType().isArray() && ArrayUtils.isEmpty((Object[])invocationResult)) {
                return true;
            }
            return defaultValue.equals(invocationResult);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

    static boolean annotationPropertyIsDefault(Annotation annotation, String method) {
        try {
            return annotationPropertyIsDefault(annotation, annotation.annotationType().getDeclaredMethod(method));
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Called by {@link PluginReflectionUtility#getAllFields(Class, List)} to finalize the list of managed fields.
     * As long as any of the fields within the provided collection is marked with {@link Replace},
     * the routine searches for the other referenced fields within the same collection, swaps the current field
     * with the first one of the found, and removes the rest of the found
     * @param allFields The list of fields to apply replacements to
     * @return Modified {@code List<Field>} collection
     */
    private static List<Field> processFieldsOverrides(List<Field> allFields) {
        List<Field> result = new ArrayList<>(allFields);
        Queue<Field> replacingFields = result.stream()
                .filter(field -> field.getDeclaredAnnotation(Replace.class) != null)
                .sorted(Predicates::compareByOrigin)
                .collect(Collectors.toCollection(LinkedList::new));
        while (!replacingFields.isEmpty()) {
            Field replacingField = replacingFields.remove();
            ClassField replaceableClassField = PluginClassFieldUtility.populateDefaults(replacingField.getAnnotation(Replace.class).value(), replacingField);
            Field replaceableField = allFields.stream()
                    .filter(field -> Predicates.isMatchByClassField(field, replaceableClassField))
                    .findFirst()
                    .orElse(null);
            if (replaceableField == null || replaceableField.equals(replacingField)) {
                continue;
            }
            // move the replacing field to the position of replaceable field
            result.remove(replacingField);
            int insertPosition = result.indexOf(replaceableField);
            result.add(insertPosition, replacingField);
            // purge the replaceable field
            result.remove(replaceableField);
            replacingFields.remove(replaceableField); // because a replaceable field may also be declared as "replacing"
        }
        return result;
    }

    /**
     * Converts {@link URI} parameter, such as of a classpath element, to an {@link URL} instance used by {@link Reflections}
     * @param uri {@code URI} value
     * @return {@code URL} value
     */
    private static URL toUrl(URI uri){
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
        return null;
    }

    /**
     * Contains utility methods for manipulating field streams and collections
     */
    public static class Predicates {

        /**
         * A predicate for picking out non-static {@code Field} instances which is by default
         * in {@link PluginReflectionUtility#getAllFields(Class)} routines
         */
        private static final Predicate<Field> NON_STATIC_FIELD_PREDICATE = field -> !Modifier.isStatic(field.getModifiers());

        private Predicates() {
        }

        /**
         * Gets a {@code Predicate<Field>} for sorting out the fields to be ignored
         * @param ignoredFields List of {@link ClassField} representing the fields to be ignored
         * @return A {@code Predicate<Field>} which is affirmative by default, that is, returns *true* if the field is
         * not ignored, and *false* if the field is set to be ignored
         */
        public static Predicate<Field> getNotIgnoredFieldsPredicate(Collection<ClassField> ignoredFields) {
            if (ignoredFields == null || ignoredFields.isEmpty()) {
                return field -> true;
            }
            return field -> ignoredFields.stream().noneMatch(
                    ignoredClassField -> isMatchByClassField(field, ignoredClassField)
            );
        }

        /**
         * Gets a {@code Predicate<Field>} for sorting out the fields to be ignored
         * @param ignoredFields List of {@link Field} instances representing the fields to be ignored
         * @return A {@code Predicate<Field>} which is affirmative by default, that is, returns *true* if the field is
         * not ignored, and *false* if the field is set to be ignored
         */
        private static Predicate<Field> getNotIgnoredFieldsPredicate(List<Field> ignoredFields) {
            if (ignoredFields == null || ignoredFields.isEmpty()) {
                return field -> true;
            }
            return field -> ignoredFields.stream().noneMatch(ignored -> ignored.equals(field));
        }

        /**
         * Generates a combined {@code Predicate<Field>} from the list of partial predicates given
         * @param predicates List of {@code Predicate<Field>} instances
         * @return An {@code AND}-joined combined predicate, or a default all-allowed predicate if no partial predicates provided
         */
        private static Predicate<Field> getCombinedPredicate(List<Predicate<Field>> predicates) {
            if (predicates == null || predicates.isEmpty()) {
                return NON_STATIC_FIELD_PREDICATE;
            }
            return predicates.stream().filter(Objects::nonNull).reduce(NON_STATIC_FIELD_PREDICATE, Predicate::and);
        }

        /**
         * Gets whether the specified {@code Field} matches a {@code @ClassField} signature
         * @param field The {@link Field} to test
         * @param classField {@link ClassField} instance representing
         * @return {@code Predicate<Field>} instance
         */
        private static boolean isMatchByClassField(Field field, ClassField classField) {
            return field.getDeclaringClass().equals(classField.source())
                    && field.getName().equals(classField.field());
        }

        /**
         * Facilitates ordering {@code Field} instances according to their optional {@link DialogField} annotations'
         * ranking values and then their class affiliation
         * @param f1 First comparison member
         * @param f2 Second comparison member
         * @return Integer value per {@code Comparator#compare(Object, Object)} convention
         */
        public static int compareByRanking(Field f1, Field f2)  {
            int rank1 = 0;
            int rank2 = 0;
            if (f1.isAnnotationPresent(DialogField.class)) {
                DialogField dialogField1 = f1.getAnnotationsByType(DialogField.class)[0];
                rank1 = dialogField1.ranking();
            }
            if (f2.isAnnotationPresent(DialogField.class)) {
                DialogField dialogField2 = f2.getAnnotationsByType(DialogField.class)[0];
                rank2 = dialogField2.ranking();
            }
            if (rank1 != rank2) {
                return Integer.compare(rank1, rank2);
            }
            return compareByOrigin(f1, f2);
        }

        /**
         * Facilitates ordering {@code Field} instances according to their class affiliation (if both fields' classes
         * are of the same inheritance tree, a field from the senior class goes first)
         * @param f1 First comparison member
         * @param f2 Second comparison member
         * @return Integer value per {@code Comparator#compare(Object, Object)} convention
         */
        private static int compareByOrigin(Field f1, Field f2) {
            if (f1.getDeclaringClass() != f2.getDeclaringClass()) {
                if (ClassUtils.isAssignable(f1.getDeclaringClass(), f2.getDeclaringClass())) {
                    return 1;
                }
                if (ClassUtils.isAssignable(f2.getDeclaringClass(), f1.getDeclaringClass())) {
                    return -1;
                }
            }
            return 0;
        }
    }
}
