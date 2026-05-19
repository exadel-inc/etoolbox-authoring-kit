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
package com.exadel.aem.toolkit.core.relay.models;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

public class ChangeSampleTest {

    private static final String PATH_SAMPLE = "/content/sample";

    @Test
    public void shouldReturnProperties() {
        ChangeSample withUser = newChangeSample(PATH_SAMPLE, 10, "author");

        assertEquals(PATH_SAMPLE, withUser.getPath());
        assertEquals(10, withUser.getLimit());
        assertEquals("author", withUser.getUser());

        // User can be null
        ChangeSample withoutUser = newChangeSample(PATH_SAMPLE, 0, null);
        assertNull(withoutUser.getUser());
    }

    @Test
    public void shouldBeEqualByPathOnly() {
        // Same path, different limit and user → equal
        ChangeSample a = newChangeSample(PATH_SAMPLE, 5, "user1");
        ChangeSample b = newChangeSample(PATH_SAMPLE, 99, "user2");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        // Different path → not equal
        assertNotEquals(a, newChangeSample("/content/other", 5, "user1"));

        // Null check
        assertNotNull(a);
    }

    private static ChangeSample newChangeSample(String path, int limit, String user) {
        String userJson = user != null ? "\"" + user + "\"" : "null";
        return ObjectConversionUtil.toObject(
            "{\"path\":\"" + path + "\",\"limit\":" + limit + ",\"user\":" + userJson + "}",
            ChangeSample.class);
    }
}
