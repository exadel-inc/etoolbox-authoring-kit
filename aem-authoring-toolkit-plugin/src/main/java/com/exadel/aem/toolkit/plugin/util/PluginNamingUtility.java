package com.exadel.aem.toolkit.plugin.util;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.handlers.Target;

public class PluginNamingUtility {
    private static final String PREFIX_GET = "get";

    private static final NamingHelper FIELD_HELPER = NamingHelper.forFieldName();
    private static final NamingHelper NODE_NAME_HELPER = NamingHelper.forNodeName();

    public static String getValidName(String name) {
        return NODE_NAME_HELPER.getValidName(name, DialogConstants.NN_ITEM);
    }

    public static String getValidName(String name, String defaultName) {
        return NODE_NAME_HELPER.getValidName(name, defaultName);
    }

    public static String getValidFieldName(String name) {
        return FIELD_HELPER.getValidName(name, DialogConstants.NN_FIELD);
    }

    public static String getUniqueName(String name, String defaultValue, Target context) {
        return NODE_NAME_HELPER.getUniqueName(name, defaultValue, context);
    }

    public static String getUniqueName(String name, String defaultValue, Element context) {
        return NODE_NAME_HELPER.getUniqueName(name, defaultValue, context);
    }

    public static String stripGetterPrefix(String name) {
        if (StringUtils.startsWith(name, PREFIX_GET)) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        return name;
    }

    private PluginNamingUtility() {}
}
