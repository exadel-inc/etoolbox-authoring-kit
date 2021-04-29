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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import com.google.common.base.CaseFormat;

import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Helper class for creating standard-compliant names for XML entities designed to work together
 * with {@link NamingUtil}
 */
class NamingHelper {
    private static final String VERB_SEPARATOR = "_";
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern NODE_NAME_INDEX_PATTERN = Pattern.compile("\\d*$");
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^\\w+:");

    private static final Pattern INVALID_FIELD_NAME_PATTERN = Pattern.compile("^\\W+|[^\\w-/]$|[^\\w-/:]+");
    private static final Pattern INVALID_FIELD_NAME_POSTFIX_PATTERN = Pattern.compile("^[^\\w-/]+|[^\\w-/]$|[^\\w-/:]+");
    private static final Pattern INVALID_NODE_NAME_NS_PATTERN = Pattern.compile("^\\W*:|\\W+:$|[^\\w:]+");
    private static final Pattern INVALID_PLAIN_NAME_PATTERN = Pattern.compile("\\W+");

    private static final Pattern PARENT_PATH_PREFIX_PATTERN = Pattern.compile("^(?:\\.\\./)+");

    private boolean lowercaseFirst;
    private boolean preserveParentPath;
    private boolean allowSoloParentPath;
    private boolean removeInvalidNamespace;
    private Pattern clearingPattern;

    /**
     * Default (instantiation-restricting) constructor
     */
    private NamingHelper() {
    }

    /* -----------------
       Interface methods
       ----------------- */

    /**
     * Checks whether the given string argument is compliant with XML entity naming rules and either returns it as is or
     * transforms to become compliant. Provided default is used to substitute a non-alphanumeric string or prepend a
     * string that does not start with a letter
     * @param source       String value to test and optionally transform
     * @param defaultValue String value to be used as a default or a valid prefix
     * @return String value
     */
    String getValidName(String source, String defaultValue) {
        if (StringUtils.isBlank(source)) {
            return StringUtils.defaultIfBlank(defaultValue, DialogConstants.NN_ITEM);
        }

        String result = source.trim();
        Matcher parentPathPrefixMatcher = preserveParentPath
                ? PARENT_PATH_PREFIX_PATTERN.matcher(result)
                : null;
        String parentPathPrefix = parentPathPrefixMatcher != null && parentPathPrefixMatcher.find()
                ? parentPathPrefixMatcher.group()
                : StringUtils.EMPTY;

        Matcher whitespacePatternMatcher = WHITESPACE_PATTERN.matcher(result);
        boolean convertToCamelCase = whitespacePatternMatcher.find();

        if (convertToCamelCase) {
            result = WHITESPACE_PATTERN.matcher(result).replaceAll(VERB_SEPARATOR);
        }

        result = clearingPattern.matcher(result).replaceAll(StringUtils.EMPTY);

        if (convertToCamelCase) {
            result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, result);
        }

        boolean needsDefaultValue = result.isEmpty() || !Character.isAlphabetic(result.codePointAt(0));
        boolean canDoWithoutDefaultValue = allowSoloParentPath && !parentPathPrefix.isEmpty();
        if (needsDefaultValue && !canDoWithoutDefaultValue) {
            result = StringUtils.defaultString(defaultValue) +  result;
        }

        if (removeInvalidNamespace) {
            result = removeInvalidNamespaces(result);
        }

        if (lowercaseFirst && !result.chars().allMatch(Character::isUpperCase)) {
            result = StringUtils.uncapitalize(result);
        }

