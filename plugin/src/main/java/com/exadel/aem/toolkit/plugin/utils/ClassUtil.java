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
package com.exadel.aem.toolkit.plugin.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.adapters.ClassMemberSetting;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Contains utility methods for parsing AEM components' classes and extracting information related to UI rendering
 */
public class ClassUtil {

    /**
     * Default (instantiation-restricting) constructor
     */
    private ClassUtil() {
    }

    /**
     * Retrieves a sequential list of {@link Source} objects representing manageable members that belong to
     * a certain {@code Class} and its superclasses
     * @param targetClass The class to extract sources from
     * @return List of {@code Source} objects
     */
    public static List<Source> getSources(Class<?> targetClass) {
        return getSources(targetClass, null);
    }

    /**
     * Retrieves a sequential list of {@link Source} objects representing manageable members that belong to
     * a certain {@code Class} (and its superclasses) and match the provided criteria represented by a {@code Predicate}
     * @param targetClass The class to extract sources from
     * @param condition   Nullable {@code Predicate<Member>} instance that helps to pick up appropriate fields and methods
     * @return List of {@code Source} objects
     */
    @SuppressWarnings("deprecation") // Processing of IgnoreFields is retained for compatibility and will be removed
                                     // in a version after 2.0.2
    public static List<Source> getSources(Class<?> targetClass, Predicate<Source> condition) {
        List<Source> raw = new ArrayList<>();
        List<ClassMemberSetting> ignoredClassMembers = new ArrayList<>();

        for (Class<?> classEntry : getInheritanceTree(targetClass)) {

            Stream<Member> classMembersStream = targetClass.isInterface()
                ? Arrays.stream(classEntry.getMethods())
                : Stream.concat(Arrays.stream(classEntry.getDeclaredFields()), Arrays.stream(classEntry.getDeclaredMethods()));
            List<Source> classMemberSources = classMembersStream
                .map(member -> Sources.fromMember(member, targetClass))
                .filter(source -> source.isValid() && (condition == null || condition.test(source)))
                .collect(Collectors.toList());
            raw.addAll(classMemberSources);

            if (classEntry.getAnnotation(Ignore.class) != null && classEntry.getAnnotation(Ignore.class).members().length > 0) {
                Arrays.stream(classEntry.getAnnotation(Ignore.class).members())
                    .map(classMember -> new ClassMemberSetting(classMember).populateDefaults(targetClass, classEntry.getName()))
                    .forEach(ignoredClassMembers::add);
            } else if (classEntry.getAnnotation(IgnoreFields.class) != null) {
                Arrays.stream(classEntry.getAnnotation(IgnoreFields.class).value())
                    .map(classMember -> new ClassMemberSetting(classMember).populateDefaults(targetClass))
                    .forEach(ignoredClassMembers::add);
            }
        }

        List<Source> reducedWithReplacements = raw
            .stream()
            .collect(ReplacementHelper.processSourceReplace());
        List<Source> preSortedByRank = reducedWithReplacements
            .stream()
            .filter(source -> ignoredClassMembers.stream().noneMatch(ignored -> ignored.matches(source)))
            .sorted(OrderingUtil::compareByRank)
            .collect(Collectors.toList());

        return OrderingUtil.sortMembers(preSortedByRank);
    }

    /**
     * Retrieves a list of ancestors of a specific {@code Class}, target class itself included,
     * starting from the "top" of the inheritance tree. {@code Object} class is not added to the hierarchy
     * @param targetClass The class to build the tree upon
     * @return List of {@code Class} objects
     */
    public static List<Class<?>> getInheritanceTree(Class<?> targetClass) {
        return getInheritanceTree(targetClass, true);
    }

    /**
     * Retrieves a list of ancestors of a specific {@code Class} starting from the "top" of the inheritance
     * tree. {@code Object} class is not added to the hierarchy
     * @param targetClass   The class to analyze
     * @param includeTarget Whether to include the {@code targetClass} itself to the hierarchy
     * @return List of {@code Class} objects
     */
    public static List<Class<?>> getInheritanceTree(Class<?> targetClass, boolean includeTarget) {
        List<Class<?>> result = new LinkedList<>();
        Class<?> current = targetClass;
        while (current != null && !current.equals(Object.class)) {
            if (!current.equals(targetClass) || includeTarget) {
                result.add(current);
                result.addAll(Arrays.asList(current.getInterfaces()));
            }
            current = current.getSuperclass();
        }
        Collections.reverse(result);
        return result;
    }

    /**
     * Retrieves values of public constant fields originating from the given constant class as a key-value map
     * @param targetClass {@code Class<?>} object representing the constants class
     * @return {@code Map<String, Object>} containing the key-value pairs. An empty map can be returned if no valid
     * constants found
     */
    public static Map<String, Object> getConstantValues(Class<?> targetClass) {
        Map<String, Object> result = new HashMap<>();
        for (Field field : targetClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())
                || !Modifier.isFinal(field.getModifiers())
                || !Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            Object fieldValue;
            try {
                fieldValue = field.get(targetClass);
            } catch (IllegalAccessException e) {
                fieldValue = null;
            }
            if (fieldValue != null) {
                result.put(field.getName(), fieldValue);
            }
        }
        return result;
    }
}
