package com.exadel.aem.toolkit.api.annotations.assets.validators;

public class ValidatorConstants {

    private ValidatorConstants() { }

    public static final String GRANITE_DATA_PREFIX = "granite:data/";

    public static final String MAX_ITEMS = "maxitems";
    public static final String MAX_ITEMS_VALUE = "validator-maxitems";
    public static final String MAX_ITEMS_MSG = "validator-maxitems-msg";
    public static final String MAX_ITEMS_DEFAULT_MSG = "A maximum of {1} items is required here.";

    public static final String MIN_ITEMS = "minitems";
    public static final String MIN_ITEMS_VALUE = "validator-minitems";
    public static final String MIN_ITEMS_MSG = "validator-minitems-msg";
    public static final String MIN_ITEMS_DEFAULT_MSG = "A minimum of {1} items is required here.";

    public static final String REGEX = "regex";
    public static final String REGEX_VALUE = "validator-regex";
    public static final String REGEX_MSG = "validator-regex-msg";
    public static final String REGEX_DEFAULT_MSG = "Value is not valid.";

}
