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
package com.exadel.aem.toolkit.core.configurator.services;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import static junitx.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;

public class ConfigUtilTest {

    @Test
    public void shouldConvertValueMapToDictionary() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("string.property", "test value");
        sourceMap.put("int.property", 42);
        sourceMap.put("boolean.property", true);
        sourceMap.put("double.property", 3.14);

        sourceMap.put("string.array", new String[]{"value1", "value2"});
        sourceMap.put("int.array", new int[]{1, 2, 3});
        sourceMap.put("empty.array", new String[0]);

        sourceMap.put("string.list", Arrays.asList("value1", "value2"));
        sourceMap.put("int.list", Arrays.asList(1, 2, 3));
        sourceMap.put("mixed.list", Arrays.asList("string", 42, true));
        sourceMap.put("null.list", Arrays.asList("value", null, "another"));
        sourceMap.put("empty.list", Collections.emptyList());

        ValueMap valueMap = new ValueMapDecorator(sourceMap);

        Dictionary<String, ?> result = ConfigUtil.toDictionary(valueMap);
        assertNotNull(result);

        assertEquals("test value", result.get("string.property"));
        assertEquals(42, result.get("int.property"));
        assertEquals(true, result.get("boolean.property"));
        assertEquals(3.14, result.get("double.property"));

        assertEquals(2, Array.getLength(result.get("string.array")));
        assertEquals(3, Array.getLength(result.get("int.array")));
        assertEquals(0, Array.getLength(result.get("empty.array")));

