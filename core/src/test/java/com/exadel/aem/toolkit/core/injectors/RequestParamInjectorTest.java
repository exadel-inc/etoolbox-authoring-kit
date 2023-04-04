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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections4.IterableUtils;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.injectors.models.RequestAdapterBase;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.RequestParams;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

public class RequestParamInjectorTest extends RequestPropertyInjectorTestBase {

    /* -----------
       Preparation
       ----------- */

    @Override
    BaseInjector<?> prepareInjector() {
        return new RequestParamInjector();
    }

    @Override
    void prepareRequest(MockSlingHttpServletRequest request, Object payload) {
        request.setParameterMap(Collections.emptyMap());
        if (payload == null) {
            return;
        }
        if (payload.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(payload); i++) {
                request.addRequestParameter(ATTRIBUTE_VALUE, Array.get(payload, i).toString());
            }
        } else if (TypeUtil.isSupportedCollection(payload.getClass())) {
            for (Object next : (Collection<?>) payload) {
                request.addRequestParameter(ATTRIBUTE_VALUE, next.toString());
            }
        } else {
            request.addRequestParameter(ATTRIBUTE_VALUE, payload.toString());
        }
    }

    /* -----
       Tests
       ----- */

    @Test
    public void shouldInjectString() {
        super.shouldInjectString(model -> assertTrue(model.getObjectValue() instanceof RequestParameter));
    }

    @Test
    public void shouldInjectStringArray() {
        super.shouldInjectStringArray((model, payload) -> {
            assertTrue(model.getObjectValue() instanceof RequestParameter);
            assertEquals(extractFirstElement(payload), ((RequestParameter) model.getObjectValue()).getString());
        });
    }

    @Test
    public void shouldInjectStringCollection() {
        super.shouldInjectStringCollection((model, payload) -> {
            assertTrue(model.getObjectValue() instanceof RequestParameter);
            assertEquals(extractFirstElement(payload), ((RequestParameter) model.getObjectValue()).getString());
        });
    }

    @Test
    public void shouldInjectInteger() {
        super.shouldInjectInteger(RequestParamInjectorTest::assertObjectValueEquals);
    }

    @Test
    public void shouldInjectIntegerArray() {
        super.shouldInjectIntegerCollection(RequestParamInjectorTest::assertArrayOrCollectionValueEquals);
    }

    @Test
    public void shouldInjectIntegerCollection() {
        super.shouldInjectIntegerCollection(RequestParamInjectorTest::assertArrayOrCollectionValueEquals);
    }

    @Test
    public void shouldInjectLong() {
        super.shouldInjectLong(RequestParamInjectorTest::assertObjectValueEquals);
    }

    @Test
    public void shouldInjectLongArray() {
        super.shouldInjectLongArray(RequestParamInjectorTest::assertArrayOrCollectionValueEquals);
    }

    @Test
    public void shouldInjectLongCollection() {
        super.shouldInjectLongCollection(RequestParamInjectorTest::assertArrayOrCollectionValueEquals);
    }

    @Test
    public void shouldInjectDouble() {
        super.shouldInjectDouble(RequestParamInjectorTest::assertObjectValueEquals);
    }

    @Test
    public void shouldInjectDoubleArray() {
        super.shouldInjectDoubleArray(RequestParamInjectorTest::assertArrayOrCollectionValueEquals);
    }

    @Test
    public void shouldInjectDoubleCollection() {
        super.shouldInjectDoubleCollection(RequestParamInjectorTest::assertArrayOrCollectionValueEquals);
    }

    @Test
    public void shouldInjectBoolean() {
        super.shouldInjectBoolean(RequestParamInjectorTest::assertObjectValueEquals);
    }

    @Test
    public void shouldInjectBooleanArray() {
        super.shouldInjectBooleanArray(RequestParamInjectorTest::assertArrayOrCollectionValueEquals);
    }

    @Test
    public void shouldInjectBooleanCollection() {
        super.shouldInjectBooleanCollection(RequestParamInjectorTest::assertArrayOrCollectionValueEquals);
    }

    @Test
    public void shouldInjectRequestParameterObjects() {
        context.request().setParameterMap(Collections.emptyMap());
        context.request().addRequestParameter(ATTRIBUTE_VALUE, EXPECTED_STRING_ARRAY[0]);
        context.request().addRequestParameter(ATTRIBUTE_VALUE, EXPECTED_STRING_ARRAY[1]);
        RequestParams model = context.request().adaptTo(RequestParams.class);
        assertNotNull(model);

        assertNotNull(model.getValue());
        assertEquals(EXPECTED_STRING_ARRAY[0], model.getValue().getString());

        assertEquals(EXPECTED_STRING_ARRAY.length,  model.getConstructorValue().length);
        assertEquals(EXPECTED_STRING_ARRAY[0], model.getConstructorValue()[0].getString());

        assertNotNull(model.getCollectionValue());
        assertEquals(EXPECTED_STRING_ARRAY.length, model.getCollectionValue().size());
        assertEquals(EXPECTED_STRING_ARRAY[0], model.getCollectionValue().get(0).getString());

        assertNotNull(model.getValueSupplier().getValue());
        RequestParameterMap requestParametersViaInterface = model.getValueSupplier().getValue();
        assertEquals(EXPECTED_STRING_ARRAY.length,  requestParametersViaInterface.get(ATTRIBUTE_VALUE).length);
    }

    @Test
    public void shouldNotCauseExceptionWhenPayloadMissing() {
        super.shouldNotCauseExceptionWhenPayloadMissing();
    }

    /* ---------------
       Service methods
       --------------- */

    private static Object extractFirstElement(Object arrayOrCollection) {
        if (arrayOrCollection.getClass().isArray() && Array.getLength(arrayOrCollection) > 0) {
            return Array.get(arrayOrCollection, 0);
        }
        if (TypeUtil.isSupportedCollection(arrayOrCollection.getClass())
            && !IterableUtils.isEmpty((Collection<?>) arrayOrCollection)) {
            return IterableUtils.get((Collection<?>) arrayOrCollection, 0);
        }
        return arrayOrCollection;
    }

    private static void assertObjectValueEquals(RequestAdapterBase<?> model, Object payload) {
        assertTrue(model.getObjectValue() instanceof RequestParameter);
        assertEquals(String.valueOf(payload), ((RequestParameter) model.getObjectValue()).getString());
    }

    private static void assertArrayOrCollectionValueEquals(RequestAdapterBase<?> model, Object payload) {
        assertTrue(model.getObjectValue() instanceof RequestParameter);
        assertEquals(String.valueOf(extractFirstElement(payload)), ((RequestParameter) model.getObjectValue()).getString());
    }
}
