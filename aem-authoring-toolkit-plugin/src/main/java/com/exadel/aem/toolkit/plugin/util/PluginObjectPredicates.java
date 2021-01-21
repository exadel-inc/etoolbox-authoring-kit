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

package com.exadel.aem.toolkit.plugin.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.ClassUtils;

import com.exadel.aem.toolkit.api.annotations.main.ClassField;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.source.SourceBase;

/**
 * Contains utility methods for manipulating field streams and collections
 */
public class PluginObjectPredicates {
    private PluginObjectPredicates() {
    }

    /* ---------
       Constants
       --------- */

    /**
     * A predicate for picking out non-static {@code Member} instances which is by default
     * in {@link PluginReflectionUtility#getAllMembers(Class)} routines
     */
    private static final java.util.function.Predicate<Member> VALID_MEMBER_PREDICATE = member ->
            ((member instanceof Field && !member.getDeclaringClass().isInterface())
                    || (member instanceof Method))
                    && !Modifier.isStatic(member.getModifiers());


    /* ---------------
       Utility methods
       --------------- */


    /**
     * Generates an combined {@code Predicate<Member>} from the list of partial predicates given
     * @param predicates List of {@code Predicate<Member>} instances
     * @return An {@code AND}-joined combined predicate, or a default all-allowed predicate if no partial predicates provided
     */
    static Predicate<Member> getMembersPredicate(List<Predicate<Member>> predicates) {
        if (predicates == null || predicates.isEmpty()) {
            return VALID_MEMBER_PREDICATE;
        }
        return predicates.stream().filter(Objects::nonNull).reduce(VALID_MEMBER_PREDICATE, Predicate::and);
    }

    /**
     * Gets a predicate for sorting out the fields set to be ignored
     * @param memberPointers List of {@link ClassMember} or {@link ClassField} annotations representing the fields
     *                       set to be ignored
     * @return A {@code Predicate<Member>} which is affirmative by default, that is, returns *tru* if the field is not
     * ignored, and *false* if the field is set to be ignored
     */
    public static Predicate<Member> getNotIgnoredMembersPredicate(List<? extends Annotation> memberPointers) {
        if (memberPointers == null || memberPointers.isEmpty()) {
            return member -> true;
        }
        return member -> memberPointers.stream().noneMatch(ptr -> isMatch(member, ptr));
    }

    /**
     * Gets whether a field or a method corresponds to the provided pointer structure, such as a {@link ClassMember}
     * @param member {@code Member} object representing Java field or method
     * @param memberPointer {@code Annotation} containing data that points to a class member
     * @return True or false
     */
    private static boolean isMatch(Member member, Annotation memberPointer) {
        if (memberPointer instanceof ClassMember) {
            ClassMember classMember = (ClassMember) memberPointer;
            return classMember.source().equals(member.getDeclaringClass())
                && classMember.name().equals(member.getName());
        } else {
            ClassField classField = (ClassField) memberPointer;
            return classField.source().equals(member.getDeclaringClass())
                && classField.field().equals(member.getName());
        }
    }

    /**
     * Facilitates ordering {@code Member} instances according to their optional {@link DialogField} annotations'
     * ranking values and then their class affiliation
     * @param f1 First comparison member
     * @param f2 Second comparison member
     * @return Integer value per {@code Comparator#compare(Object, Object)} convention
     */
    public static int compareByRanking(Source f1, Source f2)  {
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
     * Facilitates ordering {@code Member} instances according to their class affiliation (if both fields' classes
     * are of the same inheritance tree, a field from the senior class goes first)
     * @param f1 First comparison member
     * @param f2 Second comparison member
     * @return Integer value per {@code Comparator#compare(Object, Object)} convention
     */
    static int compareByOrigin(Source f1, Source f2) {
        Class<?> f1Class = f1.getProcessedClass();
        Class<?> f2Class = f2.getProcessedClass();
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
        DialogField dialogField1 = SourceBase.fromMember(f1, null).adaptTo(DialogField.class);
        int rank1 = dialogField1 != null ? dialogField1.ranking() : 0;
        DialogField dialogField2 = SourceBase.fromMember(f2, null).adaptTo(DialogField.class);
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
