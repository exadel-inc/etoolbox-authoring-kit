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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class TopologicalSorterTest {

    private static final List<String> CYCLED_GRAPH_SEQUENCE_2 = Arrays.asList(
        "Handler0",
        "Handler6",
        "Handler5",
        "Handler2",
        "Handler3",
        "Handler4",
        "Handler1");

    private static final List<String> CYCLED_GRAPH_SEQUENCE_1 = Arrays.asList(
        "Handler0",
        "Handler2",
        "Handler3",
        "Handler1",
        "Handler4");

    private static final List<String> SIMPLE_CYCLED_GRAPH_SEQUENCE = Arrays.asList(
        "Handler0",
        "Handler1",
        "Handler2",
        "Handler3");

    private static final List<String> REGULAR_GRAPH_SEQUENCE_1 = Arrays.asList(
        "Handler0",
        "Handler1",
        "Handler2",
        "Handler3",
        "Handler4",
        "Handler5",
        "Handler6");

    private static final List<String> REGULAR_GRAPH_SEQUENCE_2 = Arrays.asList(
        "Handler1",
        "Handler3",
        "Handler2",
        "Handler4",
        "Handler6",
        "Handler7",
        "Handler0",
        "Handler9",
        "Handler5",
        "Handler8");

    private static final List<String> REGULAR_GRAPH_SEQUENCE_3 = Arrays.asList(
        "Handler2",
        "Handler3",
        "Handler0",
        "Handler1");

    private static final List<String> REGULAR_GRAPH_SEQUENCE_4 = Arrays.asList(
        "Handler0",
        "Handler2",
        "Handler3",
        "Handler1");

    // Test, that size will be equal to initial size and ordered list consists of unique nodes
    @Test
    public void testRandomOrderableList() {
        List<Orderable<String>> list = getRandomList(100);

        List<String> answer = getSortedByValues(list);

        Assert.assertEquals(list.size(), answer.size());
        assertOnlyUniqueValues(answer);
    }

    @Test
    public void testGraphWithCycleInside1() {
        List<Orderable<String>> list = getList(5);

        list.get(0).getBefore().add(list.get(1));
        list.get(1).getAfter().add(list.get(0));

        list.get(1).getBefore().add(list.get(2));
        list.get(2).getAfter().add(list.get(1));

        list.get(2).getBefore().add(list.get(3));
        list.get(3).getAfter().add(list.get(2));

        list.get(3).getBefore().add(list.get(1));
        list.get(1).getAfter().add(list.get(3));

        list.get(4).getAfter().add(list.get(1));
        list.get(1).getBefore().add(0, list.get(4));

        List<String> answer = getSortedByValues(list);

        assertOnlyUniqueValues(answer);
        Assert.assertEquals(CYCLED_GRAPH_SEQUENCE_1, answer);
    }

    @Test
    public void testGraphWithCycleInside2() {
        List<Orderable<String>> list = getList(7);

        list.get(0).getBefore().add(list.get(1));
        list.get(1).getAfter().add(list.get(0));

        list.get(1).getBefore().add(list.get(2));
        list.get(2).getAfter().add(list.get(1));

        list.get(1).getAfter().add(list.get(3));
        list.get(3).getBefore().add(0, list.get(1));

        list.get(2).getBefore().add(list.get(3));
        list.get(3).getAfter().add(list.get(2));

        list.get(3).getBefore().add(list.get(4));
        list.get(4).getAfter().add(list.get(3));

        list.get(5).getBefore().add(list.get(1));
        list.get(1).getAfter().add(list.get(5));

        list.get(6).getBefore().add(list.get(1));
        list.get(1).getAfter().add(list.get(6));

        List<String> answer = getSortedByValues(list);

        Assert.assertEquals(CYCLED_GRAPH_SEQUENCE_2, answer);
    }

    @Test
    public void testSimpleCycleGraph() {
        List<Orderable<String>> list = getList(4);

        list.get(0).setBefore(Collections.singletonList(list.get(1)));
        list.get(1).setBefore(Collections.singletonList(list.get(2)));
        list.get(2).setBefore(Collections.singletonList(list.get(3)));
        list.get(3).setBefore(Collections.singletonList(list.get(0)));

        List<String> answer = getSortedByValues(list);

        Assert.assertEquals(SIMPLE_CYCLED_GRAPH_SEQUENCE, answer);
    }

    @Test
    public void testExampleGraph1() {
        List<Orderable<String>> list = getList(10);

        list.get(1).getBefore().add(list.get(0));
        list.get(0).getAfter().add(list.get(1));

        list.get(2).getBefore().add(list.get(6));
        list.get(6).getAfter().add(list.get(2));

        list.get(3).getBefore().add(list.get(2));
        list.get(2).getAfter().add(list.get(3));

        list.get(4).getAfter().add(list.get(2));
        list.get(2).getBefore().add(0, list.get(4));

        list.get(6).getBefore().add(list.get(0));
        list.get(0).getAfter().add(list.get(6));

        list.get(7).getAfter().add(list.get(6));
        list.get(6).getBefore().add(0, list.get(7));

        list.get(9).getBefore().add(list.get(5));
        list.get(5).getAfter().add(list.get(9));

        List<String> answerValues = getSortedByValues(list);

        Assert.assertEquals(REGULAR_GRAPH_SEQUENCE_2, answerValues);
    }

    // Test graph without edges
    @Test
    public void testExampleGraph2() {
        List<Orderable<String>> list = getList(7);

        List<String> answerValues = getSortedByValues(list);

        Assert.assertEquals(REGULAR_GRAPH_SEQUENCE_1, answerValues);
    }

    @Test
    public void testExampleGraph3() {
        List<Orderable<String>> list = getList(4);

        list.get(2).getBefore().add(list.get(0));
        list.get(0).getAfter().add( list.get(2));

        list.get(3).getBefore().add(list.get(0));
        list.get(0).getAfter().add(list.get(3));

        List<String> answerValues = getSortedByValues(list);

        Assert.assertEquals(REGULAR_GRAPH_SEQUENCE_3, answerValues);
    }

    @Test
    public void testExampleGraph4() {
        List<Orderable<String>> list = getList(4);

        list.get(2).getAfter().add(list.get(0));
        list.get(0).getBefore().add(0, list.get(2));

        list.get(3).getAfter().add(list.get(0));
        list.get(0).getBefore().add(0, list.get(3));

        List<String> answerValues = getSortedByValues(list);

        Assert.assertEquals(REGULAR_GRAPH_SEQUENCE_4, answerValues);
    }

    // Inits list without edges
    private List<Orderable<String>> getList(int size) {
        return IntStream.range(0, size)
            .mapToObj(i -> new Orderable<>("Handler" + i, "Handler" + i))
            .collect(Collectors.toList());
    }

    // Inits list with random edges
    @SuppressWarnings("SameParameterValue")
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
                list.get(i).setAfter(Collections.singletonList(list.get(iAfter)));
            } else if (mode == 1) {
                list.get(i).setBefore(Collections.singletonList(list.get(iBefore)));
            } else {
                list.get(i).setBefore(Collections.singletonList(list.get(iBefore)));
                list.get(i).setBefore(Collections.singletonList(list.get(iAfter)));
            }
        }
        return list;
    }

    private List<Orderable<String>> getTopologicalSorted(List<Orderable<String>> list) {
        return new TopologicalSorter<>(list).topologicalSort();
    }

    private List<String> getSortedByValues(List<Orderable<String>> list) {
        return getTopologicalSorted(list).stream().map(Orderable::getValue).collect(Collectors.toList());
    }

    private void assertOnlyUniqueValues(List<String> answer) {
        Assert.assertEquals(answer.size(), new HashSet<>(answer).size());
    }
}
