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

import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Replace;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.adapters.ClassMemberSetting;
import com.exadel.aem.toolkit.plugin.adapters.MemberRankingSetting;
import com.exadel.aem.toolkit.plugin.utils.ordering.OrderingUtil;

/**
 * Helper class used in {@code Source} processing stream for managing source replacements as set by the user. Designed
 * to work with {@link ClassUtil}
 */
class ReplacementHelper {
    private static final SourceReplacingCollector SOURCE_REPLACING = new SourceReplacingCollector();

    private final List<Source> internal;

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
            .filter(entry -> entry.adaptTo(Replace.class) != null)
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

            // Move the replacing member to the position of replaceable member
            result.remove(replacingEntry);
            int insertPosition = result.indexOf(formerEntry);
            result.add(insertPosition, replacingEntry);

            // If the replacing entry has no particular ranking value, assign to it the ranking of the former entry
            if (replacingEntry.adaptTo(MemberRankingSetting.class).isUnset()) {
                int formerRank = formerEntry.adaptTo(MemberRankingSetting.class).getRanking();
                replacingEntry.adaptTo(MemberRankingSetting.class).setRanking(formerRank);
            }

            // Purge the former entry
            result.remove(formerEntry);
            replacingEntries.remove(formerEntry); // because a replaceable member can also be declared as "replacing"
        }
        return result;
    }

    /**
     * Retrieves the {@code Collector} instance performing  replacements in the stream of {@code Source}s as
     * determined by the {@link Replace} annotations managed by the {@code Source} objects. The collector complies
     * with the Java Stream API so that it serves as the terminal operator in a {@code .collect()} method call
     * @return {@code Collector} object
     */
    public static Collector<Source, ReplacementHelper, List<Source>> processSourceReplace() {
        return SOURCE_REPLACING;
    }

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
