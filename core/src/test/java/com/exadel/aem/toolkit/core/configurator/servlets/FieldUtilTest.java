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
package com.exadel.aem.toolkit.core.configurator.servlets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.service.metatype.AttributeDefinition;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.models.ConfigAttribute;
import com.exadel.aem.toolkit.core.configurator.models.ConfigDefinition;

@RunWith(MockitoJUnitRunner.class)
public class FieldUtilTest {

    @Rule
    public AemContext context = AemContextFactory.newInstance(ResourceResolverType.JCR_OAK);

    @Mock
    private ConfigDefinition configDefinition;

    @Mock
    private AttributeDefinition textAttributeDefinition;

    @Mock
    private AttributeDefinition checkboxAttributeDefinition;

    @Mock
    private AttributeDefinition selectAttributeDefinition;

    @Mock
    private AttributeDefinition multiValueAttributeDefinition;

    private ConfigAttribute textAttribute;
    private ConfigAttribute checkboxAttribute;
    private ConfigAttribute selectAttribute;
    private ConfigAttribute multiValueAttribute;

    @Before
    public void setUp() throws PersistenceException {
        Resource resource = context.create().resource("/content/test");
        context.resourceResolver().commit();
        context.request().setResource(resource);

        Mockito.when(textAttributeDefinition.getID()).thenReturn("textField");
        Mockito.when(textAttributeDefinition.getName()).thenReturn("Text Field");
        Mockito.when(textAttributeDefinition.getDescription()).thenReturn("A text field description");
        Mockito.when(textAttributeDefinition.getType()).thenReturn(AttributeDefinition.STRING);
        Mockito.when(textAttributeDefinition.getCardinality()).thenReturn(0);
        Mockito.when(textAttributeDefinition.getOptionValues()).thenReturn(null);
        textAttribute = new ConfigAttribute(textAttributeDefinition, "test value");

        Mockito.when(checkboxAttributeDefinition.getID()).thenReturn("checkboxField");
        Mockito.when(checkboxAttributeDefinition.getName()).thenReturn("Checkbox Field");
        Mockito.when(checkboxAttributeDefinition.getType()).thenReturn(AttributeDefinition.BOOLEAN);
        Mockito.when(checkboxAttributeDefinition.getCardinality()).thenReturn(0);
        Mockito.when(checkboxAttributeDefinition.getOptionValues()).thenReturn(null);
        checkboxAttribute = new ConfigAttribute(checkboxAttributeDefinition, true);

        Mockito.when(selectAttributeDefinition.getID()).thenReturn("selectField");
        Mockito.when(selectAttributeDefinition.getName()).thenReturn("Select Field");
        Mockito.when(selectAttributeDefinition.getCardinality()).thenReturn(0);
        Mockito.when(selectAttributeDefinition.getOptionValues()).thenReturn(new String[]{"option1", "option2", "option3"});
        Mockito.when(selectAttributeDefinition.getOptionLabels()).thenReturn(new String[]{"Option 1", "Option 2", "Option 3"});
        selectAttribute = new ConfigAttribute(selectAttributeDefinition, "option1");

        Mockito.when(multiValueAttributeDefinition.getID()).thenReturn("multiValueField");
        Mockito.when(multiValueAttributeDefinition.getName()).thenReturn("Multi Value Field");
        Mockito.when(multiValueAttributeDefinition.getType()).thenReturn(AttributeDefinition.STRING);
        Mockito.when(multiValueAttributeDefinition.getCardinality()).thenReturn(Integer.MAX_VALUE);
        Mockito.when(multiValueAttributeDefinition.getOptionValues()).thenReturn(null);
        multiValueAttribute = new ConfigAttribute(multiValueAttributeDefinition, new String[]{"value1", "value2"});

        Mockito.when(configDefinition.getId()).thenReturn("test.config.pid");
        Mockito.when(configDefinition.getName()).thenReturn("Test Configuration");
        Mockito.when(configDefinition.getDescription()).thenReturn("Test configuration description");
        Mockito.when(configDefinition.isFactoryInstance()).thenReturn(false);
        Mockito.when(configDefinition.isModified()).thenReturn(true);
        Mockito.when(configDefinition.isPublished()).thenReturn(false);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldHandleAttributes() {
        Mockito.when(configDefinition.getAttributes())
            .thenReturn(Arrays.asList(textAttribute, checkboxAttribute, selectAttribute, multiValueAttribute));

        FieldUtil.processRequest(context.request(), configDefinition);

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        assertNotNull(dataSource);
        assertRequiredResourcesPresent(dataSource);

        List<Resource> resources = (List<Resource>) IteratorUtils.toList(dataSource.iterator());
        assertTrue(resources.stream().anyMatch(r -> ResourceTypes.TEXTFIELD.equals(r.getResourceType())));
        assertTrue(resources.stream().anyMatch(r -> ResourceTypes.CHECKBOX.equals(r.getResourceType())));
        assertTrue(resources.stream().anyMatch(r -> ResourceTypes.SELECT.equals(r.getResourceType())));
        assertTrue(resources.stream().anyMatch(r -> ResourceTypes.MULTIFIELD.equals(r.getResourceType())));
    }

    @Test
    public void shouldHandleEmptyAttributes() {
        Mockito.when(configDefinition.getAttributes()).thenReturn(Collections.emptyList());

        FieldUtil.processRequest(context.request(), configDefinition);

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        assertNotNull(dataSource);
        assertRequiredResourcesPresent(dataSource);
    }

    @Test
    public void shouldCreateAlertField() {
        Resource alertField = FieldUtil.newAlert(
            context.request(),
            "Test alert message with ${el}",
            "warning");

        ValueMap properties = alertField.getValueMap();
        assertEquals(ResourceTypes.ALERT, properties.get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY));
        assertEquals("Test alert message with \\${el}", properties.get(CoreConstants.PN_TEXT));
        assertEquals("warning", properties.get("variant"));
        assertEquals("centered", properties.get("granite:class"));
    }

