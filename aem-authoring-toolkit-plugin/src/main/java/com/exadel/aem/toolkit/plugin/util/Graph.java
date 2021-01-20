package com.exadel.aem.toolkit.plugin.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.exadel.aem.toolkit.api.markers._Default;

/**
 * Implements topological sorting
 * List<Class<?>> nodes stores all existing Custom Handlers
 * HashMap<Class<?>, ArrayList<Class<?>>> edges stores Handler as a key and all neighbors as list of values
 */
class Graph {
    private HashMap<Class<?>, ArrayList<Class<?>>> edges;
    private List<Class<?>> nodes;
    private List<Class<?>> nodeVisited;
    private ArrayList<Class<?>> edgeList;

    Graph(List vertices) {
        nodes = vertices;
        edges = new HashMap<>();
        nodeVisited = new ArrayList<>();
    }

    /**
     * Method to add edge(Handler y) to a node(Handler x) edgeList
     * and put result in edges where we store all existing nodes with their subsequent values.
     *
     * @param x {@code Class<?>} Custom Handler x that comes before Custom Handler y
     * @param y {@code Class<?>} Custom Handler y that comes after Custom Handler x
     */
    void addEdge(Class<?> x, Class<?> y) {
        if (!x.equals(_Default.class)) {
            if (!edges.containsKey(x)) {
                edgeList = new ArrayList<>();
            } else {
                edgeList = edges.get(x);
            }
            // Check to prevent loops
            if (edges.containsKey(y)) {
                ArrayList<Class<?>> edgeListY = edges.get(y);
                if (!edgeListY.contains(x)) {
                    edgeList.add(y);
                    edges.put(x, edgeList);
                }
            } else {
                edgeList.add(y);
                edges.put(x, edgeList);
            }
        }
    }

    /**
     * Method containing the logic to sort the given Handlers(nodes) recursively
     *
     * @return {@code List<Class<? extends T>>} of Handlers in necessary way
     */
    <T> List<Class<? extends T>> topologicalSort() {
        Stack<Class<? extends T>> stack = new Stack<>();
        List<Class<? extends T>> list = new ArrayList<>();
        // iterate through all the nodes and their neighbours if not already visited.
        for (Object c : nodes) {
            if (!nodeVisited.contains(c.getClass())) {
                sort(c.getClass(), stack);
            }
        }
        while (!stack.empty()) {
            list.add(stack.pop());
        }
        return list;
    }

    /**
     * Method iterates through all the nodes and neighbours.
     * Pushes the visited items to stack
     */
    void sort(Class<?> node, Stack stack) {
        // add the visited node to list, so we don't repeat this node again
        nodeVisited.add(node);
        if (edges.get(node) != null) {
            // get all the neighbor nodes, by referring its edges
            Iterator iter = edges.get(node).iterator();
            Class<?> neighborNode;
            // if an edge exists for the node, then visit that neighbor node
            while (iter.hasNext()) {
                neighborNode = (Class<?>) iter.next();
                if (!nodeVisited.contains(neighborNode)) {
                    sort(neighborNode, stack);
                }
            }
        }
        // push the latest node on to the stack
        stack.push(node);
    }
}
