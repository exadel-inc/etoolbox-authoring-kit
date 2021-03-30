package com.exadel.aem.toolkit.plugin.utils;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.handlers.Target;

public class NamingUtil {
    private static final String PREFIX_GET = "get";

    private static final NamingHelper FIELD_HELPER = NamingHelper.forFieldName();
    private static final NamingHelper FIELD_PREFIX_HELPER = NamingHelper.forFieldNamePrefix();
    private static final NamingHelper FIELD_POSTFIX_HELPER = NamingHelper.forFieldNamePostfix();
    private static final NamingHelper NODE_NAME_HELPER = NamingHelper.forNodeName();
    private static final NamingHelper PLAIN_NAME_HELPER = NamingHelper.forPlainName();

    private NamingUtil() {}

    public static String getValidFieldName(String value) {
        return FIELD_HELPER.getValidName(value, DialogConstants.NN_FIELD);
    }

    public static String getValidFieldPrefix(String value) {
        return FIELD_PREFIX_HELPER.getValidName(value, StringUtils.EMPTY);
    }

    public static String getValidFieldPostfix(String value) {
        return FIELD_POSTFIX_HELPER.getValidName(value, StringUtils.EMPTY);
    }

    public static String getValidPlainName(String value) {
        return PLAIN_NAME_HELPER.getValidName(value, StringUtils.EMPTY);
    }

    public static String getValidNodeName(String value) {
        return NODE_NAME_HELPER.getValidName(value, DialogConstants.NN_ITEM);
    }

    public static String getValidNodeName(String value, String defaultValue) {
        return NODE_NAME_HELPER.getValidName(value, defaultValue);
    }

    public static String getUniqueName(String value, String defaultValue, Target context) {
        return NODE_NAME_HELPER.getUniqueName(value, defaultValue, context);
    }

    public static String getUniqueName(String value, String defaultValue, Element context) {
        return NODE_NAME_HELPER.getUniqueName(value, defaultValue, context);
    }

    public static String stripGetterPrefix(String value) {
        if (StringUtils.startsWith(value, PREFIX_GET)) {
            return Character.toLowerCase(value.charAt(3)) + value.substring(4);
        }
        return value;
    }
}
