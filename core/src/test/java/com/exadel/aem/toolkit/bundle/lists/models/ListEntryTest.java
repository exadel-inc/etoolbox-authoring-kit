/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.exadel.aem.toolkit.bundle.lists.models;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.junit.AemContext;

import static org.junit.Assert.assertEquals;

public class ListEntryTest {

    private static final String SIMPLE_LIST_PATH = "/content/aat-lists/simpleList";
    private static final String LIST_ITEM_PATH = "/jcr:content/list/list_item_1006003058";

    @Rule
    public AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);
    private ListEntry listEntry;

    @Before
    public void setUp() {
        ResourceResolver resolver = context.resourceResolver();
        context.load().json("/com/exadel/aem/toolkit/bundle/lists/util/simpleList.json", SIMPLE_LIST_PATH);
        listEntry = resolver.resolve(SIMPLE_LIST_PATH + LIST_ITEM_PATH).adaptTo(ListEntry.class);
    }

    @Test
    public void getItemResType() {
        final String expected = "/apps/authoring-toolkit/lists/components/content/genericItem";
        String actual1 = listEntry.getItemResType();
        assertEquals(expected, actual1);
    }

    @Test
    public void getProperties() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("jcr:title", "key1");
        expected.put("value", "value1");
        Map<String, Object> actual = listEntry.getProperties();
        assertEquals(expected, actual);
    }
}
