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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class TestOrdering {

    private static final List<String> expectedTestGraphWithCycleInside1 = Arrays
        .asList("Handler0", "Handler1", "Handler2", "Handler4", "Handler3");

    private static final List<String> expectedTestGraphWithCycleInside2 = Arrays
        .asList("Handler0", "Handler5", "Handler6", "Handler1", "Handler2", "Handler3", "Handler4");

    private static final List<String> expectedTestSimpleCycleGraph = Arrays
        .asList("Handler0", "Handler1", "Handler2", "Handler3");

    private static final List<String> expectedTestExampleGraph1 = Arrays
        .asList("Handler1", "Handler3", "Handler8", "Handler9", "Handler2", "Handler5", "Handler4", "Handler6", "Handler0", "Handler7");

    private static final List<String> expectedTestExampleGraph2 = Arrays
        .asList("Handler0", "Handler1", "Handler2", "Handler3", "Handler4", "Handler5", "Handler6");

    // Test, that size will be equal to initial size and ordered list consists of unique nodes
    @Test
    public void testRandomOrderableList() {
        List<Orderable<String>> list = getRandomList(100);

        List<String> answer = sortListToValues(list);

        Assert.assertEquals(list.size(), answer.size());
        assertUnique(answer);
    }

    // Test graph with cycle inside
    @Test
    public void testGraphWithCycleInside1() {
        List<Orderable<String>> list = getList(5);

        list.get(0).setBefore(list.get(1));
        list.get(1).setBefore(list.get(2));
        list.get(4).setAfter(list.get(1));
        list.get(2).setBefore(list.get(3));
        list.get(3).setBefore(list.get(1));

        List<String> answer = sortListToValues(list);

        assertUnique(answer);
        Assert.assertEquals(expectedTestGraphWithCycleInside1, answer);
    }

    // Test graph with cycle inside
    @Test
    public void testGraphWithCycleInside2() {
        List<Orderable<String>> list = getList(7);

        list.get(0).setBefore(list.get(1));
        list.get(1).setBefore(list.get(2));
        list.get(2).setBefore(list.get(3));
        list.get(3).setBefore(list.get(4));
        list.get(1).setAfter(list.get(3));
        list.get(5).setBefore(list.get(1));
        list.get(6).setBefore(list.get(1));

        List<String> answer = sortListToValues(list);

        Assert.assertEquals(expectedTestGraphWithCycleInside2, answer);
    }

    // Test cycle graph
    @Test
    public void testSimpleCycleGraph() {
        List<Orderable<String>> list = getList(4);

        list.get(0).setBefore(list.get(1));
        list.get(1).setBefore(list.get(2));
        list.get(2).setBefore(list.get(3));
        list.get(3).setBefore(list.get(0));

        List<String> answer = sortListToValues(list);

        Assert.assertEquals(expectedTestSimpleCycleGraph, answer);
    }

    @Test
    public void testExampleGraph1() {
        List<Orderable<String>> list = getList(10);

        list.get(1).setBefore(list.get(0));
        list.get(2).setBefore(list.get(6));
        list.get(3).setBefore(list.get(2));
        list.get(4).setAfter(list.get(2));
        list.get(6).setBefore(list.get(0));
        list.get(7).setAfter(list.get(6));
        list.get(9).setBefore(list.get(5));

        List<Orderable<String>> answer = sortList(list);
        List<String> answerValues = sortListToValues(list);

        assertCommon(answer, list);
        Assert.assertEquals(expectedTestExampleGraph1, answerValues);
    }

    // Test graph without edges
    @Test
    public void testExampleGraph2() {
        List<Orderable<String>> list = getList(7);

        List<Orderable<String>> answer = sortList(list);
        List<String> answerValues = sortListToValues(list);

        assertCommon(answer, list);
        Assert.assertEquals(expectedTestExampleGraph2, answerValues);
    }

    // Inits list without edges
    private List<Orderable<String>> getList(int size) {
        return IntStream.range(0, size)
            .mapToObj(i -> new Orderable<>("Handler" + i, "Handler" + i))
            .collect(Collectors.toList());
    }

    // Inits list with random edges
    private List<Orderable<String>> getRandomList(int size) {
        List<Orderable<String>> list = new ArrayList<>(size);
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            list.add(new Orderable<>("Handler" + i, "Handler" + i));
        }
        for (int i = 0; i < size; i++) {
            int mode = random.nextInt(3);
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
            } else {
                list.get(i).setBefore(list.get(iBefore));
                list.get(i).setBefore(list.get(iAfter));
            }
        }
        return list;
    }

    // Gets sorted list
    private List<Orderable<String>> sortList(List<Orderable<String>> list) {
        return new Graph<>(list).topologicalSort();
    }

    // Gets sorted list mapped to their values
    private List<String> sortListToValues(List<Orderable<String>> list) {
        return sortList(list).stream().map(Orderable::getValue).collect(Collectors.toList());
    }

    // Asserts that before, after rules are achieved
    private void assertCommon(List<Orderable<String>> answer, List<Orderable<String>> initial) {
        for (Orderable<String> node : initial) {
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

    // Asserts that ordered list consists of only unique values
    private void assertUnique(List<String> answer) {
        Assert.assertEquals(answer.size(), new HashSet<>(answer).size());
    }
}
