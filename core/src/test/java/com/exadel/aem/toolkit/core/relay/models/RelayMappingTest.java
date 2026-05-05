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

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

public class RelayMappingTest {

    private static final String PATH_SOURCE = "/content/source";
    private static final String PATH_TARGET = "/content/target";
    private static final String PATH_OTHER = "/content/other";

    @Test
    public void shouldReturnPropertiesAndValidity() {
        RelayMapping mapping = newMapping(PATH_SOURCE, PATH_TARGET);

        assertEquals(PATH_SOURCE, mapping.getFrom());
        assertEquals(PATH_TARGET, mapping.getTo());
        assertTrue(mapping.isValid());

        assertFalse(newMapping(StringUtils.EMPTY, PATH_TARGET).isValid());
        assertFalse(newMapping(PATH_SOURCE, StringUtils.EMPTY).isValid());
        assertFalse(newMapping(null, PATH_TARGET).isValid());
        assertFalse(newMapping(PATH_SOURCE, "non-tree").isValid());
        assertFalse(newMapping(PATH_SOURCE, PATH_SOURCE).isValid());
        assertFalse(newMapping(PATH_SOURCE, PATH_SOURCE + "/child").isValid());
        assertFalse(newMapping(PATH_SOURCE + "/child", PATH_SOURCE).isValid());
    }

    @Test
    public void shouldCheckEquality() {
        RelayMapping a = newMapping(PATH_SOURCE, PATH_TARGET);
        RelayMapping b = newMapping(PATH_SOURCE, PATH_TARGET);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, newMapping(PATH_SOURCE, PATH_OTHER));
        assertNotEquals(a, newMapping(PATH_OTHER, PATH_TARGET));
    }

    private static RelayMapping newMapping(String from, String to) {
        String fromJson = from != null ? "\"" + from + "\"" : "null";
        String toJson = to != null ? "\"" + to + "\"" : "null";
        return ObjectConversionUtil.toObject(
            "{\"from\":" + fromJson + ",\"to\":" + toJson + "}",
            RelayMapping.class);
    }
}
