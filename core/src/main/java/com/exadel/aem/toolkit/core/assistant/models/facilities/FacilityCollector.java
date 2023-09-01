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
package com.exadel.aem.toolkit.core.assistant.models.facilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Implements the functional interface to accumulate facilities that represent services coming from different vendors
 * into functional groups
 * <p><u>Note:</u> this class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class FacilityCollector implements Collector<Facility, List<Facility>, List<Facility>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Supplier<List<Facility>> supplier() {
        return ArrayList::new;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BiConsumer<List<Facility>, Facility> accumulator() {
        return this::place;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BinaryOperator<List<Facility>> combiner() {
        return (list1, list2) -> {
            list2.forEach(item -> place(list1, item));
            return list1;
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Function<List<Facility>, List<Facility>> finisher() {
        return list -> {
            list.sort(FacilityCollector::compareFacilities);
            return list;
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Characteristics> characteristics() {
        return new HashSet<>(Collections.singletonList(Characteristics.UNORDERED));
    }

    /**
     * Called internally to either add the given {@link Facility} to the upper-level list of facilities or merge it into
     * another facility as a vendor-specific variant
     * @param list      The {@code List} of {@code Facility} objects in which to accommodate the {@code candidate}
     *                  Cannot be null.
     * @param candidate The {@code Facility} object that wqe need to place into the List. Cannot be null.
     */
    private void place(List<Facility> list, Facility candidate) {
        Facility existing = list.stream().filter(f -> isAdjacentById(f, candidate)).findFirst().orElse(null);
        if (existing != null) {
            if (existing instanceof SimpleFacility) {
                CombinedFacility combined = new CombinedFacility(existing);
                list.add(list.indexOf(existing), combined);
                list.remove(existing);
                existing = combined;
            }
            existing.getVariants().add(candidate);
        } else {
            list.add(candidate);
        }
    }

    /**
     * This method checks if two provided facilities can be merged into one complex facility because are adjacent by
     * their IDs
     * @param first  The first facility to check. Cannot be null.
     * @param second The second facility to check. Cannot be null.
     * @return True or false
     * @throws NullPointerException if either of the arguments is null.
     */
    private static boolean isAdjacentById(Facility first, Facility second) {
        boolean isSimilarStructure = StringUtils.countMatches(first.getId(), CoreConstants.SEPARATOR_DOT)
            == StringUtils.countMatches(second.getId(), CoreConstants.SEPARATOR_DOT);
        if (isSimilarStructure) {
            return StringUtils.equals(
                StringUtils.substringBeforeLast(first.getId(), CoreConstants.SEPARATOR_DOT),
                StringUtils.substringBeforeLast(second.getId(), CoreConstants.SEPARATOR_DOT));
        }
        return StringUtils.startsWith(first.getId(), second.getId() + CoreConstants.SEPARATOR_DOT)
            || StringUtils.startsWith(second.getId(), first.getId() + CoreConstants.SEPARATOR_DOT);
    }

    private static int compareFacilities(Facility first, Facility second) {
        int sortByRanking = first.getRanking() - second.getRanking();
        if (sortByRanking != 0) {
            return sortByRanking;
        }
        return StringUtils.compare(first.getTitle(), second.getTitle());
    }
}
