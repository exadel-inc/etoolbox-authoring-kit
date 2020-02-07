package com.exadel.aem.toolkit.test.custom;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;

@SuppressWarnings("unused")
public class CustomValidator implements Validator {
    private static final Pattern COLOR_PATTERN = Pattern.compile("^(red|green|blue)$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean test(Object obj) {
        return isApplicableTo(obj) && (StringUtils.isEmpty(obj.toString()) || COLOR_PATTERN.matcher(obj.toString()).matches());
    }

    @Override
    public boolean isApplicableTo(Object obj) {
        return obj instanceof String;
    }

    @Override
    public String getWarningMessage() {
        return "one of 'red', 'green', or 'blue' must be provided";
    }
}
