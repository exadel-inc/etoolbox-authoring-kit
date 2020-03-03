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

package com.exadel.aem.toolkit.core.util;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import com.google.common.base.CaseFormat;

/**
 * Helper class for creating standard compliant names for XML entities designed to work together
 * with a {@link com.exadel.aem.toolkit.api.runtime.XmlUtility} implementation
 */
class XmlNamingHelper {
    private static final String VERB_SEPARATOR = "_";
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern NODE_NAME_INDEX_PATTERN = Pattern.compile("\\d*$");

    private static final Pattern INVALID_FIELD_NAME_PATTERN = Pattern.compile("^\\W+|[^\\w-/]$|[^\\w-/:]+");
    private static final Pattern INVALID_NODE_NAME_PATTERN = Pattern.compile("\\W+");
    private static final Pattern INVALID_NAMESPACE_NODE_NAME_PATTERN = Pattern.compile("^\\W*:|\\W+:$|[^\\w:]+");

    private PluginXmlUtility xmlUtil;
    private boolean lowercaseFirst;
    private Pattern clearingPattern;

    /**
     * {@code XmlNamingHelper} constructor. Stores reference to {@link PluginXmlUtility} object
     * @param xmlUtility {@code PluginXmlUtility} instance
     */
    private XmlNamingHelper(PluginXmlUtility xmlUtility) {
        this.xmlUtil = xmlUtility;
    }

    /**
     * Checks whether the given string argument is compliant to XML entity naming rules and either returns it as is or
     * transforms to become compliant. Provided default is used to substitute a non-alphanumeric string or prepend a
     * string that not starts with a letter
     * @param source String value to test and optionally transform
     * @param defaultValue String value to be used as a default or a valid prefix
     * @return String value
     */
    String getValidName(String source, String defaultValue) {
        if (StringUtils.isBlank(source)) {
            return StringUtils.defaultIfBlank(defaultValue, DialogConstants.NN_ITEM);
        }

        String result = source.trim();
        boolean convertToCamelCase = WHITESPACE_PATTERN.matcher(source).find();

        if (convertToCamelCase) {
            result = WHITESPACE_PATTERN.matcher(result).replaceAll(VERB_SEPARATOR);
        }
        result = clearingPattern.matcher(result).replaceAll(StringUtils.EMPTY);

        if (convertToCamelCase) {
            result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, result);
        }

        if (result.isEmpty() || !Character.isAlphabetic(result.codePointAt(0))) {
            result = StringUtils.defaultString(defaultValue) +  result;
        }
        if (lowercaseFirst && !result.chars().allMatch(Character::isUpperCase)) {
            return StringUtils.uncapitalize(result);
        }
        return result;
    }

    /**
     * Creates an XML compliant node name that is unique among children of  a specified XNL element by optionally adding
     * an incremental number index to the name
     * @param source String value to test and optionally transform
     * @param defaultValue String value to be used as a default or a valid prefix
     * @param context {@code Element} instance to search for existing child nodes
     * @return String value
     */
    String getUniqueName(String source, String defaultValue, Element context) {
        String result = getValidName(source, defaultValue);
        if (context == null) {
            return result;
        }
        int index = 1;
        while (xmlUtil.getChildElement(context, result) != null) {
            result = NODE_NAME_INDEX_PATTERN.matcher(result).replaceFirst(String.valueOf(index++));
        }
        return result;
    }

    /**
     * Creates and initializes an instance of {@link XmlNamingHelper} to deal with field names
     * @param xmlUtility {@code PluginXmlUtility} instance
     * @return {@code XmlNamingHelper} object
     */
    static XmlNamingHelper forFieldName(PluginXmlUtility xmlUtility) {
        XmlNamingHelper helper = new XmlNamingHelper(xmlUtility);
        helper.lowercaseFirst = false;
        helper.clearingPattern = INVALID_FIELD_NAME_PATTERN;
        return helper;
    }

    /**
     * Creates and initializes an instance of {@link XmlNamingHelper} to deal with simple (non-namespaced) node names
     * and attribute names
     * @param xmlUtility {@code PluginXmlUtility} instance
     * @return {@code XmlNamingHelper} object
     */
    static XmlNamingHelper forSimpleName(PluginXmlUtility xmlUtility) {
        XmlNamingHelper helper = new XmlNamingHelper(xmlUtility);
        helper.lowercaseFirst = true;
        helper.clearingPattern = INVALID_NODE_NAME_PATTERN;
        return helper;
    }

    /**
     * Creates and initializes an instance of {@link XmlNamingHelper} to deal with fully qualified (namespaced) node
     * names and attribute names
     * @param xmlUtility {@code PluginXmlUtility} instance
     * @return {@code XmlNamingHelper} object
     */
    static XmlNamingHelper forNamespaceAndName(PluginXmlUtility xmlUtility) {
        XmlNamingHelper helper = new XmlNamingHelper(xmlUtility);
        helper.lowercaseFirst = true;
        helper.clearingPattern = INVALID_NAMESPACE_NODE_NAME_PATTERN;
        return helper;
    }
}
