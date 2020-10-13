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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import com.exadel.aem.toolkit.core.SourceFacadeImpl;
import org.apache.commons.lang3.ClassUtils;

import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;

/**
 * Contains utility methods for manipulating field streams and collections
 */
public class PluginObjectPredicates {
    private PluginObjectPredicates() {
    }

    /**
     * A predicate for picking out non-static {@code Field} instances which is by default
     * in {@link PluginReflectionUtility#getAllMembers(Class)} routines
     */
    private static final java.util.function.Predicate<Member> VALID_MEMBER_PREDICATE = member ->
            ((member instanceof Field && !member.getDeclaringClass().isInterface())
                    || (member instanceof Method && member.getDeclaringClass().isInterface()))
                    && !Modifier.isStatic(member.getModifiers());

    /**
     * Gets a predicate for sorting out the fields set to be ignored
     * @param ignoredMembers List of {@link ClassMember} representing the fields set to be ignored
     * @return A {@code Predicate<Field>} which is affirmative by default, that is, returns *tru* if the field is not
     * ignored, and *false* if the field is set to be ignored
     */
    public static Predicate<Member> getNotIgnoredMembersPredicate(List<ClassMember> ignoredMembers) {
        if (ignoredMembers == null || ignoredMembers.isEmpty()) {
            return field -> true;
        }
        return field -> ignoredMembers.stream().noneMatch(
                ignoredMember -> ignoredMember.source().equals(field.getDeclaringClass())
                        && ignoredMember.member().equals(field.getName())
        );
    }

    /**
     * Generates an combined {@code Predicate<Field>} from the list of partial predicates given
     * @param predicates List of {@code Predicate<Field>} instances
     * @return An {@code AND}-joined combined predicate, or a default all-allowed predicate if no partial predicates provided
     */
    static Predicate<Member> getMembersPredicate(List<Predicate<Member>> predicates) {
        if (predicates == null || predicates.isEmpty()) {
            return VALID_MEMBER_PREDICATE;
        }
        return predicates.stream().filter(Objects::nonNull).reduce(VALID_MEMBER_PREDICATE, Predicate::and);
    }

    /**
     * Facilitates ordering {@code Field} instances according to their optional {@link DialogField} annotations'
     * ranking values and then their class affiliation
     * @param f1 First comparison member
     * @param f2 Second comparison member
     * @return Integer value per {@code Comparator#compare(Object, Object)} convention
     */
    public static int compareByRanking(SourceFacade f1, SourceFacade f2)  {
        int rank1 = 0;
        int rank2 = 0;
        if (f1.adaptTo(DialogField.class) != null) {
            DialogField dialogField1 = f1.adaptTo(DialogField.class);
            rank1 = dialogField1.ranking();
        }
        if (f2.adaptTo(DialogField.class) != null) {
            DialogField dialogField2 = f2.adaptTo(DialogField.class);
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
    static int compareByOrigin(SourceFacade f1, SourceFacade f2) {
        Class<?> f1Class = ((Member) f1.getSource()).getDeclaringClass();
        Class<?> f2Class = ((Member) f2.getSource()).getDeclaringClass();
        if (f1Class != f2Class) {
            if (ClassUtils.isAssignable(f1Class, f2Class)) {
                return 1;
            }
            if (ClassUtils.isAssignable(f2Class, f1Class)) {
                return -1;
            }
        }
        return 0;
    }

    public static int compareDialogMembers(Member f1, Member f2)  {
        DialogField dialogField1 = new SourceFacadeImpl(f1).adaptTo(DialogField.class);
        int rank1 = dialogField1 != null ? dialogField1.ranking() : 0;
        DialogField dialogField2 = new SourceFacadeImpl(f2).adaptTo(DialogField.class);
        int rank2 = dialogField2 != null ? dialogField2.ranking() : 0;

        if (rank1 != rank2) {
            return Integer.compare(rank1, rank2);
        }
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
