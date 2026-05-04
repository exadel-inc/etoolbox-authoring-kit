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

import java.util.Iterator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.junit.Rule;
import org.junit.Test;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;

public class RelayResourceTest {

    private static final String PATH_TARGET = "/content/target";
    private static final String PATH_SOURCE = "/content/source";
    private static final String PATH_CHILD = "/child";
    private static final String PATH_NO_SLASH = "noSlash";

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    @Test
    public void shouldCopyAndOverrideProperties() {
        Resource plain = context.create().resource(PATH_TARGET);
        RelayResource relay = new RelayResource(plain, PATH_SOURCE);

        assertEquals(PATH_SOURCE, relay.getPath());
        assertSame(plain, relay.getResource());
        assertNotSame(plain.getResourceMetadata(), relay.getResourceMetadata());
        assertEquals(PATH_SOURCE, relay.getResourceMetadata().get(ResourceMetadata.RESOLUTION_PATH));
    }

    @Test
    public void shouldGetName() {
        Resource plain = context.create().resource(PATH_TARGET);

        assertEquals("source", new RelayResource(plain, PATH_SOURCE).getName());
        // Path without any slash: returned as-is
        assertEquals(PATH_NO_SLASH, new RelayResource(plain, PATH_NO_SLASH).getName());
    }

    @Test
    public void shouldGetParent() {
        final String pathRelayParent = "/relay";
        Resource original = context.create().resource(PATH_TARGET);
        context.create().resource(pathRelayParent);

        RelayResource relay = new RelayResource(original, pathRelayParent + "/item");
        Resource parent = relay.getParent();
        assertNotNull(parent);
        assertEquals(pathRelayParent, parent.getPath());

        // Top-level path: substringBeforeLast("/top", "/") is blank → null
        assertNull(new RelayResource(original, "/top").getParent());

        // Path with no slash: no parent branch → null
        assertNull(new RelayResource(original, PATH_NO_SLASH).getParent());

        // Parent /missing does not exist in resolver → null
        assertNull(new RelayResource(original, "/missing/item").getParent());
    }

    @Test
    public void shouldGetChild() {
        Resource original = context.create().resource(PATH_TARGET);
        context.create().resource(PATH_SOURCE + PATH_CHILD);
        RelayResource relay = new RelayResource(original, PATH_SOURCE);

        Resource child = relay.getChild("child");
        assertNotNull(child);
        assertEquals(PATH_SOURCE + PATH_CHILD, child.getPath());

        // Relative path with leading/trailing slashes is stripped
        Resource childFromSlashed = relay.getChild("/child/");
        assertNotNull(childFromSlashed);
        assertEquals(PATH_SOURCE + PATH_CHILD, childFromSlashed.getPath());

        assertNull(relay.getChild("nonexistent"));
    }

    @Test
    public void shouldListChildrenWithOverriddenPath() {
        Resource parent = context.create().resource(PATH_TARGET);
        context.create().resource(PATH_TARGET + PATH_CHILD);
        RelayResource relay = new RelayResource(parent, PATH_SOURCE);

        Iterator<Resource> children = relay.listChildren();

        assertNotNull(children);
        assertTrue(children.hasNext());
        Resource child = children.next();
        assertEquals(PATH_SOURCE + PATH_CHILD, child.getPath());
        assertTrue(child instanceof RelayResource);
        assertFalse(children.hasNext());
    }
}
