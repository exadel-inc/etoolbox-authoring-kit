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
package com.exadel.aem.toolkit.core.utils;

import java.beans.Transient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.CoreConstants;

@SuppressWarnings("unused")
public class ObjectConversionUtilTest {

    private static final String PN_VISIBLE = "visible";
    private static final String VALUE_VISIBLE = "visible-value";
    private static final String VALUE_HIDDEN = "hidden-value";

    @Test
    public void shouldDetectValidJson() {
        assertTrue(ObjectConversionUtil.isJson("{\"key\": \"value\"}"));
        assertTrue(ObjectConversionUtil.isJson("[1, 2, 3]"));
        assertTrue(ObjectConversionUtil.isJson("42"));
        assertTrue(ObjectConversionUtil.isJson("null"));
        assertTrue(ObjectConversionUtil.isJson("true"));
        assertTrue(ObjectConversionUtil.isJson("false"));
        assertTrue(ObjectConversionUtil.isJson("\"hello\""));
    }

    @Test
    public void shouldRejectBlankOrInvalidJson() {
        assertFalse(ObjectConversionUtil.isJson(null));
        assertFalse(ObjectConversionUtil.isJson(""));
        assertFalse(ObjectConversionUtil.isJson("   "));
        assertFalse(ObjectConversionUtil.isJson("{not valid json}"));
        assertFalse(ObjectConversionUtil.isJson("{\"key\": \"value\""));
    }

    @Test
    public void shouldParseJsonPrimitivesToTypedObjects() {
        assertEquals("hello", ObjectConversionUtil.toObject("\"hello\"", String.class));
        assertEquals(Integer.valueOf(42), ObjectConversionUtil.toObject("42", Integer.class));
        assertEquals(Boolean.TRUE, ObjectConversionUtil.toObject("true", Boolean.class));
    }

