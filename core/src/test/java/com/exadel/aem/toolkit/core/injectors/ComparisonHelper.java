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
package com.exadel.aem.toolkit.core.injectors;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class ComparisonHelper {

    private ComparisonHelper() {
    }

    public static <T, U> void assertCollectionsEqual(Collection<T> expected, U[] actual, BiConsumer<T, U> assertion) {
        assertCollectionsEqual(expected, Arrays.asList(actual), assertion);
    }

    public static <T, U> void assertCollectionsEqual(Collection<T> expected, Collection<U> actual, BiConsumer<T, U> assertion) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        Iterator<T> expectedIterator = expected.iterator();
        Iterator<U> actualIterator = actual.iterator();
        while (expectedIterator.hasNext()) {
            T expectedItem = expectedIterator.next();
            U actualItem = actualIterator.next();
            assertion.accept(expectedItem, actualItem);
        }
    }
}
