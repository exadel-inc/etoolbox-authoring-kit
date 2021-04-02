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
package com.exadel.aem.toolkit.test.custom;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;

@SuppressWarnings("unused")
public class CustomValidator implements Validator {
    private static final Pattern COLOR_PATTERN = Pattern.compile("^(red|green|blue)$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean test(Object value) {
        return isApplicableTo(value) && (StringUtils.isEmpty(value.toString()) || COLOR_PATTERN.matcher(value.toString()).matches());
    }

    @Override
    public boolean isApplicableTo(Object value) {
        return value instanceof String;
    }

    @Override
    public String getWarningMessage() {
        return "one of 'red', 'green', or 'blue' must be provided";
    }
}
