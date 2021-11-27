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
package com.exadel.aem.toolkit.plugin.handlers.containers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.adapters.ResourceTypeSetting;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.sources.ModifiableMemberSource;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Contains helper methods for the {@link PlacementHelper} that resolve collisions either between same-named Java class
 * members of a parent and a child class or between a field and a method sharing the same name. These methods are aimed
 * at avoiding ambiguities in naming and/or reporting to the user of potential rendering problems
 */
class PlacementCollisionSolver {

    private static final String TYPE_FIELD = "Field";
    private static final String TYPE_METHOD = "Method";

    private static final String NAMING_COLLISION_MESSAGE_TEMPLATE = "%s named \"%s\" in class \"%s\" " +
        "collides with the %s named \"%s\" in class \"%s\" (%s). This may cause unexpected behavior";

    private static final String REASON_AMBIGUOUS_ORDER = "attributes of the parent class member will have precedence";
    private static final String REASON_DIFFERENT_RESTYPE = "different resource types provided";

    private PlacementCollisionSolver() {
    }

    /* ---------------------
       Collisions management
       ---------------------*/
    /**
     * Tests the provided collection of members for possible collisions (Java class members that produce the same tag
     * name), and throws an exception if: <br> - a member from a superclass is positioned after the same-named member
     * from a subclass, therefore, will "shadow" it and produce unexpected UI display; <br> - a member from a class has
     * a resource type other than of a same-named member from a superclass or interface, therefore, is at risk of
     * producing a "mixed" markup
     * @param sources {@code List} of sources available for rendering
     */
    public static void checkForCollisions(List<Source> sources) {
        List<String> distinctNames = sources
            .stream()
            .map(Source::getName)
            .map(NamingUtil::stripGetterPrefix)
            .distinct()
            .collect(Collectors.toList());
        if (distinctNames.size() ==  sources.size()) {
            // All names are different: there are no collisions
            return;
        }
        for (String name : distinctNames) {
            checkForNameCollisions(sources, name);
            checkForResourceTypeCollisions(sources, name);
        }
    }

    /**
     * Tests the provided collection of member sources sharing the particular name for naming collisions. If a collision
     * is found, the {@link InvalidLayoutException} is thrown
     * @param sources {@code List} of sources available for rendering
     * @param name    String representing a common name of sources being tested
     */
    private static void checkForNameCollisions(List<Source> sources, String name) {
        LinkedList<Source> sameNameMembers = getMembersWithSameName(sources, name);
        LinkedList<Source> sameNameMembersByOrigin = sameNameMembers
            .stream()
            .sorted(OrderingUtil::compareByOrigin)
            .collect(Collectors.toCollection(LinkedList::new));

        if (!sameNameMembers.getLast().equals(sameNameMembersByOrigin.getLast())) {
            reportCollision(
                sameNameMembersByOrigin.getLast().adaptTo(MemberSource.class),
                sameNameMembers.getLast().adaptTo(MemberSource.class),
                REASON_AMBIGUOUS_ORDER);
        }

    }

    /**
     * Tests the provided collection of member sources sharing the particular name for collisions in exposed resource
     * types. If a collision is found, the {@link InvalidLayoutException} is thrown
     * @param sources {@code List} of sources available for rendering
     * @param name    String representing the common name of the sources being tested
     */
    private static void checkForResourceTypeCollisions(List<Source> sources, String name) {
        List<Source> sameNameMembers = getMembersWithSameName(sources, name);

        // We consider fields separately from methods
        // because we allow that a field, and a method of the same name have different resource types
        Predicate<Source> fieldPredicate = PlacementCollisionSolver::isField;
        Predicate<Source> methodPredicate = PlacementCollisionSolver::isMethod;
        for (Predicate<Source> currentMemberType : Arrays.asList(fieldPredicate, methodPredicate)) {

            Map<String, Source> membersByResourceType = sameNameMembers
                .stream()
                .filter(currentMemberType)
                .collect(Collectors.toMap(
                    source -> source.adaptTo(ResourceTypeSetting.class).getValue(),
                    source -> source,
                    (first, second) -> second,
                    LinkedHashMap::new));
            if (membersByResourceType.size() > 1) {
                Source[] competitorArray = membersByResourceType.values().toArray(new Source[0]);
                reportCollision(
                    competitorArray[1].adaptTo(MemberSource.class),
                    competitorArray[0].adaptTo(MemberSource.class),
                    REASON_DIFFERENT_RESTYPE);
            }
        }
    }

    /**
     * Throws a formatted exception whenever a collision is found
     * @param first  {@code MemberSource} instance representing the first member of a collision
     * @param second {@code MemberSource} instance representing the second member of a collision
     * @param reason String explaining the essence of the collision
     */
    private static void reportCollision(MemberSource first, MemberSource second, String reason) {
        PluginRuntime
            .context()
            .getExceptionHandler()
            .handle(new InvalidLayoutException(String.format(
                NAMING_COLLISION_MESSAGE_TEMPLATE,
                isField(first) ? TYPE_FIELD : TYPE_METHOD,
                first.getName(),
                first.getDeclaringClass().getSimpleName(),
                isField(second) ? TYPE_FIELD.toLowerCase() : TYPE_METHOD.toLowerCase(),
                second.getName(),
                second.getDeclaringClass().getSimpleName(),
                reason)));
    }

