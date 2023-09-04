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
package com.exadel.aem.toolkit.plugin.utils.ordering;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements topological sorting for the {@link OrderingUtil} methods. The two collections are internally managed:
 * {@code List<Orderable<T>>} - a collection of entities that must be sorted; and {@code List<List<Orderable<T>>>} -
 * a collection of ordered lists used to represent a finite graph (each list describing neighbors of a node in the graph)
 * @param <T> Type of the sorted entities
 * @see OrderingUtil
 */
class TopologicalSorter<T> {

    private final List<Orderable<T>> nodes;

    /**
     * Initializes a class instance
     * @param nodes Collection of entities to be sorted
     */
    TopologicalSorter(List<Orderable<T>> nodes) {
        this.nodes = nodes;
    }

    /**
     * Performs the main sorting routine
     * @return List of entities with the sorting applied
     */
    public List<Orderable<T>> topologicalSort() {
        List<Orderable<T>> deque = new ArrayList<>();

        for (int i = 0; i < this.nodes.size(); i++) {
            Orderable<T> orderable = nodes.get(i);
            Deque<Orderable<T>> temp = new LinkedList<>();

            Deque<Orderable<T>> after = after(orderable, new ArrayList<>());
            while (!after.isEmpty()) {
                Orderable<T> tOrderable = after.removeLast();
                if (!deque.contains(tOrderable) && !temp.contains(tOrderable)) {
                    temp.addFirst(tOrderable);
                }
            }

            Deque<Orderable<T>> before = before(orderable, new ArrayList<>());
            while (!before.isEmpty()) {
                Orderable<T> tOrderable = before.removeFirst();
                if (!deque.contains(tOrderable) && !temp.contains(tOrderable)) {
                    temp.addLast(tOrderable);
                }
            }

            if (!deque.contains(orderable)) {
                temp.addLast(orderable);
            }

            deque.addAll(temp);
        }
        return deque;
    }

    /**
     * Called by {@link TopologicalSorter#topologicalSort()} to get all entities connected to
     * the entity as 'after' relationship
     * @param orderable Orderable entity
     * @param values List of all already used entities in sort
     * @return Deque of connected entities
     */
    private Deque<Orderable<T>> after(Orderable<T> orderable, List<Orderable<T>> values) {
        Deque<Orderable<T>> deque = new LinkedList<>();
        if (orderable.getAfter() != null && !values.contains(orderable)) {
            values.add(orderable);
            Deque<Orderable<T>> after = after(orderable.getAfter(), values);
            while (!after.isEmpty()) {
                deque.addFirst(after.removeLast());
            }
        }
        deque.addLast(orderable);
        return deque;
    }

    /**
     * Called by {@link TopologicalSorter#topologicalSort()} to get all entities connected to
     * the entity as 'before' relationship
     * @param orderable Orderable entity
     * @param values List of all already used entities in sort
     * @return Deque of connected entities
     */
    private Deque<Orderable<T>> before(Orderable<T> orderable, List<Orderable<T>> values) {
        Deque<Orderable<T>> deque = new LinkedList<>();
        if (orderable.getBefore() != null && !values.contains(orderable)) {
            values.add(orderable);
            Deque<Orderable<T>> before = before(orderable.getBefore(), values);
            while (!before.isEmpty()) {
                deque.addLast(before.removeFirst());
            }
        }
        deque.addFirst(orderable);
        return deque;
    }
}