    @Test
    public void shouldParseJsonObjectToBean() {
        SimpleBean bean = ObjectConversionUtil.toObject("{\"name\":\"test\",\"value\":7}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.getName());
        assertEquals(7, bean.getValue());
    }

    @Test
    public void shouldReturnNullForBlankNullOrUnparsableInput() {
        assertNull(ObjectConversionUtil.toObject(null, String.class));
        assertNull(ObjectConversionUtil.toObject("", String.class));
        assertNull(ObjectConversionUtil.toObject("   ", String.class));
        // Bare word is not a valid JSON
        assertNull(ObjectConversionUtil.toObject("not-a-number", Integer.class));
    }

    @Test
    public void shouldConvertPojoToPropertyMap() {
        Map<String, Object> simple = ObjectConversionUtil.toPropertyMap(new SimpleBean("hello", 42));
        assertNotNull(simple);
        assertEquals(2, simple.size());
        assertEquals("hello", simple.get(CoreConstants.PN_NAME));
        assertEquals(42, simple.get(CoreConstants.PN_VALUE));

        // Bean with nested object serialized as a sub-map
        Map<String, Object> withNested = ObjectConversionUtil.toPropertyMap(
            new BeanWithNestedObject("outer", new SimpleBean("inner", 5)));
        assertNotNull(withNested);
        assertEquals(2, withNested.size());
        assertEquals("outer", withNested.get("label"));
        Object childValue = withNested.get("child");
        assertTrue(childValue instanceof Map);
        Map<?, ?> childMap = (Map<?, ?>) childValue;
        assertEquals("inner", childMap.get(CoreConstants.PN_NAME));
        assertEquals(5, childMap.get(CoreConstants.PN_VALUE));

        // Map input passes through as an equivalent map
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("a", 1);
        sourceMap.put("b", "two");
        Map<String, Object> fromMap = ObjectConversionUtil.toPropertyMap(sourceMap);
        assertNotNull(fromMap);
        assertEquals(2, fromMap.size());
        assertEquals(1, fromMap.get("a"));
        assertEquals("two", fromMap.get("b"));
    }

    @Test
    public void shouldReturnNullForNullPropertyMapInput() {
        assertNull(ObjectConversionUtil.toPropertyMap(null));
    }

    @Test
    public void shouldReturnEmptyMapForNoPropertiesOrConversionError() {
        // Empty bean has no serializable properties → empty map, not null
        Map<String, Object> fromEmpty = ObjectConversionUtil.toPropertyMap(new EmptyBean());
        assertNotNull(fromEmpty);
        assertTrue(fromEmpty.isEmpty());

        // A plain String cannot be deserialized as a Map → IllegalArgumentException caught → emptyMap
        Map<String, Object> fromError = ObjectConversionUtil.toPropertyMap("a plain string");
        assertNotNull(fromError);
        assertTrue(fromError.isEmpty());
    }

    @Test
    public void shouldExcludeIgnoredProperties() {
        // @java.beans.Transient on getter
        assertSingleVisibleProperty(ObjectConversionUtil.toPropertyMap(new BeanWithTransientAnnotation()));

        // transient field modifier
        assertSingleVisibleProperty(ObjectConversionUtil.toPropertyMap(new BeanWithTransientField()));

        // @JsonIgnore triggers super._isIgnorable in LocalAnnotationIntrospector
        assertSingleVisibleProperty(ObjectConversionUtil.toPropertyMap(new BeanWithJsonIgnore()));

        // Both @Transient annotation and transient modifier present at once
        assertSingleVisibleProperty(ObjectConversionUtil.toPropertyMap(new BeanWithBothTransientMechanisms()));
    }

    @Test
    public void shouldParseJsonToNodeTree() throws IOException {
        JsonNode objectNode = ObjectConversionUtil.toNodeTree("{\"key\": \"value\"}");
        assertNotNull(objectNode);
        JsonNode keyNode = objectNode.get("key");
        assertNotNull(keyNode);
        assertEquals("value", keyNode.asText());

        // JSON array with two elements — validates caller usage pattern in InlineOptionSourceResolver
        JsonNode arrayNode = ObjectConversionUtil.toNodeTree("[{\"name\":\"a\"},{\"name\":\"b\"}]");
        assertNotNull(arrayNode);
        assertTrue(arrayNode.isArray());
        assertEquals(2, arrayNode.size());
        JsonNode firstName = arrayNode.get(0).get(CoreConstants.PN_NAME);
        assertNotNull(firstName);
        assertEquals("a", firstName.asText());
        JsonNode secondName = arrayNode.get(1).get(CoreConstants.PN_NAME);
        assertNotNull(secondName);
        assertEquals("b", secondName.asText());

        // Nested JSON — validates traversal pattern used by HttpOptionSourceResolver
        JsonNode nestedNode = ObjectConversionUtil.toNodeTree("{\"outer\": {\"inner\": \"deep\"}}");
        assertNotNull(nestedNode);
        JsonNode outerNode = nestedNode.get("outer");
        assertNotNull(outerNode);
        JsonNode innerNode = outerNode.get("inner");
        assertNotNull(innerNode);
        assertEquals("deep", innerNode.asText());
    }

    @Test(expected = IOException.class)
    public void shouldThrowOnInvalidJsonInNodeTree() throws IOException {
        ObjectConversionUtil.toNodeTree("{not valid}");
    }

    /* -------
       Fixtures
       ------- */

    private static void assertSingleVisibleProperty(Map<String, Object> map) {
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(VALUE_VISIBLE, map.get(PN_VISIBLE));
    }

    private static class SimpleBean {
        private String name;
        private int value;

        // Required by Jackson for JSON deserialization
        SimpleBean() {}

        SimpleBean(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    private static class BeanWithNestedObject {
        private final String label;
        private final SimpleBean child;

        BeanWithNestedObject(String label, SimpleBean child) {
            this.label = label;
            this.child = child;
        }

        public String getLabel() {
            return label;
        }

        public SimpleBean getChild() {
            return child;
        }
    }

    private static class EmptyBean {}

    private static class BeanWithTransientAnnotation {
        public String getVisible() {
            return VALUE_VISIBLE;
        }

        @Transient
        public String getHidden() {
            return VALUE_HIDDEN;
        }
    }

    private static class BeanWithTransientField {
        public String visible = VALUE_VISIBLE;
        public transient String hidden = VALUE_HIDDEN;
    }

    private static class BeanWithJsonIgnore {
        public String getVisible() {
            return VALUE_VISIBLE;
        }

        @JsonIgnore
        public String getHidden() {
            return VALUE_HIDDEN;
        }
    }

    private static class BeanWithBothTransientMechanisms {
        public transient String hiddenField = VALUE_HIDDEN;

        public String getVisible() {
            return VALUE_VISIBLE;
        }

        @Transient
        public String getHiddenAnnotated() {
            return VALUE_HIDDEN;
        }
    }
}
