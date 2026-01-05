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
package com.exadel.aem.toolkit.core.configurator.servlets.form;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.service.metatype.AttributeDefinition;
import com.adobe.granite.ui.components.FormData;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.configurator.models.internal.ConfigAttribute;
import com.exadel.aem.toolkit.core.configurator.models.internal.ConfigDefinition;

@RunWith(MockitoJUnitRunner.class)
public class ValueUtilTest {

    @Rule
    public AemContext context = AemContextFactory.newInstance(ResourceResolverType.JCR_OAK);

    @Mock
    private ConfigDefinition configDefinition;

    @Mock
    private AttributeDefinition multiValueAttributeDefinition;

    @Mock
    private AttributeDefinition singleValueAttributeDefinition;

    private ConfigAttribute multiValueAttribute;
    private ConfigAttribute singleValueAttribute;

    @Before
    public void setUp() {
        Mockito.when(multiValueAttributeDefinition.getID()).thenReturn("multiValueAttr");
        Mockito.when(multiValueAttributeDefinition.getDefaultValue()).thenReturn(new String[]{"default1", "default2"});
        Mockito.when(multiValueAttributeDefinition.getCardinality()).thenReturn(Integer.MAX_VALUE);
        multiValueAttribute = new ConfigAttribute(
            multiValueAttributeDefinition,
            Arrays.asList("value1", "value2"));

        Mockito.when(singleValueAttributeDefinition.getID()).thenReturn("singleValueAttr");
        Mockito.when(singleValueAttributeDefinition.getDefaultValue()).thenReturn(new String[] {"defaultValue"});
        Mockito.when(singleValueAttributeDefinition.getCardinality()).thenReturn(0);
        singleValueAttribute = new ConfigAttribute(
            singleValueAttributeDefinition,
            "singleValue");

        Mockito.when(configDefinition.getAttributes()).thenReturn(Arrays.asList(multiValueAttribute, singleValueAttribute));
    }

    @Test
    public void shouldProcessRequestWithExistingFormData() {
        Map<String, Object> existingFormData = new HashMap<>();
        existingFormData.put("existing.property", "existingValue");
        ValueMap existingValueMap = new ValueMapDecorator(existingFormData);
        FormData.push(context.request(), existingValueMap, FormData.NameNotFoundMode.IGNORE_FRESHNESS);

        ValueUtil.processRequest(context.request(), configDefinition);

        FormData resultFormData = FormData.from(context.request());
        assertNotNull(resultFormData);
        ValueMap resultValueMap = resultFormData.getValueMap();
        assertEquals("existingValue", resultValueMap.get("existing.property"));
        assertArrayEquals(new Object[]{"value1", "value2"}, (Object[]) resultValueMap.get("./data/multiValueAttr"));
        assertEquals("singleValue", resultValueMap.get("./data/singleValueAttr"));
    }

    @Test
    public void shouldProcessRequestWithoutExistingFormData() {
        ValueUtil.processRequest(context.request(), configDefinition);

        FormData resultFormData = FormData.from(context.request());
        assertNotNull(resultFormData);
        ValueMap resultValueMap = resultFormData.getValueMap();

        assertArrayEquals(new Object[]{"value1", "value2"}, (Object[]) resultValueMap.get("./data/multiValueAttr"));
        assertEquals("singleValue", resultValueMap.get("./data/singleValueAttr"));
    }

    @Test
    public void shouldUseDefaultValuesWhenAttributesAreNull() {
        multiValueAttribute = new ConfigAttribute(multiValueAttributeDefinition, null);
        singleValueAttribute = new ConfigAttribute(singleValueAttributeDefinition, null);
        Mockito.when(configDefinition.getAttributes()).thenReturn(Arrays.asList(multiValueAttribute, singleValueAttribute));

        ValueUtil.processRequest(context.request(), configDefinition);

        FormData resultFormData = FormData.from(context.request());
        assertNotNull(resultFormData);
        ValueMap resultValueMap = resultFormData.getValueMap();

        assertArrayEquals(new Object[]{"default1", "default2"}, (Object[]) resultValueMap.get("./data/multiValueAttr"));
        assertEquals("defaultValue", resultValueMap.get("./data/singleValueAttr"));
    }

    @Test
    public void shouldSkipAttributesWithNullValuesAndDefaults() {
        multiValueAttribute = new ConfigAttribute(multiValueAttributeDefinition, null);
        singleValueAttribute = new ConfigAttribute(singleValueAttributeDefinition, null);
        Mockito.when(multiValueAttributeDefinition.getDefaultValue()).thenReturn(null);
        Mockito.when(singleValueAttributeDefinition.getDefaultValue()).thenReturn(null);
        Mockito.when(configDefinition.getAttributes()).thenReturn(Arrays.asList(multiValueAttribute, singleValueAttribute));

        ValueUtil.processRequest(context.request(), configDefinition);

        FormData resultFormData = FormData.from(context.request());
        assertNotNull(resultFormData);
        ValueMap resultValueMap = resultFormData.getValueMap();
        assertNull(resultValueMap.get("./data/multiValueAttr"));
        assertNull(resultValueMap.get("./data/singleValueAttr"));
    }

    @Test
    public void shouldCoerceAttributesWithDifferentCardinality() {
        ConfigAttribute emptyMultiValueAttr = new ConfigAttribute(multiValueAttributeDefinition, "value");
        ConfigAttribute validSingleValueAttr = new ConfigAttribute(singleValueAttributeDefinition, new String[] {"value", "anotherValue"});
        Mockito.when(configDefinition.getAttributes()).thenReturn(Arrays.asList(emptyMultiValueAttr, validSingleValueAttr));

        ValueUtil.processRequest(context.request(), configDefinition);

        FormData resultFormData = FormData.from(context.request());
        assertNotNull(resultFormData);
        ValueMap resultValueMap = resultFormData.getValueMap();
        assertArrayEquals(new Object[] {"value"}, (Object[]) resultValueMap.get("./data/multiValueAttr"));
        assertEquals("value", resultValueMap.get("./data/singleValueAttr"));
    }
}


