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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PathHelperTest {

    private static final String PATH_SOURCE = "/content/source";
    private static final String PATH_TARGET = "/content/target";

    @Test
    public void shouldReplacePrefix() {
        assertEquals(PATH_TARGET, PathHelper.replace(PATH_SOURCE, PATH_SOURCE, PATH_TARGET));
        assertEquals(PATH_TARGET + "/child", PathHelper.replace(PATH_SOURCE + "/child", PATH_SOURCE, PATH_TARGET));
        assertEquals(PATH_TARGET + "/a/b/c", PathHelper.replace(PATH_SOURCE + "/a/b/c", PATH_SOURCE, PATH_TARGET));
    }

    @Test
    public void shouldReturnPathUnchanged() {
        assertEquals("/content/other", PathHelper.replace("/content/other", PATH_SOURCE, PATH_TARGET));
        assertEquals(PATH_SOURCE + "-extra", PathHelper.replace(PATH_SOURCE + "-extra", PATH_SOURCE, PATH_TARGET));
        assertNull(PathHelper.replace(null, PATH_SOURCE, PATH_TARGET));
    }
}
