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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

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

    private static final String WILDCARD = ".*";

    /**
     * Default (instantiation-restricting) constructor
     */
    private ClassUtil() {
    }

    /**
     * Gets whether the given {@code Class} conforms to one of the provided references that represent package names
     * @param sourceClass The class to check
     * @param references  Zero or more string values; non-blank strings are expected
     * @return True or false
     */
    public static boolean matchesReference(Class<?> sourceClass, String... references) {
        if (ArrayUtils.isEmpty(references)) {
            return true;
        }
        return Arrays.stream(references)
            .filter(StringUtils::isNotBlank)
            .map(ref -> ref.endsWith(WILDCARD) ? ref.substring(0, ref.length() - WILDCARD.length()) : ref)
            .anyMatch(ref -> sourceClass.getName().startsWith(ref + DialogConstants.SEPARATOR_DOT));
    }

    /**
     * Retrieves a sequential list of {@link Source} objects representing manageable members that belong to a certain
     * {@code Class} and its superclasses
     * @param sourceClass The class to extract sources from
     * @return List of {@code Source} objects
     */
    public static List<Source> getSources(Class<?> sourceClass) {
        return getSources(sourceClass, null, null, true);
    }

    /**
     * Retrieves a list of {@link Source} objects representing manageable members that belong to a certain {@code Class}
     * (and its superclasses) and match the criteria represented by a {@code Predicate}
     * @param sourceClass  The class to extract sources from
     * @param sourceMember
     * @param condition    Nullable {@code Predicate<Member>} instance that helps to pick up appropriate fields and
     *                     methods
     * @param ordered      True to perform proper sources ordering (considers the {@code @DialogField(ranking=..,)}
     *                     values, and then the {@code @Place(before/after=...)} instructions). False to skip ordering
     * @return List of {@code Source} objects
     */
    @SuppressWarnings("deprecation") // Processing of IgnoreFields is retained for compatibility and will be removed
    // in a version after 2.0.2
    public static List<Source> getSources(
        Class<?> sourceClass,
        Member sourceMember,
        Predicate<Source> condition,
        boolean ordered) {
        List<Source> raw = new ArrayList<>();
        List<ClassMemberSetting> ignoredClassMembers = new ArrayList<>();

        for (Class<?> classEntry : getInheritanceTree(sourceClass)) {

            Stream<Member> classMembersStream = sourceClass.isInterface()
                ? Arrays.stream(classEntry.getMethods())
                : Stream.concat(Arrays.stream(classEntry.getDeclaredFields()), Arrays.stream(classEntry.getDeclaredMethods()));
            List<Source> classMemberSources = classMembersStream
                .map(member -> Sources.fromMember(member, sourceClass, sourceMember))
                .filter(source -> source.isValid() && (condition == null || condition.test(source)))
                .collect(Collectors.toList());
            raw.addAll(classMemberSources);

            if (classEntry.getAnnotation(Ignore.class) != null && classEntry.getAnnotation(Ignore.class).members().length > 0) {
                Arrays.stream(classEntry.getAnnotation(Ignore.class).members())
                    .map(classMember -> new ClassMemberSetting(classMember).populateDefaults(sourceClass, classEntry.getName()))
                    .forEach(ignoredClassMembers::add);
            } else if (classEntry.getAnnotation(IgnoreFields.class) != null) {
                Arrays.stream(classEntry.getAnnotation(IgnoreFields.class).value())
                    .map(classMember -> new ClassMemberSetting(classMember).populateDefaults(sourceClass))
                    .forEach(ignoredClassMembers::add);
            }
        }

        List<Source> reducedWithReplacements = raw
            .stream()
            .filter(source -> ignoredClassMembers.stream().noneMatch(ignored -> ignored.matches(source)))
            .collect(ReplacementHelper.processSourceReplace());

        return ordered
            ? OrderingUtil.sortMembers(reducedWithReplacements)
            : reducedWithReplacements;
    }

    /**
     * Retrieves a list of ancestors of a specific {@code Class}, target class itself included, starting from the "top"
     * of the inheritance tree. {@code Object} class is not added to the hierarchy
     * @param sourceClass The class to build the tree upon
     * @return List of {@code Class} objects
     */
    public static List<Class<?>> getInheritanceTree(Class<?> sourceClass) {
        return getInheritanceTree(sourceClass, true);
    }

    /**
     * Retrieves a list of ancestors of a specific {@code Class} starting from the "top" of the inheritance tree. {@code
     * Object} class is not added to the hierarchy
     * @param sourceClass    The class to analyze
     * @param includeCurrent Whether to include the {@code targetClass} itself to the hierarchy
     * @return List of {@code Class} objects
     */
    public static List<Class<?>> getInheritanceTree(Class<?> sourceClass, boolean includeCurrent) {
        List<Class<?>> result = new LinkedList<>();
        Class<?> current = sourceClass;
        while (current != null && !current.equals(Object.class)) {
            if (!current.equals(sourceClass) || includeCurrent) {
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
     * @param sourceClass {@code Class<?>} object representing the constants class
     * @return {@code Map<String, Object>} containing the key-value pairs. An empty map can be returned if no valid
     * constants found
     */
    public static Map<String, Object> getConstantValues(Class<?> sourceClass) {
        Map<String, Object> result = new HashMap<>();
        for (Field field : sourceClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())
                || !Modifier.isFinal(field.getModifiers())
                || !Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            Object fieldValue;
            try {
                fieldValue = field.get(sourceClass);
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
