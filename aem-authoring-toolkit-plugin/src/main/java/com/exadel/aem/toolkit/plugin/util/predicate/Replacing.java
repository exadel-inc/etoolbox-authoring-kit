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

package com.exadel.aem.toolkit.plugin.util.predicate;

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

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Replace;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.adapters.ClassMemberSettings;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginReflectionUtility;

public class Replacing {
    private static final SourceReplacingCollector SOURCE_REPLACING = new SourceReplacingCollector();

    private final List<Source> internal = new ArrayList<>();

    /**
     * Grooms the provided list of {@link Source}s.
     * As long as any of the fields within the provided collection is marked with {@link Replace},
     * the routine searches for the other referenced fields within the same collection, swaps the current field
     * with the first one of the found, and removes the rest of the found
     * @return Modified {@code List<Source>} collection
     */
    private List<Source> processInternal() {
        List<Source> result = new ArrayList<>(internal);
        Queue<Source> replacingEntries = result.stream()
            .filter(entry -> entry.adaptTo(Replace.class) != null)
            .sorted(Sorting::compareByOrigin)
            .collect(Collectors.toCollection(LinkedList::new));

        while (!replacingEntries.isEmpty()) {

            Source replacingEntry = replacingEntries.remove();
            Replace replace = replacingEntry.adaptTo(Replace.class);

            ClassMemberSettings formerClassMemberAnnotation = new ClassMemberSettings(replace.value())
                .populateDefaultSource(replacingEntry.getDeclaringClass());

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
            if (!PluginReflectionUtility.annotationPropertyIsNotDefault(replacingEntry.adaptTo(DialogField.class), DialogConstants.PN_RANKING)) {
                int formerRank = formerEntry.getSetting(DialogField.class, DialogConstants.PN_RANKING, 0);
                replacingEntry.storeSetting(DialogField.class, DialogConstants.PN_RANKING, formerRank);
            }

            // Purge the former entry
            result.remove(formerEntry);
            replacingEntries.remove(formerEntry); // because a replaceable member may also be declared as "replacing"
        }
        return result;
    }

    public static Collector<Source, Replacing, List<Source>> processSourceReplace() {
        return SOURCE_REPLACING;
    }

    private static class SourceReplacingCollector implements Collector<Source, Replacing, List<Source>> {

        @Override
        public Supplier<Replacing> supplier() {
            return Replacing::new;
        }

        @Override
        public BiConsumer<Replacing, Source> accumulator() {
            return (left, right) -> left.internal.add(right);
        }

        @Override
        public BinaryOperator<Replacing> combiner() {
            return (left, right) -> {
                left.internal.addAll(right.internal);
                return left;
            };
        }

        @Override
        public Function<Replacing, List<Source>> finisher() {
            return Replacing::processInternal;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Sets.immutableEnumSet(Characteristics.UNORDERED);
        }
    }
}
