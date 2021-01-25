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

import java.lang.reflect.Member;

import org.apache.commons.lang3.ClassUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.adapters.MemberRankingSetting;

/**
 * Utility class that exposes comparison routines used to order {@code Source} instances by their origin or explicit ranking
 */
public class Sorter {

    /**
     * Default private (hiding) constructor
     */
    private Sorter() {
    }

    /**
     * Facilitates ordering {@code Member} instances according to their optional {@link DialogField} annotations'
     * ranking values and then their class affiliation
     * @param f1 First comparison member
     * @param f2 Second comparison member
     * @return Integer value per {@code Comparator#compare(Object, Object)} convention
     */
    public static int compareByRank(Source f1, Source f2)  {
        int rank1 = f1.adaptTo(MemberRankingSetting.class).getRanking();
        int rank2 = f2.adaptTo(MemberRankingSetting.class).getRanking();
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

    /**
     * Facilitates ordering {@code Source} instances according to their class affiliation (if both fields' classes
     * are of the same inheritance tree, a field from the senior class goes first)
     * @param f1 First comparison member
     * @param f2 Second comparison member
     * @return Integer value per {@code Comparator#compare(Object, Object)} convention
     */
    public static int compareByOrigin(Source f1, Source f2) {
        Class<?> f1Class = f1.adaptTo(Member.class).getDeclaringClass();
        Class<?> f2Class = f2.adaptTo(Member.class).getDeclaringClass();
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
}