        assertEquals(2, CollectionUtils.size(result.get("string.list")));
        assertEquals(3, CollectionUtils.size(result.get("int.list")));
        assertEquals(3, CollectionUtils.size(result.get("mixed.list")));
        assertNull(result.get("null.list")); // Should be filtered out due to null element
        assertEquals(0, CollectionUtils.size(result.get("empty.list")));
    }

    @Test
    public void shouldExcludeSystemProperties() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("jcr:primaryType", "nt:unstructured");
        sourceMap.put("jcr:created", "2023-01-01");
        sourceMap.put("sling:resourceType", "test/component");
        sourceMap.put("normal.property", "test value");
        ValueMap valueMap = new ValueMapDecorator(sourceMap);

        Dictionary<String, ?> result = ConfigUtil.toDictionary(valueMap);
        assertNotNull(result);

        assertEquals("test value", result.get("normal.property"));
        assertNull(result.get("jcr:primaryType"));
        assertNull(result.get("jcr:created"));
        assertNull(result.get("sling:resourceType"));
    }

    @Test
    public void shouldExcludeUnsupportedTypesFromDictionary() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("string.property", "valid");
        sourceMap.put("object.property", new Object());
        sourceMap.put("null.property", null);
        ValueMap valueMap = new ValueMapDecorator(sourceMap);

        Dictionary<String, ?> result = ConfigUtil.toDictionary(valueMap);
        assertNotNull(result);

        assertEquals("valid", result.get("string.property"));
        assertNull(result.get("object.property"));
        assertNull(result.get("null.property"));
    }

    @Test
    public void shouldConvertDictionaryToValueMap() {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put("string.property", "test value");
        dictionary.put("int.property", 42);
        dictionary.put("boolean.property", true);
        dictionary.put("double.property", 3.14);

        dictionary.put("string.array", new String[]{"value1", "value2"});
        dictionary.put("int.array", new int[]{1, 2, 3});

        dictionary.put("string.list", Arrays.asList("value1", "value2"));
        dictionary.put("int.list", Arrays.asList(1, 2, 3));
        dictionary.put("mixed.list", Arrays.asList("string", 42, true));
        dictionary.put("null.list", Arrays.asList("value", null, "another"));

        Map<String, Object> result = ConfigUtil.toMap(dictionary);
        assertNotNull(result);

        assertEquals("test value", result.get("string.property"));
        assertEquals(42, result.get("int.property"));
        assertEquals(Boolean.TRUE, result.get("boolean.property"));
        assertEquals(3.14, result.get("double.property"));

        assertEquals(2, Array.getLength(result.get("string.array")));
        assertEquals(3, Array.getLength(result.get("int.array")));

        assertEquals(2, CollectionUtils.size(result.get("string.list")));
        assertEquals(3, CollectionUtils.size(result.get("int.list")));
        assertEquals(3, CollectionUtils.size(result.get("mixed.list")));
        assertNull(result.get("null.list")); // Should be filtered out due to null element
    }

    @Test
    public void shouldExcludeServiceProperties() {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put("service.pid", "com.example.service");
        dictionary.put("service.factoryPid", "com.example.factory");
        dictionary.put("normal.property", "test value");

        Map<String, Object> result = ConfigUtil.toMap(dictionary);
        assertNotNull(result);
        assertEquals("test value", result.get("normal.property"));
        assertNull(result.get("service.pid"));
        assertNull(result.get("service.factoryPid"));
    }

    @Test
    public void shouldExcludeUnsupportedTypesFromMap() {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put("string.property", "valid");
        dictionary.put("object.property", new Object());

        Map<String, Object> result = ConfigUtil.toMap(dictionary);
        assertNotNull(result);
        assertEquals("valid", result.get("string.property"));
        assertNull(result.get("object.property"));
    }

    @Test
    public void shouldHandleEmptyDictionary() {
        Dictionary<String, Object> dictionary = new Hashtable<>();

        Map<String, Object> result = ConfigUtil.toMap(dictionary);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        dictionary = null;

        result = ConfigUtil.toMap(dictionary);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings({"UnnecessaryBoxing"})
    @Test
    public void shouldReturnTrueForEqualDictionaryAndMap() {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put("string.property", "test value");
        dictionary.put("int.property", 42);
        dictionary.put("long.property", 42L);
        dictionary.put("boolean.property", true);
        dictionary.put("string.array", new String[]{"value1", "value2"});
        dictionary.put("int.list", Arrays.asList(1, 2, 3));
        dictionary.put("dummy$eakbackup", "dummy"); // Should be ignored

        Map<String, Object> map = new HashMap<>();
        map.put("string.property", "test value");
        map.put("int.property", 42);
        map.put("long.property", Long.valueOf(42L));
        map.put("boolean.property", Boolean.valueOf(true));
        map.put("string.array", new String[]{"value1", "value2"});
        map.put("int.list", Arrays.asList(1, 2, Integer.getInteger("3")));

        assertTrue(ConfigUtil.equals(dictionary, map));
        map.put("string.array", Arrays.asList("value1", "value2"));
        assertTrue(ConfigUtil.equals(dictionary, map));
    }

    @Test
    public void shouldReturnFalseForDifferingDictionaryAndMap() {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put("string.property", "test value");
        dictionary.put("int.property", 42);

        Map<String, Object> map = new HashMap<>();
        map.put("string.property", "different value");
        map.put("int.property", 42);

        assertFalse(ConfigUtil.equals(dictionary, map));
    }

    @Test
    public void shouldHandleNullValuesInEquals() {
        assertTrue(ConfigUtil.equals(null, null));
        assertFalse(ConfigUtil.equals(new Hashtable<>(), null));
        assertFalse(ConfigUtil.equals(null, new HashMap<>()));

        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put("string.property", "test value");

        Map<String, Object> map = new HashMap<>();
        map.put("string.property", "test value");
        map.put("null.property", null);

        assertTrue(ConfigUtil.equals(dictionary, map));
    }

    @Test
    public void shouldIgnoreServicePropertiesInEquals() {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put("service.pid", "com.example.service");
        dictionary.put("service.factoryPid", "com.example.factory");
        dictionary.put("string.property", "test value");

        Map<String, Object> map = new HashMap<>();
        map.put("string.property", "test value");

        assertTrue(ConfigUtil.equals(dictionary, map));
    }

    @Test
    public void shouldCompareArraysAndLists() {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put("array.property", new String[]{"value1", "value2"});

        Map<String, Object> map = new HashMap<>();
        map.put("array.property", Arrays.asList("value1", "value2"));

        assertTrue(ConfigUtil.equals(dictionary, map));
    }

    @Test
    public void shouldExtractBackupValues() {
        Configuration configuration = createMockConfiguration();

        Dictionary<String, Object> result = ConfigUtil.getBackup(configuration);
        assertNotNull(result);

        assertEquals("backup value 1", result.get("property1"));
        assertEquals("backup value 2", result.get("property2"));
        assertEquals(42, result.get("number"));
        assertNull(result.get("normal.property")); // Should not be included as it's not a backup
        assertNull(result.get("service.pid")); // Should not be included as it's not a backup
    }

    @Test
    public void shouldExtractValidData() {
        Configuration configuration = createMockConfiguration();

        Dictionary<String, Object> result = ConfigUtil.getData(configuration);
        assertNotNull(result);

        assertEquals("test value", result.get("normal.property"));
        assertEquals(123, result.get("another.property"));
        assertEquals(true, result.get("boolean.property"));
        assertNull(result.get("service.pid")); // Should be excluded
        assertNull(result.get("service.factoryPid")); // Should be excluded
        assertNull(result.get("property1" + ConfiguratorConstants.SUFFIX_BACKUP)); // Should be excluded
        assertNull(result.get(ConfiguratorConstants.ATTR_CONFIGURATOR)); // Should be excluded
        assertNull(result.get(ConfiguratorConstants.ATTR_NAME_HINT)); // Should be excluded
    }

    private Configuration createMockConfiguration() {
        Configuration configuration = mock(Configuration.class);

        Dictionary<String, Object> props = new Hashtable<>();

        // Normal properties that should be included in getData
        props.put("normal.property", "test value");
        props.put("another.property", 123);
        props.put("boolean.property", true);

        // Backup properties that should be included in getBackup
        props.put("property1" + ConfiguratorConstants.SUFFIX_BACKUP, "backup value 1");
        props.put("property2" + ConfiguratorConstants.SUFFIX_BACKUP, "backup value 2");
        props.put("number" + ConfiguratorConstants.SUFFIX_BACKUP, 42);

        // Excluded properties
        props.put("service.pid", "com.example.service");
        props.put("service.factoryPid", "com.example.factory");
        props.put(ConfiguratorConstants.ATTR_CONFIGURATOR, "layout.value");
        props.put(ConfiguratorConstants.ATTR_NAME_HINT, "name.hint");

        when(configuration.getProperties()).thenReturn(props);
        when(configuration.getPid()).thenReturn("test.pid");
        when(configuration.getFactoryPid()).thenReturn("test.factory.pid");

        return configuration;
    }
}
