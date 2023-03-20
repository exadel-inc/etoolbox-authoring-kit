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
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class ObjectConversionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectConversionUtil.class);

    private static final String EMPTY_JSON = "{}";

    private static final TypeReference<Map<String, Object>> PROPERTY_MAP_REFERENCE = new TypeReference<Map<String, Object>>() {};
    private static final ObjectMapper OBJECT_MAPPER;
    private static final String EXCEPTION_COULD_NOT_SERIALIZE = "Could not serialize to JSON";


    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setAnnotationIntrospector(new LocalAnnotationIntrospector());
    }

    private ObjectConversionUtil() {
    }

    public static Map<String, Object> toPropertyMap(Object value) {
        return OBJECT_MAPPER.convertValue(value, PROPERTY_MAP_REFERENCE);
    }

    public static String toJson(Object value) {
        return toJson(value, e -> {
            LOG.error("Could not serialize to JSON", e);
            return EMPTY_JSON;
        });
    }

    public static String toJson(Object value, Function<Exception, String> fallback) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return fallback.apply(e);
        }
    }

    public static JsonNode toNodeTree(String value) throws IOException {
        return OBJECT_MAPPER.readTree(value);
    }

    public static JsonNode toNodeTree(InputStream input) throws IOException {
        return OBJECT_MAPPER.readTree(input);
    }

    public static JsonNode toNodeTree(Object value) {
        return OBJECT_MAPPER.valueToTree(value);
    }

    /**
     * Extends {@link JacksonAnnotationIntrospector} to add support for ToolKit-specific annotations used in
     * entity-to-map object mapping
     */
    private static class LocalAnnotationIntrospector extends JacksonAnnotationIntrospector {
        @Override
        protected boolean _isIgnorable(Annotated a) {
            if (super._isIgnorable(a)) {
                return true;
            }
            if (a.hasAnnotation(Transient.class)) {
                return true;
            }
            if (a.getAnnotated() instanceof Field) {
                Field field = (Field) a.getAnnotated();
                return Modifier.isTransient(field.getModifiers());
            }
            return false;
        }
    }
}
