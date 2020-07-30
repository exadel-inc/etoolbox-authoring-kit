package com.exadel.aem.toolkit.samples.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ListUtils {

    public static final String COMMA_SPACE_DELIMITER = ", ";

    private ListUtils() {}

    public static String joinNonBlank(String delimiter, String... args) {
        return Arrays.stream(args)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(delimiter));
    }
}
