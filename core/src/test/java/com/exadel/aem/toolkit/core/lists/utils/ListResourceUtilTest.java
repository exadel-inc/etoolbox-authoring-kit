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
package com.exadel.aem.toolkit.core.lists.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Rule;
import org.junit.Test;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.core.CoreConstants;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ListResourceUtilTest {

    @Rule
    public AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

    @Test
    public void shouldTransformKeyValuePairMapToResource() {
        Map<String, Object> properties = Collections.singletonMap("first", "firstValue");

        List<Resource> resources = ListResourceUtil.mapToValueMapResources(context.resourceResolver(), properties);

        Resource resource = resources.get(0);
        assertNotNull(resource);
        assertEquals("first", resource.getValueMap().get(JcrConstants.JCR_TITLE, StringUtils.EMPTY));
        assertEquals("firstValue", resource.getValueMap().get(CoreConstants.PN_VALUE, StringUtils.EMPTY));
    }
}