        if (StringUtils.isNotEmpty(parentPathPrefix)) {
            return parentPathPrefix + result;
        }
        return result;
    }

    /**
     * Creates an XML compliant node name that is unique among children of a specified entity by optionally adding
     * an incremental number index to the name
     * @param source       String value to test and optionally transform
     * @param defaultValue String value to be used as a default or a valid prefix
     * @param context      {@code Target} instance to search for existing child nodes
     * @return String value
     */
    String getUniqueName(String source, String defaultValue, Target context) {
        String result = getValidName(source, defaultValue);
        if (context == null) {
            return result;
        }
        int index = 1;
        while (context.exists(result)) {
            result = NODE_NAME_INDEX_PATTERN.matcher(result).replaceFirst(String.valueOf(index++));
        }
        return result;
    }

    /**
     * Creates an XML compliant node name that is unique among children of a specified entity by optionally adding
     * an incremental number index to the name
     * @param source       String value to test and optionally transform
     * @param defaultValue String value to be used as a default or a valid prefix
     * @param context      {@code Element} instance to search for existing child nodes
     * @return String value
     */
    String getUniqueName(String source, String defaultValue, Element context) {
        String result = getValidName(source, defaultValue);
        if (context == null) {
            return result;
        }
        int index = 1;
        while (PluginRuntime.context().getXmlUtility().getChildElement(context, result) != null) {
            result = NODE_NAME_INDEX_PATTERN.matcher(result).replaceFirst(String.valueOf(index++));
        }
        return result;
    }

    /**
     * Called by {@link NamingHelper#getValidName(String, String)} to remove namespace sign ("{@code :}") from a name
     * source when a namespace prefix does not correspond to any registered XML namespace
     * @param value Name source
     * @return Same value if no invalid namespace detected, or a transformed string
     */
    private static String removeInvalidNamespaces(String value) {
        Matcher namespaceMatcher = NAMESPACE_PATTERN.matcher(value);
        String namespaceCapture = namespaceMatcher.find()
            ? namespaceMatcher.group().substring(0, namespaceMatcher.end() - 1)
            : null;
        if (namespaceCapture != null && !XmlFactory.XML_NAMESPACES.containsKey(namespaceCapture)) {
            return value.replace(namespaceMatcher.group(), namespaceCapture);
        }
        return value;
    }

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Creates and initializes an instance of {@link NamingHelper} to deal with regular field names and name prefixes
     * @return {@code NamingHelper} object
     */
    static NamingHelper forFieldName() {
        NamingHelper helper = new NamingHelper();
        helper.preserveParentPath = true;
        helper.clearingPattern = INVALID_FIELD_NAME_PATTERN;
        return helper;
    }

    /**
     * Creates and initializes an instance of {@link NamingHelper} to deal with field name postfix parts
     * @return {@code NamingHelper} object
     */
    static NamingHelper forFieldNamePrefix() {
        NamingHelper helper = new NamingHelper();
        helper.preserveParentPath = true;
        helper.allowSoloParentPath = true;
        helper.clearingPattern = INVALID_FIELD_NAME_PATTERN;
        return helper;
    }

    /**
     * Creates and initializes an instance of {@link NamingHelper} to deal with field name postfix parts
     * @return {@code NamingHelper} object
     */
    static NamingHelper forFieldNamePostfix() {
        NamingHelper helper = new NamingHelper();
        helper.clearingPattern = INVALID_FIELD_NAME_POSTFIX_PATTERN;
        return helper;
    }

    /**
     * Creates and initializes an instance of {@link NamingHelper} to deal with fully qualified (namespaced) node
     * names and attribute names
     * @return {@code NamingHelper} object
     */
    static NamingHelper forNodeName() {
        NamingHelper helper = new NamingHelper();
        helper.lowercaseFirst = true;
        helper.clearingPattern = INVALID_NODE_NAME_NS_PATTERN;
        helper.removeInvalidNamespace = true;
        return helper;
    }

    /**
     * Creates and initializes an instance of {@link NamingHelper} to deal with plan names that contain only alphanumeric
     * characters
     * @return {@code NamingHelper} object
     */
    static NamingHelper forPlainName() {
        NamingHelper helper = new NamingHelper();
        helper.clearingPattern = INVALID_PLAIN_NAME_PATTERN;
        return helper;
    }
}
