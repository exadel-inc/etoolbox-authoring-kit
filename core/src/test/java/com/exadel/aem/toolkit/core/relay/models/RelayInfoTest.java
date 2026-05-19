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

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

public class RelayInfoTest {

    private static final String PATH_SOURCE = "/content/source";
    private static final String PATH_TARGET = "/content/target";
    private static final String PATH_SAMPLE = "/content/sample";
    private static final String USER_AUTHOR = "author";
    private static final String USER_ADMIN = "admin";

    @Test
    public void shouldReturnSourceAndTarget() {
        RelayInfo relay = newRelayInfo(PATH_SOURCE, PATH_TARGET);

        assertEquals(PATH_SOURCE, relay.getSource());
        assertEquals(PATH_TARGET, relay.getTarget());

        // Null from/to in path mapping defaults to empty string
        RelayInfo withNulls = new RelayInfo(
            newMapping(null, null),
            Collections.emptyList(),
            Collections.emptyList());
        assertEquals(StringUtils.EMPTY, withNulls.getSource());
        assertEquals(StringUtils.EMPTY, withNulls.getTarget());
    }

    @Test
    public void shouldLookUpUserMapping() {
        RelayInfo relay = new RelayInfo(
            newMapping(PATH_SOURCE, PATH_TARGET),
            Collections.singletonList(newMapping(USER_AUTHOR, USER_ADMIN)),
            Collections.emptyList());

        assertEquals(USER_ADMIN, relay.getUserMapping(USER_AUTHOR));
        assertNull(relay.getUserMapping("unknown"));

        // Empty or null user mappings → always null
        RelayInfo noMappings = newRelayInfo(PATH_SOURCE, PATH_TARGET);
        assertNull(noMappings.getUserMapping(USER_AUTHOR));

        RelayInfo nullMappings = new RelayInfo(
            newMapping(PATH_SOURCE, PATH_TARGET),
            null,
            Collections.emptyList());
        assertNull(nullMappings.getUserMapping(USER_AUTHOR));
    }

    @Test
    public void shouldReturnChangeSamples() {
        ChangeSample sample = newChangeSample(PATH_SAMPLE);
        RelayInfo relay = new RelayInfo(
            newMapping(PATH_SOURCE, PATH_TARGET),
            Collections.emptyList(),
            Collections.singletonList(sample));

        Collection<ChangeSample> samples = relay.getChangeSamples();
        assertEquals(1, samples.size());
        assertEquals(PATH_SAMPLE, samples.iterator().next().getPath());

        // The returned collection is unmodifiable
        boolean threw = false;
        try {
            samples.add(newChangeSample(PATH_SAMPLE));
        } catch (UnsupportedOperationException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    @SuppressWarnings("SameParameterValue")
    private static RelayInfo newRelayInfo(String source, String target) {
        return new RelayInfo(
            newMapping(source, target),
            Collections.emptyList(),
            Collections.emptyList());
    }

    private static RelayMapping newMapping(String from, String to) {
        String fromJson = from != null ? "\"" + from + "\"" : "null";
        String toJson = to != null ? "\"" + to + "\"" : "null";
        return ObjectConversionUtil.toObject(
            "{\"from\":" + fromJson + ",\"to\":" + toJson + "}",
            RelayMapping.class);
    }

    @SuppressWarnings("SameParameterValue")
    private static ChangeSample newChangeSample(String path) {
        return ObjectConversionUtil.toObject(
            "{\"path\":\"" + path + "\"}",
            ChangeSample.class);
    }
}
