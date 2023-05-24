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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * Contains utility methods for serializing and deserializing as well as learning objects' structure with use of the
 * {@code Jackson} logic
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class ObjectConversionUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectConversionUtil.class);

    private static final TypeReference<Map<String, Object>> PROPERTY_MAP_REFERENCE = new TypeReference<Map<String, Object>>() {
    };
    private static final ObjectMapper OBJECT_MAPPER;
    private static final String EXCEPTION_COULD_NOT_PARSE = "Could not parse JSON value";
    private static final String EXCEPTION_COULD_NOT_READ = "Could not read the object";

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setAnnotationIntrospector(new LocalAnnotationIntrospector());
    }

    /**
     * Default (instantiation-restricting) constructor
     */
    private ObjectConversionUtil() {
    }

    /**
     * Gets whether the given string value represents a valid JSON object
     * @param value A nullable string value
     * @return True or false
     */
    public static boolean isJson(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(value);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        } catch (IOException e) {
            LOG.error(EXCEPTION_COULD_NOT_PARSE, e);
            return false;
        }
    }

    /**
     * Analyzes the structure of the given Java entity and converts its available methods and accessors into the
     * property map. The method reveals the same fields and methods that the {@code Jackson} serializer would find
     * @param value An arbitrary object
     * @return A nullable map instance
     */
    public static Map<String, Object> toPropertyMap(Object value) {
        try {
            return OBJECT_MAPPER.convertValue(value, PROPERTY_MAP_REFERENCE);
        } catch (IllegalArgumentException e) {
            LOG.error(EXCEPTION_COULD_NOT_READ, e);
        }
        return Collections.emptyMap();
    }

    /**
     * Parses the provided string value that expectedly represents a JSON into a {@link JsonNode} object
     * @param value String value; a non-null string is expected
     * @return A nullable {@link JsonNode} value
     * @throws IOException if the conversion was not possible or failed
     */
    public static JsonNode toNodeTree(String value) throws IOException {
        return OBJECT_MAPPER.readTree(value);
    }

    /* ---------------
       Service classes
       --------------- */

    /**
     * Extends {@link JacksonAnnotationIntrospector} to add support for ToolKit-specific annotations used in
     * entity-to-map object mapping
     */
    private static class LocalAnnotationIntrospector extends JacksonAnnotationIntrospector {
        /**
         * Checks whether the given value can be ignored in the process of serialization
         * @param value An arbitrary value which has a {@code Jackson} annotation attached
         * @return True or false
         */
        @Override
        protected boolean _isIgnorable(Annotated value) {
            if (super._isIgnorable(value)) {
                return true;
            }
            if (value.hasAnnotation(Transient.class)) {
                return true;
            }
            if (value.getAnnotated() instanceof Field) {
                Field field = (Field) value.getAnnotated();
                return Modifier.isTransient(field.getModifiers());
            }
            return false;
        }
    }
}
