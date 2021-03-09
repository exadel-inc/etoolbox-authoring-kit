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

package com.exadel.aem.toolkit.plugin.util.stream;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.adapters.ClassMemberSetting;

/**
 * Helper class used in {@code Source} processing stream for filtering source entries
 */
public class Filter {

    /**
     * Default (hiding) constructor
     */
    private Filter() {
    }

    /**
     * Generates a combined {@code Predicate<Member>} from the list of partial predicates given
     * @param predicates List of {@code Predicate<Member>} instances
     * @return An {@code AND}-joined combined predicate, or a default all-allowed predicate if no partial predicates provided
     */
    public static Predicate<Source> getSourcesPredicate(List<Predicate<Source>> predicates) {
        if (predicates == null || predicates.isEmpty()) {
            return Source::isValid;
        }
        return predicates.stream().filter(Objects::nonNull).reduce(Source::isValid, Predicate::and);
    }

    /**
     * Gets a predicate for sorting out the fields set to be ignored
     * @param memberPointers List of {@link ClassMemberSetting} objects representing the ignored class members
     * @return A {@code Predicate<Source>} which is affirmative by default, that is, returns <i>true</i> if the field
     * is not ignored, and <i>false</i> if the field is set to be ignored
     */
    public static Predicate<Source> getNotIgnoredSourcesPredicate(List<? extends ClassMemberSetting> memberPointers) {
        if (memberPointers == null || memberPointers.isEmpty()) {
            return member -> true;
        }
        return source -> memberPointers.stream().noneMatch(memberPointer -> memberPointer.matches(source));
    }
}
