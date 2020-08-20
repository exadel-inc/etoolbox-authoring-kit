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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.ClassUtils;

import com.exadel.aem.toolkit.api.annotations.main.ClassField;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;

/**
 * Contains utility methods for manipulating field streams and collections
 */
public class PluginObjectPredicates {
    private PluginObjectPredicates() {
    }

    /**
     * A predicate for picking out non-static {@code Field} instances which is by default
     * in {@link PluginReflectionUtility#getAllFields(Class)} routines
     */
    private static final java.util.function.Predicate<Field> NON_STATIC_FIELD_PREDICATE = field -> !Modifier.isStatic(field.getModifiers());

    /**
     * Gets a predicate for sorting out the fields set to be ignored
     * @param ignoredFields List of {@link ClassField} representing the fields set to be ignored
     * @return A {@code Predicate<Field>} which is affirmative by default, that is, returns *tru* if the field is not
     * ignored, and *false* if the field is set to be ignored
     */
    public static Predicate<Field> getNotIgnoredFieldsPredicate(List<ClassField> ignoredFields) {
        if (ignoredFields == null || ignoredFields.isEmpty()) {
            return field -> true;
        }
        return field -> ignoredFields.stream().noneMatch(
                ignoredField -> ignoredField.source().equals(field.getDeclaringClass())
                        && ignoredField.field().equals(field.getName())
        );
    }

    /**
     * Generates an combined {@code Predicate<Field>} from the list of partial predicates given
     * @param predicates List of {@code Predicate<Field>} instances
     * @return An {@code AND}-joined combined predicate, or a default all-allowed predicate if no partial predicates provided
     */
    static Predicate<Field> getFieldsPredicate(List<Predicate<Field>> predicates) {
        if (predicates == null || predicates.isEmpty()) {
            return NON_STATIC_FIELD_PREDICATE;
        }
        return predicates.stream().filter(Objects::nonNull).reduce(NON_STATIC_FIELD_PREDICATE, Predicate::and);
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
    static int compareByOrigin(Field f1, Field f2) {
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