    @Test
    public void shouldSkipNameHintAttribute() {
        AttributeDefinition nameHintDefinition = Mockito.mock(AttributeDefinition.class);
        Mockito.when(nameHintDefinition.getID()).thenReturn("webconsole.configurationFactory.nameHint");
        ConfigAttribute nameHintAttribute = new ConfigAttribute(nameHintDefinition, "hint value");

        Mockito.when(configDefinition.getAttributes()).thenReturn(
            java.util.Arrays.asList(nameHintAttribute, textAttribute));

        FieldUtil.processRequest(context.request(), configDefinition);

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        assertNotNull(dataSource);
        assertTrue(dataSource.iterator().hasNext());
    }


    @SuppressWarnings("unchecked")
    private void assertRequiredResourcesPresent(DataSource dataSource) {
        List<Resource> resources = (List<Resource>) IteratorUtils.toList(dataSource.iterator());

        assertEquals(ResourceTypes.HEADING, resources.get(0).getResourceType());
        assertEquals(ResourceTypes.TEXT, resources.get(1).getResourceType());

        String[] hiddenFieldIds = resources.stream()
            .filter(res -> ResourceTypes.HIDDEN.equals(res.getResourceType()))
            .map(res -> res.getValueMap().get("granite:id", String.class))
            .filter(StringUtils::isNotEmpty)
            .toArray(String[]::new);
        assertArrayEquals(new String[] {"ownPath", "modified", "published"}, hiddenFieldIds);

        String[] hiddenFieldValues = resources.stream()
            .filter(res -> ResourceTypes.HIDDEN.equals(res.getResourceType()))
            .filter(res -> res.getValueMap().get(CoreConstants.PN_NAME, String.class) != null)
            .map(res -> res.getValueMap().get(CoreConstants.PN_NAME, String.class)
                + "=" + res.getValueMap().get(CoreConstants.PN_VALUE, String.class))
            .filter(str -> !str.contains("@TypeHint"))
            .toArray(String[]::new);
        assertArrayEquals(
            new String[]{
                "./jcr:primaryType=nt:unstructured",
                "./data/jcr:primaryType=nt:unstructured",
                "./sling:resourceType=/bin/etoolbox/authoring-kit/config"
            },
            hiddenFieldValues);
    }
}
