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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Replace;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.api.markers._Super;
import com.exadel.aem.toolkit.plugin.adapters.ClassMemberSetting;
import com.exadel.aem.toolkit.plugin.adapters.MemberRankingSetting;
import com.exadel.aem.toolkit.plugin.sources.ModifiableMemberSource;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Helper class used in {@code Source} processing stream for managing source replacements as set by the user. Designed
 * to work with {@link ClassUtil}
 */
class ReplacementHelper {
    private static final SourceReplacingCollector SOURCE_REPLACING = new SourceReplacingCollector();

    private final List<Source> internal;

    /* ---------------------------------
       Instance constructors and methods
       --------------------------------- */

    /**
     * Default (instantiation-restricting) constructor
     */
    private ReplacementHelper() {
        internal = new ArrayList<>();
    }

    /**
     * Grooms the provided list of {@link Source}s.
     * As long as any of the fields within the provided collection is marked with {@link Replace},
     * the routine searches for the other referenced fields within the same collection, swaps the current field
     * with the first one of the found items, and removes the rest of the found items
     * @return Modified {@code List<Source>} collection
     */
    private List<Source> processInternal() {
        List<Source> result = new ArrayList<>(internal);
        Queue<Source> replacingEntries = result.stream()
            .filter(entry -> entry.tryAdaptTo(Replace.class).isPresent())
            .sorted(OrderingUtil::compareByOrigin)
            .collect(Collectors.toCollection(LinkedList::new));

        while (!replacingEntries.isEmpty()) {

            Source replacingEntry = replacingEntries.remove();
            Replace replace = replacingEntry.adaptTo(Replace.class);

            ClassMemberSetting formerClassMemberAnnotation = new ClassMemberSetting(replace.value())
                .populateDefaults(replacingEntry.adaptTo(MemberSource.class).getDeclaringClass(), replacingEntry.getName());

            Source formerEntry = internal.stream()
                .filter(formerClassMemberAnnotation::matches)
                .findFirst()
                .orElse(null);
            if (formerEntry == null || formerEntry.equals(replacingEntry)) {
                continue;
            }

            // Move the replacing member to the position of the member being replaced
            // We try to find the index of the element being replaced in the resulting list. However, it can be missing
            // from the list (in case there have been several @Replace-s pointing to that particular element, and
            // it has already been replaced with the first of the "contenders"). In such case, we look for the element
            // that has replaced the original one, and replace in with the new "contender".
            // Therefore, the very last @Replace-annotated element is the actual replacement
            result.remove(replacingEntry);
            int insertPosition = result.indexOf(formerEntry);
            boolean alreadyReplaced = insertPosition == -1;
            if (alreadyReplaced) {
                Source alreadyExistingReplacement = result.stream()
                    .filter(source -> isSameReplacement(source, replacingEntry))
                    .findFirst()
                    .orElse(null);
                if (alreadyExistingReplacement != null) {
                    insertPosition = result.indexOf(alreadyExistingReplacement);
                    result.remove(alreadyExistingReplacement);
                } else {
                    insertPosition = result.size();
                }
            }
            result.add(insertPosition, replacingEntry);

            // If the replacing entry has no particular ranking value, assign to it the ranking of the former entry
            if (replacingEntry.adaptTo(MemberRankingSetting.class).isUnset()) {
                int formerRank = formerEntry.adaptTo(MemberRankingSetting.class).getRanking();
                replacingEntry.adaptTo(MemberRankingSetting.class).setRanking(formerRank);
            }

            // Assign the {@code declaringClass} of the entry being replaced to the replacement entry
            replacingEntry
                .adaptTo(ModifiableMemberSource.class)
                .setDeclaringClass(formerEntry.adaptTo(MemberSource.class).getDeclaringClass());

            // Purge the former entry
            result.remove(formerEntry);
            replacingEntries.remove(formerEntry); // because a replaceable member can also be declared as "replacing"
        }
        return result;
    }

    /* ------------------
       Public entry-point
       ------------------ */

    /**
     * Retrieves the {@code Collector} instance performing  replacements in the stream of {@code Source}s as
     * determined by the {@link Replace} annotations managed by the {@code Source} objects. The collector complies
     * with the Java Stream API so that it serves as the terminal operator in a {@code .collect()} method call
     * @return {@code Collector} object
     */
    public static Collector<Source, ReplacementHelper, List<Source>> processSourceReplace() {
        return SOURCE_REPLACING;
    }


    /* ---------------
       Utility methods
       --------------- */

    /**
     * Gets whether the two {@code Source}s claim to replace the same Java class member pointed at by their {@link Replace}
     * annotations
     * @param existing  {@code Source} object that represents the already processed {@link Replace}-annotated class member
     *                  that may have replaced some previous class member
     * @param contender {@code Source} object that represents a {@link Replace}-annotated class member that has yet to
     *                  be processed
     * @return True if both members point to one prevoisly existing class member that needs to be replaced; otherwise
     * false
     */
    private static boolean isSameReplacement(Source existing, Source contender) {
        if (!existing.tryAdaptTo(Replace.class).isPresent() || !contender.tryAdaptTo(Replace.class).isPresent()) {
            return false;
        }
        ClassMember firstMemberName = existing.adaptTo(Replace.class).value();
        ClassMember secondMemberName = contender.adaptTo(Replace.class).value();
        if (!firstMemberName.value().equals(secondMemberName.value())) {
            return false;
        }
        return existing.adaptTo(MemberSource.class).getDeclaringClass()
            .equals(getReferencedReplacementClass(contender));
    }

    /**
     * Called by {@link ReplacementHelper#isSameReplacement(Source, Source)} to extract a reference to the class specified
     * in the {@link Replace} annotation of the current Java class member, either directly or via a pointer interface,
     * such as {@link _Super} or {@link _Default}
     * @param source {@code Source} object that represents a {@link Replace}-annotated class member
     * @return {@code Class} reference
     */
    private static Class<?> getReferencedReplacementClass(Source source) {
        Class<?> result = source.adaptTo(Replace.class).value().source();
        if (_Default.class.equals(result)) {
            return source.adaptTo(MemberSource.class).getDeclaringClass();
        }
        if (_Super.class.equals(result)) {
            return source.adaptTo(MemberSource.class).getDeclaringClass().getSuperclass();
        }
        return result;
    }

    /* ---------------
       Utility classes
       --------------- */

    /**
     * Implements {@code Collector<Source, ReplacementHelper, List<Source>>} to run the replacements as determined by
     * the {@link Replace} annotations managed by the {@code Source} objects
     */
    private static class SourceReplacingCollector implements Collector<Source, ReplacementHelper, List<Source>> {
        /**
         * {@inheritDoc}
         */
        @Override
        public Supplier<ReplacementHelper> supplier() {
            return ReplacementHelper::new;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BiConsumer<ReplacementHelper, Source> accumulator() {
            return (left, right) -> left.internal.add(right);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BinaryOperator<ReplacementHelper> combiner() {
            return (left, right) -> {
                left.internal.addAll(right.internal);
                return left;
            };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Function<ReplacementHelper, List<Source>> finisher() {
            return ReplacementHelper::processInternal;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Characteristics> characteristics() {
            return Sets.immutableEnumSet(Characteristics.UNORDERED);
        }
    }
}
