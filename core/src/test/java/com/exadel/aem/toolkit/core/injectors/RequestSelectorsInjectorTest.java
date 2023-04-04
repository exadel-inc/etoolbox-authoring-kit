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
package com.exadel.aem.toolkit.core.injectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.models.RequestAdapterBase;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

public class RequestSelectorsInjectorTest extends RequestPropertyInjectorTestBase {

    private static final String ARRAY_BRACKETS = "[{}]";
    private static final String SEPARATOR_COMMA_SPACE = ", ";
    private static final String FRACTIONAL_PART_PATTERN = "\\.\\d+";

    /* -----------
       Preparation
       ----------- */

    @Override
    BaseInjector<?> prepareInjector() {
        return new RequestSelectorsInjector();
    }

    @Override
    void prepareRequest(MockSlingHttpServletRequest request, Object payload) {
        MockRequestPathInfo requestPathInfo = (MockRequestPathInfo) request.getRequestPathInfo();
        requestPathInfo.setSelectorString(toSelectorString(payload));
    }

    /* -----
       Tests
       ----- */

    @Test
    public void shouldInjectString() {
        super.shouldInjectString();
    }

    @Test
    public void shouldInjectStringArray() {
        super.shouldInjectStringArray(RequestSelectorsInjectorTest::assertIterablesEqual);
    }

    @Test
    public void shouldInjectStringCollection() {
        super.shouldInjectStringCollection(RequestSelectorsInjectorTest::assertIterablesEqual);
    }

    @Test
    public void shouldInjectInteger() {
        super.shouldInjectInteger(RequestSelectorsInjectorTest::assertStringifiedValuesEqual);
    }

    @Test
    public void shouldInjectIntegerArray() {
        super.shouldInjectIntegerArray(RequestSelectorsInjectorTest::assertIterablesEqual);
    }

    @Test
    public void shouldInjectIntegerCollection() {
        super.shouldInjectIntegerCollection(RequestSelectorsInjectorTest::assertIterablesEqual);
    }

    @Test
    public void shouldInjectLong() {
        super.shouldInjectLong(RequestSelectorsInjectorTest::assertStringifiedValuesEqual);
    }

    @Test
    public void shouldInjectLongArray() {
        super.shouldInjectLongArray(RequestSelectorsInjectorTest::assertIterablesEqual);
    }

    @Test
    public void shouldInjectLongCollection() {
        super.shouldInjectLongCollection(RequestSelectorsInjectorTest::assertIterablesEqual);
    }

    @Test
    public void shouldInjectBoolean() {
        super.shouldInjectBoolean(RequestSelectorsInjectorTest::assertStringifiedValuesEqual);
    }

    @Test
    public void shouldInjectBooleanArray() {
        super.shouldInjectBooleanArray(RequestSelectorsInjectorTest::assertIterablesEqual);
    }

    @Test
    public void shouldInjectBooleanCollection() {
        super.shouldInjectBooleanCollection(RequestSelectorsInjectorTest::assertIterablesEqual);
    }

    @Test
    public void shouldNotCauseExceptionWhenPayloadMissing() {
        super.shouldNotCauseExceptionWhenPayloadMissing();
    }

    /* ---------------
       Service methods
       --------------- */

    private static void assertIterablesEqual(RequestAdapterBase<?> model, Object payload) {
        assertNotNull(model.getObjectValue());
        assertEquals(toInlineIteration(payload), toInlineIteration(model.getObjectValue()));
    }

    private static void assertStringifiedValuesEqual(RequestAdapterBase<?> model, Object payload) {
        assertEquals(
            String.valueOf(model.getObjectValue()),
            String.valueOf(payload).replaceAll(FRACTIONAL_PART_PATTERN, StringUtils.EMPTY));
    }

    private static String toSelectorString(Object payload) {
        return toInlineIteration(payload).replace(CoreConstants.SEPARATOR_COMMA, CoreConstants.SEPARATOR_DOT);
    }

    private static String toInlineIteration(Object value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        String result = value.toString();
        if (value.getClass().isArray()) {
            result =  ArrayUtils.toString(value);
        } else if (TypeUtil.isSupportedCollection(value.getClass())) {
            result = StringUtils.join((Iterable<?>) value, CoreConstants.SEPARATOR_COMMA);
        }
        return StringUtils.strip(result, ARRAY_BRACKETS)
            .replace(SEPARATOR_COMMA_SPACE, CoreConstants.SEPARATOR_COMMA)
            .replaceAll(FRACTIONAL_PART_PATTERN, StringUtils.EMPTY);

    }
}
