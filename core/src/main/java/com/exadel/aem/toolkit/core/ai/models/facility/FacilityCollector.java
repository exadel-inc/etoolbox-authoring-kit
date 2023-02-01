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
package com.exadel.aem.toolkit.core.ai.models.facility;

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

import org.apache.commons.lang.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;

public class FacilityCollector implements Collector<Facility, List<Facility>, List<Facility>> {

    @Override
    public Supplier<List<Facility>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<Facility>, Facility> accumulator() {
        return this::place;
    }

    @Override
    public BinaryOperator<List<Facility>> combiner() {
        return (list1, list2) -> {
            list2.forEach(item -> place(list1, item));
            return list1;
        };
    }

    @Override
    public Function<List<Facility>, List<Facility>> finisher() {
        return Collections::unmodifiableList;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return new HashSet<>(Collections.singletonList(Characteristics.UNORDERED));
    }

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

    private static boolean isAdjacentById(Facility first, Facility second) {
        return StringUtils.equals(
            StringUtils.substringBefore(first.getId(), CoreConstants.SEPARATOR_DOT),
            StringUtils.substringBefore(second.getId(), CoreConstants.SEPARATOR_DOT));
    }
}
