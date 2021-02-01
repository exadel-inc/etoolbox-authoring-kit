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

package com.exadel.aem.toolkit.plugin.util.ordering;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestOrdering {

    int size = 100;
    List<Orderable> list = new ArrayList<>(size);

    // Init list with random nodes
    @Before
    public void init() {
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            list.add(Orderable.from("Handler" + i));
        }
        for (int i = 0; i < size; i++) {
            int mode = random.nextInt(4);
            int iAfter = random.nextInt(size);
            while (iAfter == i) {
                iAfter = random.nextInt(size);
            }
            int iBefore = random.nextInt(size);
            while (iBefore == i) {
                iBefore = random.nextInt(size);
            }
            if (mode == 0) {
                list.get(i).setAfter(list.get(iAfter));
            } else if (mode == 1) {
                list.get(i).setBefore(list.get(iBefore));
            } else if (mode == 2) {
                list.get(i).setBefore(list.get(iBefore));
                list.get(i).setBefore(list.get(iAfter));
            }
        }
    }

    // Test, that size will be equal to initial size and ordered list consists of unique nodes
    @Test
    public void testRandomOrderableList() {
        Graph graph = new Graph(list);
        List<Orderable> answer = graph.topologicalSort();
        Assert.assertEquals(size, answer.size());
        assertUnique(answer);
    }

    // Test graph with cycle inside
    @Test
    public void testGraphWithCycleInside1() {
        List<Orderable> list = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            list.add(Orderable.from("Handler" + i));
        }
        list.get(0).setBefore(list.get(1));
        list.get(1).setBefore(list.get(2));
        list.get(4).setAfter(list.get(1));
        list.get(2).setBefore(list.get(3));
        list.get(3).setBefore(list.get(1));
        Graph graph = new Graph(list);
        List<Orderable> answer = graph.topologicalSort();

        List<String> expected = new ArrayList<>(5);
        expected.add("Handler0");
        expected.add("Handler1");
        expected.add("Handler2");
        expected.add("Handler4");
        expected.add("Handler3");
        Assert.assertEquals(5, answer.size());
        Assert.assertEquals(answer.stream().map(Orderable::getName).collect(Collectors.toList()), expected);
    }

    // Test graph with cycle inside
    @Test
    public void testGraphWithCycleInside2() {
        List<Orderable> list = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            list.add(Orderable.from("Handler" + i));
        }
        list.get(0).setBefore(list.get(1));
        list.get(1).setBefore(list.get(2));
        list.get(2).setBefore(list.get(3));
        list.get(3).setBefore(list.get(4));
        list.get(1).setAfter(list.get(3));
        list.get(5).setBefore(list.get(1));
        list.get(6).setBefore(list.get(1));
        Graph graph = new Graph(list);
        List<Orderable> answer = graph.topologicalSort();

        Assert.assertEquals(7, answer.size());
        assertUnique(answer);
        List<String> expected = new ArrayList<>(7);
        expected.add("Handler0");
        expected.add("Handler5");
        expected.add("Handler6");
        expected.add("Handler1");
        expected.add("Handler2");
        expected.add("Handler3");
        expected.add("Handler4");
        Assert.assertEquals(answer.stream().map(Orderable::getName).collect(Collectors.toList()), expected);
        assertUnique(answer);
    }

    // Test cycle graph
    @Test
    public void testSimpleCycleGraph() {
        List<Orderable> list = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            list.add(Orderable.from("Handler" + i));
        }
        list.get(0).setBefore(list.get(1));
        list.get(1).setBefore(list.get(2));
        list.get(2).setBefore(list.get(3));
        list.get(3).setBefore(list.get(0));
        Graph graph = new Graph(list);
        List<Orderable> answer = graph.topologicalSort();

        Assert.assertEquals(4, answer.size());
        assertUnique(answer);
        Assert.assertEquals(answer, list);
    }

    @Test
    public void testExampleGraph1() {
        List<Orderable> list = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            list.add(Orderable.from("Handler" + i));
        }
        list.get(1).setBefore(list.get(0));
        list.get(2).setBefore(list.get(6));
        list.get(3).setBefore(list.get(2));
        list.get(4).setAfter(list.get(2));
        list.get(6).setBefore(list.get(0));
        list.get(7).setAfter(list.get(6));
        list.get(9).setBefore(list.get(5));
        Graph graph = new Graph(list);
        List<Orderable> answer = graph.topologicalSort();

        Assert.assertEquals(10, answer.size());
        List<String> expected = new ArrayList<>(10);
        expected.add("Handler1");
        expected.add("Handler3");
        expected.add("Handler8");
        expected.add("Handler9");
        expected.add("Handler2");
        expected.add("Handler5");
        expected.add("Handler4");
        expected.add("Handler6");
        expected.add("Handler0");
        expected.add("Handler7");
        Assert.assertEquals(expected, answer.stream().map(Orderable::getName).collect(Collectors.toList()));
        assertUnique(answer);
        assertCommon(answer, list);
    }

    // Test graph without edges
    @Test
    public void testExampleGraph2() {
        List<Orderable> list = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            list.add(Orderable.from("Handler" + i));
        }

        Graph graph = new Graph(list);
        List<Orderable> answer = graph.topologicalSort();
        Assert.assertEquals(answer, list);
    }

    // Assert that before, after rules are achieved
    private void assertCommon(List<Orderable> answer, List<Orderable> initial) {
        for (Orderable node : initial) {
            int currentPosition = answer.indexOf(node);
            int beforePosition = node.getBefore() != null
                ? answer.indexOf(node.getBefore())
                : initial.size();
            int afterPosition = node.getAfter() != null
                ? answer.indexOf(node.getAfter())
                : 0;
            Assert.assertTrue(beforePosition >= currentPosition);
            Assert.assertTrue(currentPosition >= afterPosition);
        }
    }

    // Assert that ordered list consists of only unique values
    private void assertUnique(List<Orderable> answer) {
        Set<Orderable> set = new HashSet<>(answer.size());
        set.addAll(answer);
        Assert.assertEquals(answer.size(), set.size());
    }
}
