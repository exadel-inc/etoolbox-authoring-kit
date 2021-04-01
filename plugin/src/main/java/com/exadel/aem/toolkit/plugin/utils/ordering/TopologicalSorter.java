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
import java.util.Comparator;
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
    private final List<List<Orderable<T>>> adjacencyList;

    /**
     * Initializes a class instance
     * @param nodes Collection of entities to be sorted
     */
    public TopologicalSorter(List<Orderable<T>> nodes) {
        this.nodes = nodes;
        this.adjacencyList = new ArrayList<>(this.nodes.size());
        for (int i = 0; i < this.nodes.size(); i++) {
            this.nodes.get(i).setPosition(i);
            this.adjacencyList.add(new ArrayList<>());
        }
        initAdjacencyList();
    }

    /**
     * Performs the main sorting routine
     * @return List of entities with the sorting applied
     */
    public List<Orderable<T>> topologicalSort() {
        // Array to store how many edges are incoming to the i node
        int[] inDegrees = new int[this.nodes.size()];
        // Deque for bfs that store nodes in special order for bfs
        Deque<Orderable<T>> deque = new LinkedList<>();
        // List to store sorted order
        List<Orderable<T>> sortedOrder = new ArrayList<>(this.nodes.size());

        // Loop to count how many edges are incoming to the i node
        for (int i = 0; i < this.nodes.size(); i++) {
            for (Orderable<T> node : this.adjacencyList.get(i)) {
                inDegrees[node.getPosition()]++;
            }
        }

        // Loop to init dequeue with nodes, that do not have incoming edges
        for (int i = 0; i < this.nodes.size(); i++) {
            if (inDegrees[i] == 0) {
                deque.addLast(nodes.get(i));
            }
        }

        // Check if entire graph is loop
        if (deque.isEmpty()) {
            // Set that the first node do not have incoming nodes
            inDegrees[0] = 0;
            // Add the first node to deque to start bfs from the first node
            deque.addLast(nodes.get(0));
            // Remove all edges incoming to the first node
            this.adjacencyList.forEach(list -> list.remove(nodes.get(0)));
        }

        // Start of bfs
        while (!deque.isEmpty()) {
            Orderable<T> currentNode = deque.pollFirst();
            sortedOrder.add(currentNode);

            int indexOfCurrentNode = currentNode.getPosition();

            // Iterate a trough all neighbors for the current node (that means bfs)
            for (Orderable<T> adjacent : this.adjacencyList.get(indexOfCurrentNode)) {
                int indexOfNeighborNode = adjacent.getPosition();
                inDegrees[indexOfNeighborNode]--;
                if (inDegrees[indexOfNeighborNode] == 0) {
                    deque.addLast(adjacent);
                }
            }
        }

        // Check if not all nodes are visited
        if (sortedOrder.size() != this.nodes.size()) {
            sortedOrder.addAll(sortLoop(inDegrees));
        }
        return sortedOrder;
    }

    /**
     * Initializes the collection of ordered lists used to represent a finite graph
     */
    private void initAdjacencyList() {
        for (int i = 0; i < this.nodes.size(); i++) {
            Orderable<T> currNode = this.nodes.get(i);
            Orderable<T> before = currNode.getBefore();
            Orderable<T> after = currNode.getAfter();
            // Check for null and self-loop
            if (before != null && !before.equals(currNode)) {
                this.adjacencyList.get(i).add(before);
            }
            // Check for null, self-loop and simple cycle, e.g. 1->2 and 2->1
            if (after != null
                && !after.equals(currNode)
                && !this.adjacencyList.get(after.getPosition()).contains(currNode)) {
                this.adjacencyList.get(after.getPosition()).add(currNode);
            }
        }
        // Sort every list to keep alphabetical order
        for (int i = 0; i < this.nodes.size(); i++) {
            this.adjacencyList.get(i).sort((Comparator.comparing(Orderable::getName)));
        }
    }

    /**
     * Called by {@link TopologicalSorter#topologicalSort()} when there's no possibility to process all the nodes
     * in a single run (due to a loop-like relation when e.g. two nodes refer to each other in their "before" hints).
     * This method collects the nodes that are involved in a loop-like relation and composes a separate graph in order
     * to perform another sorting run for these nodes separately
     * @param inDegrees Array of integer values defining the number of incoming edges
     * @return List of entities with the sorting applied
     */
    private List<Orderable<T>> sortLoop(int[] inDegrees) {
        List<Orderable<T>> loopNodes = new ArrayList<>();
        for (int i = 0; i < this.nodes.size(); i++) {
            if (inDegrees[i] != 0) {
                loopNodes.add(this.nodes.get(i));
            }
        }
        return new TopologicalSorter<>(loopNodes).topologicalSort();
    }
}
