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
package com.exadel.aem.toolkit.core.relay.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RelayPathHelperTest {

    private static final String PATH_SOURCE = "/content/source";
    private static final String PATH_TARGET = "/content/target";

    @Test
    public void shouldCheckForSamePathOrSubpath() {
        assertTrue(RelayPathHelper.isSamePathOrSubpath(PATH_SOURCE, PATH_SOURCE));
        assertTrue(RelayPathHelper.isSamePathOrSubpath(PATH_SOURCE + "/child", PATH_SOURCE));
        assertTrue(RelayPathHelper.isSamePathOrSubpath(PATH_SOURCE + "/a/b/c", PATH_SOURCE));
        assertTrue(RelayPathHelper.isSamePathOrSubpath(PATH_SOURCE + "/", PATH_SOURCE));
        assertTrue(RelayPathHelper.isSamePathOrSubpath(PATH_SOURCE, PATH_SOURCE + "/"));

        assertFalse(RelayPathHelper.isSamePathOrSubpath("/content/other", PATH_SOURCE));
        assertFalse(RelayPathHelper.isSamePathOrSubpath(PATH_SOURCE + "-extra", PATH_SOURCE));
        assertFalse(RelayPathHelper.isSamePathOrSubpath(null, PATH_SOURCE));
        assertTrue(RelayPathHelper.isSamePathOrSubpath(StringUtils.EMPTY, null));
    }

    @Test
    public void shouldReplacePrefix() {
        assertEquals(PATH_TARGET, RelayPathHelper.replace(PATH_SOURCE, PATH_SOURCE, PATH_TARGET));
        assertEquals(PATH_TARGET + "/child", RelayPathHelper.replace(PATH_SOURCE + "/child", PATH_SOURCE, PATH_TARGET));
        assertEquals(PATH_TARGET + "/a/b/c", RelayPathHelper.replace(PATH_SOURCE + "/a/b/c", PATH_SOURCE, PATH_TARGET));
    }

    @Test
    public void shouldReturnPathUnchanged() {
        assertEquals("/content/other", RelayPathHelper.replace("/content/other", PATH_SOURCE, PATH_TARGET));
        assertEquals(PATH_SOURCE + "-extra", RelayPathHelper.replace(PATH_SOURCE + "-extra", PATH_SOURCE, PATH_TARGET));
        assertNull(RelayPathHelper.replace(null, PATH_SOURCE, PATH_TARGET));
    }
}