    /* ------------------------------
       Naming coincidences management
       ------------------------------ */

    /**
     * Checks for cases when sources intended to be placed in the same container differ in {@code type} (e.g., one is a
     * Java method, another is field) but share the same name. If same-named sources have different resource types, the
     * tag names of field-bound sources are left the same while the methods' names are changed. <p>This is the special
     * case which allows the user to place a "service" annotation such as {@code @Heading} on the class or interface
     * method while placing a "traditional" annotation such as {@code @TextField} on the field</p>
     * @param sources List of sources, such as members of a Java class
     */
    public static void resolveFieldMethodNameCoincidences(List<Source> sources) {
        List<Source> fields = sources
            .stream()
            .filter(PlacementCollisionSolver::isField)
            .collect(Collectors.toList());

        for (Source currentField : fields) {
            List<Source> methodsToRename = getMembersWithSameName(
                sources,
                currentField.getName(),
                member -> isMethod(member)
                    && isSameOrSuperClass(currentField, member)
                    && hasDifferentResourceType(currentField, member)
            );
            if (methodsToRename.isEmpty()) {
                continue;
            }
            Map<String, List<Source>> methodGroupsByResourceType = new HashMap<>();
            methodsToRename.forEach(method -> methodGroupsByResourceType.computeIfAbsent(
                method.adaptTo(ResourceTypeSetting.class).getValue(),
                key -> new ArrayList<>())
                .add(method));

            for (Map.Entry<String, List<Source>> methodGroupByResourceType : methodGroupsByResourceType.entrySet()) {
                List<Source> methodGroup = methodGroupByResourceType.getValue();
                String simpleResourceType = StringUtils.substringAfterLast(methodGroupByResourceType.getKey(), CoreConstants.SEPARATOR_SLASH);
                String newName = currentField.getName() + CoreConstants.SEPARATOR_UNDERSCORE + simpleResourceType.toLowerCase();
                methodGroup.forEach(method -> method.adaptTo(ModifiableMemberSource.class).setName(newName));
            }
        }
    }

    /**
     * Detects whether the two provided {@code Source}s represent classes that are the same or else are
     * in the "child - parent" relation
     * @param first  {@code Source} instance representing a class member
     * @param second {@code Source} instance representing a class member
     * @return True or false
     */
    private static boolean isSameOrSuperClass(Source first, Source second) {
        Class<?> firstClass = first.adaptTo(MemberSource.class).getDeclaringClass();
        Class<?> secondClass = second.adaptTo(MemberSource.class).getDeclaringClass();
        return ClassUtils.isAssignable(firstClass, secondClass);
    }

    /**
     * Detects whether the two provided {@code Source}s represent Granite UI components with the different resource types
     * @param first  {@code Source} instance representing a class member
     * @param second {@code Source} instance representing a class member
     * @return True or false
     */
    private static boolean hasDifferentResourceType(Source first, Source second) {
        return !first.adaptTo(ResourceTypeSetting.class).getValue().equals(second.adaptTo(ResourceTypeSetting.class).getValue());
    }

    /* ----------------------
       Common utility methods
       ----------------------*/

    /**
     * Retrieves the list of {@code Source} objects matching the provided name. Names of fields and methods are coerced,
     * e.g., both {@code private String text;} and {@code public String getText() {...}} are considered sharing the same
     * name
     * @param sources {@code List} of sources available for rendering
     * @param name    String representing the common name of sources to select
     * @return An ordered list of {@code Source} objects
     */
    private static LinkedList<Source> getMembersWithSameName(List<Source> sources, String name) {
        return getMembersWithSameName(sources, name, null);
    }

    /**
     * Retrieves the list of {@code Source} objects matching the provided name. Names of fields and methods are coerced,
     * e.g., both {@code private String text;} and {@code public String getText() {...}} are considered sharing the same
     * name
     * @param sources {@code List} of sources available for rendering
     * @param name    String representing the common name of sources to select
     * @param filter  Nullable {@code Predicate} used as the additional filter of matching sources
     * @return An ordered list of {@code Source} objects
     */
    private static LinkedList<Source> getMembersWithSameName(List<Source> sources, String name, Predicate<Source> filter) {
        return sources
            .stream()
            .filter(source -> StringUtils.equals(NamingUtil.stripGetterPrefix(source.getName()), name))
            .filter(source -> filter == null || filter.test(source))
            .map(source -> source.adaptTo(MemberSource.class))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Used by the naming coincidences resolution logic to detect whether the current {@code Source} instance represents
     * a Java class field
     * @param source {@code Source} object
     * @return True or false
     */
    private static boolean isField(Source source) {
        return source.adaptTo(Field.class) != null;
    }

    /**
     * Used by the naming coincidences resolution logic to detect whether the current {@code Source} instance represents
     * a Java class method
     * @param source {@code Source} object
     * @return True or false
     */
    private static boolean isMethod(Source source) {
        return source.adaptTo(Method.class) != null;
    }
}
