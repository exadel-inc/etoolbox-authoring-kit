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
package com.exadel.aem.toolkit.api.annotations.meta;

import org.junit.Assert;
import org.junit.Test;

public class StringTransformationTest {

    private static final String SAMPLE1 = "L0rem IPSum! Do1or Sit Amet";
    private static final String SAMPLE2 = "Lorem IPSum  dolor-sit-Amet";
    private static final String SAMPLE3 = "Lorem_IPSum__dolor_Sit_Amet";

    @Test
    public void shouldConvertToLowerCase() {
        Assert.assertEquals("l0rem ipsum! do1or sit amet", StringTransformation.LOWERCASE.apply(SAMPLE1));
    }

    @Test
    public void shouldConvertToUpperCase() {
        Assert.assertEquals("L0REM IPSUM! DO1OR SIT AMET", StringTransformation.UPPERCASE.apply(SAMPLE1));
    }

    @Test
    public void shouldConvertToCamelCase() {
        Assert.assertEquals("l0remIpsum!Do1orSitAmet", StringTransformation.CAMELCASE.apply(SAMPLE1));
        Assert.assertEquals("loremIpsumDolorSitAmet", StringTransformation.CAMELCASE.apply(SAMPLE2));
        Assert.assertEquals("loremIpsumDolorSitAmet", StringTransformation.CAMELCASE.apply(SAMPLE3));
    }

    @Test
    public void shouldCapitalize() {
        Assert.assertEquals("L0rem Ipsum! Do1or Sit Amet", StringTransformation.CAPITALIZE.apply(SAMPLE1));
        Assert.assertEquals("Lorem Ipsum Dolor Sit Amet", StringTransformation.CAPITALIZE.apply(SAMPLE2));
        Assert.assertEquals("Lorem Ipsum Dolor Sit Amet", StringTransformation.CAPITALIZE.apply(SAMPLE3));
    }
}
