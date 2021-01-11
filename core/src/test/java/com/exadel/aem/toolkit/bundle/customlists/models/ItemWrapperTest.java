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
package com.exadel.aem.toolkit.bundle.customlists.models;

import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ItemWrapperTest {

    private static final String SIMPLE_LIST_PATH = "/content/aem-custom-lists/simpleList";
    private static final String LIST_ITEM_PATH = "/jcr:content/list/list_item_1006003058";

    @Rule
    public AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);
    private ItemWrapper itemWrapper1;

    @Before
    public void setUp() {
        ResourceResolver resolver = context.resourceResolver();
        context.load().json("/com/exadel/aem/toolkit/bundle/customlists/util/simpleList.json", SIMPLE_LIST_PATH);
        itemWrapper1 = resolver.resolve(SIMPLE_LIST_PATH + LIST_ITEM_PATH).adaptTo(ItemWrapper.class);
    }

    @Test
    public void getItemResType() {
        final String expected = "/apps/aem-custom-lists/components/content/genericItem";
        String actual1 = itemWrapper1.getItemResType();
        assertEquals(expected, actual1);
    }

    @Test
    public void getProperties() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("jcr:title", "key1");
        expected.put("value", "value1");
        Map<String, Object> actual = itemWrapper1.getProperties();
        assertEquals(expected, actual);
    }
}
