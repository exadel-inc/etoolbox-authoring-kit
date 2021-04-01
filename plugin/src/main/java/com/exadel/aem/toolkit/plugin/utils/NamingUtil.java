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
package com.exadel.aem.toolkit.plugin.utils;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.handlers.Target;

/**
 * Contains utility methods for creating valid entity names
 */
public class NamingUtil {

    private static final String PREFIX_GET = "get";

    private static final NamingHelper FIELD_HELPER = NamingHelper.forFieldName();
    private static final NamingHelper FIELD_PREFIX_HELPER = NamingHelper.forFieldNamePrefix();
    private static final NamingHelper FIELD_POSTFIX_HELPER = NamingHelper.forFieldNamePostfix();
    private static final NamingHelper NODE_NAME_HELPER = NamingHelper.forNodeName();
    private static final NamingHelper PLAIN_NAME_HELPER = NamingHelper.forPlainName();

    /**
     * Default (instantiation-preventing) constructor
     */
    private NamingUtil() {
    }

    /**
     * Creates a valid {@code name} property for a Granite UI field
     * @param value String used as the source for the name
     * @return String value
     */
    public static String getValidFieldName(String value) {
        return FIELD_HELPER.getValidName(value, DialogConstants.NN_FIELD);
    }

    /**
     * Creates a valid prefix for the {@code name} property of a Granite UI field
     * @param value String used as the source for the prefix
     * @return String value
     */
    public static String getValidFieldPrefix(String value) {
        return FIELD_PREFIX_HELPER.getValidName(value, StringUtils.EMPTY);
    }

    /**
     * Creates a valid postfix for the {@code name} property of a Granite UI field
     * @param value String used as the source for the postfix
     * @return String value
     */
    public static String getValidFieldPostfix(String value) {
        return FIELD_POSTFIX_HELPER.getValidName(value, StringUtils.EMPTY);
    }

    /**
     * Creates a valid simplified name-like string value (without non-letter characters)
     * @param value String used as the source for the simplified name
     * @return String value
     */
    public static String getValidPlainName(String value) {
        return PLAIN_NAME_HELPER.getValidName(value, StringUtils.EMPTY);
    }

    /**
     * Creates a valid node name rendition for use with XML/JCR. If a name cannot be created from the provided string,
     * the default standard "item" value is returned
     * @param value String used as the source for the name
     * @return String value
     */
    public static String getValidNodeName(String value) {
        return NODE_NAME_HELPER.getValidName(value, DialogConstants.NN_ITEM);
    }

    /**
     * Creates a valid node name rendition for use with XML/JCR. If a name cannot be created from the provided string,
     * the provided {@code defaultValue} is returned
     * @param value        String used as the source for the name
     * @param defaultValue Fallback string
     * @return String value
     */
    public static String getValidNodeName(String value, String defaultValue) {
        return NODE_NAME_HELPER.getValidName(value, defaultValue);
    }

    /**
     * Creates a valid and unique node name rendition for use with XML/JCR taking into account the existing sibling
     * nodes situated within the same parent context
     * @param value        String used as the source for the name
     * @param defaultValue Fallback string
     * @param context      {@code Target} object representing the sibling nodes container
     * @return String value
     */
    public static String getUniqueName(String value, String defaultValue, Target context) {
        return NODE_NAME_HELPER.getUniqueName(value, defaultValue, context);
    }

    /**
     * Creates a valid and unique node name rendition for use with XML/JCR taking into account the existing sibling
     * nodes situated within the same parent context
     * @param value        String used as the source for the name
     * @param defaultValue Fallback string
     * @param context      {@code Element} object representing the sibling nodes container
     * @return String value
     */
    public static String getUniqueName(String value, String defaultValue, Element context) {
        return NODE_NAME_HELPER.getUniqueName(value, defaultValue, context);
    }

    /**
     * Retrieves the member name value with the Java getter prefix stripped
     * @param value String used as the source
     * @return String value
     */
    public static String stripGetterPrefix(String value) {
        if (StringUtils.startsWith(value, PREFIX_GET)) {
            return Character.toLowerCase(value.charAt(3)) + value.substring(4);
        }
        return value;
    }
}
