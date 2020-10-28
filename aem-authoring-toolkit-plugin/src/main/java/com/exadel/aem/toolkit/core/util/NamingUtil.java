package com.exadel.aem.toolkit.core.util;

import com.exadel.aem.toolkit.api.handlers.TargetFacade;

public class NamingUtil {


    private static final NamingHelper fieldNameHelper = NamingHelper.forFieldName();
    private static final NamingHelper simpleNameHelper = NamingHelper.forSimpleName();
    private static final NamingHelper namespaceNameHelper = NamingHelper.forNamespaceAndName();

    public static String getValidName(String name) {
        return namespaceNameHelper.getValidName(name, DialogConstants.NN_ITEM);
    }

    public static String getValidSimpleName(String name) {
        return simpleNameHelper.getValidName(name, DialogConstants.NN_ITEM);
    }

    public static String getValidFieldName(String name) {
        return fieldNameHelper.getValidName(name, DialogConstants.NN_FIELD);
    }

    public static String getUniqueName(String name, String defaultValue, TargetFacade context) {
        return simpleNameHelper.getUniqueName(name, defaultValue, context);
    }

    private NamingUtil() {}
}
