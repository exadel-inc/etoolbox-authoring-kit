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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Before;
import org.junit.Rule;
import org.slf4j.LoggerFactory;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.models.RequestAdapterBase;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.BooleanArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.BooleanCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.Booleans;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.DoubleArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.DoubleCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.Doubles;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.IntegerArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.IntegerCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.Integers;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.LongArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.LongCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.Longs;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.StringArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.StringCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.Strings;

abstract class RequestPropertyInjectorTestBase {

    private static final List<Class<? extends RequestAdapterBase<?>>> GENERIC_TEST_CASES = Arrays.asList(
        Strings.class, StringArrays.class, StringCollections.class,
        Integers.class, IntegerArrays.class, IntegerCollections.class,
        Longs.class, LongArrays.class, LongCollections.class,
        Doubles.class, DoubleArrays.class, DoubleCollections.class,
        Booleans.class, BooleanArrays.class, BooleanCollections.class);

    private static final String MODELS_PACKAGE_NAME = CoreConstants.ROOT_PACKAGE + ".core.injectors.models.requestproperty";

    private static final String EXPECTED_STRING = "Hello World";
    private static final String EXPECTED_DEFAULT_STRING = "default";
    static final String[] EXPECTED_STRING_ARRAY = {"Hello", "World"};
    private static final List<String> EXPECTED_STRING_LIST = Arrays.asList(EXPECTED_STRING_ARRAY);

    private static final byte EXPECTED_BYTE = 42;
    private static final byte[] EXPECTED_BYTE_ARRAY = {42, 43, 44};

    private static final List<? extends Serializable> HALF_PARSEABLE_INTEGERS = Arrays.asList("foo", 42);

    private static final Integer EXPECTED_INTEGER = 42;
    private static final Integer EXPECTED_DEFAULT_INTEGER = 10;
    private static final Integer[] EXPECTED_INTEGER_ARRAY = {42, 43, 44};
    private static final List<Integer> EXPECTED_INTEGER_LIST = Arrays.asList(42, 43, 44);
    private static final List<Integer> EXPECTED_HALF_PARSED_INTEGER_LIST = Collections.singletonList(42);
    private static final String EXPECTED_INTEGER_ARRAY_STRING = "42,43,44";

    private static final long EXPECTED_LONG = 42L;
    private static final long EXPECTED_DEFAULT_LONG = 10L;
    private static final Long[] EXPECTED_LONG_ARRAY = {42L, 43L, 44L};
    private static final List<Long> EXPECTED_LONG_LIST = Arrays.asList(42L, 43L, 44L);
    private static final List<Long> EXPECTED_HALF_PARSED_LONG_LIST = Collections.singletonList(42L);

    private static final float EXPECTED_FLOAT = 42.1f;
    private static final String EXPECTED_FLOAT_STRING = "42.1f";

    private static final List<? extends Serializable> HALF_PARSEABLE_FLOATING_POINT_VALUES = Arrays.asList("foo", 42.1f, 43.1d, "44.1d");

    private static final double EXPECTED_DOUBLE = 42.1d;
    private static final double EXPECTED_DEFAULT_DOUBLE = 1.1d;
    private static final Double[] EXPECTED_DOUBLE_ARRAY = {42.1d, 43.1d, 44.1d};
    static final List<Double> EXPECTED_DOUBLE_LIST = Arrays.asList(42.1d, 43.1d, 44.1d);
    static final List<Double> EXPECTED_HALF_PARSED_DOUBLE_LIST = Arrays.asList(42.1d, 43.1d, 44.1d);
    private static final String EXPECTED_DOUBLE_ARRAY_STRING = "42.1,43.1,44.1";

    private static final Boolean[] EXPECTED_BOOLEAN_ARRAY = {true, true, false};
    private static final List<Boolean> EXPECTED_BOOLEAN_LIST = Arrays.asList(true, true, false);
    private static final String EXPECTED_BOOLEAN_ARRAY_STRING = "true,true,false";

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    /* -----------
       Preparation
       ----------- */

    @Before
    public void beforeTest() {
        LoggerFactory.getLogger(getClass()).info("Running test: {}", getClass().getSimpleName());
        context.registerInjectActivateService(new DelegateInjector(prepareInjector()));
        context.addModelsForPackage(MODELS_PACKAGE_NAME);
    }

    abstract BaseInjector<?> prepareInjector();

    abstract void prepareRequest(MockSlingHttpServletRequest request, Object payload);

    /* -----
       Tests
       ----- */

    // region Strings
    void shouldInjectString() {
        shouldInjectString(model -> assertEquals(EXPECTED_STRING, model.getObjectValue()));
    }

    void shouldInjectString(Consumer<RequestAdapterBase<?>> objectValueChecker) {
        prepareRequest(context.request(), EXPECTED_STRING);
        Strings model = context.request().adaptTo(Strings.class);
        assertNotNull(model);
        assertEquals(EXPECTED_STRING, model.getValue());
        objectValueChecker.accept(model);
        assertEquals(EXPECTED_STRING, model.getConstructorValue());
        assertEquals(EXPECTED_STRING, model.getValueSupplier().getValue());
    }

    void shouldInjectDefaultString() {
        Strings model = context.request().adaptTo(Strings.class);
        assertNotNull(model);
        assertEquals(EXPECTED_DEFAULT_STRING, model.getDefaultValue());
    }

    void shouldInjectStringArray() {
        shouldInjectStringArray(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectStringArray(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        for (Object payload : Arrays.asList(
            EXPECTED_STRING_ARRAY,
            toObjectArray(EXPECTED_STRING_ARRAY),
            EXPECTED_STRING_LIST)) {

            prepareRequest(context.request(), payload);
            StringArrays model = context.request().adaptTo(StringArrays.class);
            assertNotNull(model);
            assertArrayEquals(EXPECTED_STRING_ARRAY, model.getValue());
            objectValueChecker.accept(model, payload);
            assertArrayEquals(EXPECTED_STRING_ARRAY, model.getConstructorValue());
            assertArrayEquals(EXPECTED_STRING_ARRAY, model.getValueSupplier().getValue());
        }

        prepareRequest(context.request(), EXPECTED_STRING);
        StringArrays model = context.request().adaptTo(StringArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertEquals(EXPECTED_STRING, model.getValue()[0]);
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(EXPECTED_STRING, model.getConstructorValue()[0]);
    }

    void shouldInjectDefaultStringArray() {
        StringArrays model = context.request().adaptTo(StringArrays.class);
        assertNotNull(model);
        assertArrayEquals(EXPECTED_STRING_ARRAY, model.getDefaultValue());
    }

    @SuppressWarnings("unchecked")
    void shouldInjectStringCollection() {
        shouldInjectStringArray((model, payload) -> {
            if (EXPECTED_STRING_LIST.equals(payload)) {
                assert model.getObjectValue() != null;
                assertTrue(collectionsAreEqual(EXPECTED_STRING_LIST, (Collection<String>) model.getObjectValue()));
            } else {
                assertEquals(payload, model.getObjectValue());
            }
        });
    }

    void shouldInjectStringCollection(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        for (Object payload : Arrays.asList(
            EXPECTED_STRING_ARRAY,
            toObjectArray(EXPECTED_STRING_ARRAY),
            EXPECTED_STRING_LIST)) {

            prepareRequest(context.request(), payload);
            StringCollections model = context.request().adaptTo(StringCollections.class);
            assertNotNull(model);
            assertNotNull(model.getValue());
            objectValueChecker.accept(model, payload);
            assertNotNull(model.getConstructorValue());
            assertNotNull(model.getValueSupplier().getValue());
            assertTrue(collectionsAreEqual(EXPECTED_STRING_LIST, model.getValue()));
            assertTrue(collectionsAreEqual(EXPECTED_STRING_LIST, model.getConstructorValue()));
            assertTrue(collectionsAreEqual(EXPECTED_STRING_LIST, model.getValueSupplier().getValue()));
        }
        prepareRequest(context.request(), EXPECTED_STRING);
        StringCollections model = context.request().adaptTo(StringCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertEquals(EXPECTED_STRING, model.getValue().toArray()[0]);
        assertEquals(1, model.getConstructorValue().size());
        assertEquals(EXPECTED_STRING, model.getConstructorValue().toArray()[0]);
    }

    void shouldInjectDefaultStringCollection() {
        StringCollections model = context.request().adaptTo(StringCollections.class);
        assertNotNull(model);
        assertTrue(collectionsAreEqual(EXPECTED_STRING_LIST, model.getDefaultValue()));
    }
    // endregion

    // region Integers
    void shouldInjectInteger(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE,
            EXPECTED_INTEGER,
            EXPECTED_LONG,
            EXPECTED_FLOAT,
            EXPECTED_FLOAT_STRING,
            EXPECTED_DOUBLE)) {

            prepareRequest(context.request(), payload);
            Integers model = context.request().adaptTo(Integers.class);
            assertNotNull(model);
            assertEquals(EXPECTED_INTEGER, model.getValue());
            objectValueChecker.accept(model, payload);
            assertEquals(EXPECTED_INTEGER, model.getConstructorValue());
            assertEquals(EXPECTED_INTEGER, model.getValueSupplier().getValue());
            if (!payload.equals(EXPECTED_FLOAT_STRING)) {
                assertEquals(
                    StringUtils.substringBefore(String.valueOf(payload), CoreConstants.SEPARATOR_DOT),
                    StringUtils.substringBefore(model.getStringValue(), CoreConstants.SEPARATOR_DOT));
            }
        }

        prepareRequest(context.request(), EXPECTED_STRING);
        Integers model = context.request().adaptTo(Integers.class);
        assertNotNull(model);
        assertEquals(0, model.getValue().intValue());
        assertEquals(0, model.getConstructorValue().intValue());
        assertEquals(0, model.getValueSupplier().getValue().intValue());
    }

    void shouldInjectDefaultInteger() {
        Integers model = context.request().adaptTo(Integers.class);
        assertNotNull(model);
        assertEquals(EXPECTED_DEFAULT_INTEGER, model.getDefaultValue());
        assertEquals(String.valueOf(EXPECTED_DEFAULT_INTEGER), model.getDefaultStringValue());

        prepareRequest(context.request(), EXPECTED_STRING);
        model = context.request().adaptTo(Integers.class);
        assertNotNull(model);
        assertEquals(EXPECTED_DEFAULT_INTEGER, model.getDefaultValue());
    }

    void shouldInjectIntegerArray() {
        shouldInjectIntegerArray(RequestPropertyInjectorTestBase::assertObjectValueEquals, false);
    }

    void shouldInjectIntegerArray(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker, boolean skipStringified) {
        Integer[] objectArray = EXPECTED_INTEGER_ARRAY;
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            toObjectArray(EXPECTED_INTEGER_ARRAY),
            EXPECTED_INTEGER_LIST,
            EXPECTED_LONG_ARRAY,
            EXPECTED_LONG_LIST,
            EXPECTED_DOUBLE_ARRAY,
            EXPECTED_DOUBLE_LIST,
            EXPECTED_DOUBLE_LIST.stream().map(String::valueOf).collect(Collectors.toList()))) {

            prepareRequest(context.request(), payload);
            IntegerArrays model = context.request().adaptTo(IntegerArrays.class);
            assertNotNull(model);
            assertArrayEquals(objectArray, model.getValue());
            objectValueChecker.accept(model, payload);
            assertArrayEquals(objectArray, model.getConstructorValue());
            assertArrayEquals(objectArray, model.getValueSupplier().getValue());
            if (skipStringified) {
                continue;
            }
            if (EXPECTED_INTEGER_ARRAY.equals(payload) || EXPECTED_LONG_LIST.equals(payload)) {
                assertEquals(EXPECTED_INTEGER_ARRAY_STRING, model.getStringValue());
            } else if (EXPECTED_DOUBLE_ARRAY.equals(payload) || EXPECTED_DOUBLE_LIST.equals(payload)) {
                assertEquals(EXPECTED_DOUBLE_ARRAY_STRING, model.getStringValue());
            }
        }

        prepareRequest(context.request(), EXPECTED_INTEGER);
        IntegerArrays model = context.request().adaptTo(IntegerArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertEquals(EXPECTED_INTEGER, model.getValue()[0]);
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(EXPECTED_INTEGER, model.getConstructorValue()[0]);
    }

    void shouldInjectDefaultIntegerArray() {
        IntegerArrays model = context.request().adaptTo(IntegerArrays.class);
        assertNotNull(model);
        assertTrue(arraysAreEqual(EXPECTED_INTEGER_ARRAY, model.getDefaultValue()));
        assertEquals(EXPECTED_INTEGER_ARRAY_STRING, model.getDefaultStringValue());
    }

    void shouldInjectIntegerCollection() {
        shouldInjectIntegerCollection(RequestPropertyInjectorTestBase::assertObjectValueEquals, false);
    }

    void shouldInjectIntegerCollection(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker, boolean skipStringified) {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            EXPECTED_LONG_ARRAY,
            EXPECTED_LONG_LIST,
            EXPECTED_DOUBLE_ARRAY,
            EXPECTED_DOUBLE_LIST)) {

            prepareRequest(context.request(), payload);
            IntegerCollections model = context.request().adaptTo(IntegerCollections.class);
            assertNotNull(model);
            assertNotNull(model.getValue());
            assertNotNull(model.getConstructorValue());
            assertNotNull(model.getValueSupplier().getValue());
            assertTrue(collectionsAreEqual(EXPECTED_INTEGER_LIST, model.getValue()));
            objectValueChecker.accept(model, payload);
            assertTrue(collectionsAreEqual(EXPECTED_INTEGER_LIST, model.getConstructorValue()));
            assertTrue(collectionsAreEqual(EXPECTED_INTEGER_LIST, model.getValueSupplier().getValue()));
            if (skipStringified) {
                continue;
            }
            if (EXPECTED_INTEGER_ARRAY.equals(payload) || EXPECTED_LONG_LIST.equals(payload)) {
                assertEquals(EXPECTED_INTEGER_ARRAY_STRING, model.getStringValue());
            } else if (EXPECTED_DOUBLE_ARRAY.equals(payload) || EXPECTED_DOUBLE_LIST.equals(payload)) {
                assertEquals(EXPECTED_DOUBLE_ARRAY_STRING, model.getStringValue());
            }
        }

        prepareRequest(context.request(), EXPECTED_BYTE);
        IntegerCollections model = context.request().adaptTo(IntegerCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertEquals(EXPECTED_INTEGER, model.getValue().toArray()[0]);
        assertEquals(1, model.getConstructorValue().size());
        assertEquals(EXPECTED_INTEGER, model.getConstructorValue().toArray()[0]);
    }

    void shouldInjectDefaultIntegerCollection() {
        IntegerCollections model = context.request().adaptTo(IntegerCollections.class);
        assertNotNull(model);
        assertTrue(collectionsAreEqual(EXPECTED_INTEGER_LIST, model.getDefaultValue()));

        prepareRequest(context.request(), EXPECTED_STRING_ARRAY);
        model = context.request().adaptTo(IntegerCollections.class);
        assertNotNull(model);
        assertTrue(collectionsAreEqual(EXPECTED_INTEGER_LIST, model.getDefaultValue()));
    }

    void shouldInjectUnparseableIntegerCollection() {
        shouldInjectUnparseableIntegerCollection(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectUnparseableIntegerCollection(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        prepareRequest(context.request(), HALF_PARSEABLE_INTEGERS);
        IntegerCollections model = context.request().adaptTo(IntegerCollections.class);
        assertNotNull(model);
        assertNotNull(model.getValue());
        assertNotNull(model.getConstructorValue());
        assertNotNull(model.getValueSupplier().getValue());
        assertTrue(collectionsAreEqual(EXPECTED_HALF_PARSED_INTEGER_LIST, model.getValue()));
        objectValueChecker.accept(model, HALF_PARSEABLE_INTEGERS);
        assertTrue(collectionsAreEqual(EXPECTED_HALF_PARSED_INTEGER_LIST, model.getConstructorValue()));
        assertTrue(collectionsAreEqual(EXPECTED_HALF_PARSED_INTEGER_LIST, model.getValueSupplier().getValue()));
    }
    // endregion

    // region Longs
    void shouldInjectLong(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE,
            EXPECTED_INTEGER,
            EXPECTED_LONG,
            EXPECTED_FLOAT,
            EXPECTED_FLOAT_STRING,
            EXPECTED_DOUBLE)) {

            prepareRequest(context.request(), payload);
            Longs model = context.request().adaptTo(Longs.class);
            assertNotNull(model);
            assert model.getValue() != null;
            assertEquals(EXPECTED_LONG, (long) model.getValue());
            objectValueChecker.accept(model, payload);
            assertEquals(EXPECTED_LONG, (long) model.getConstructorValue());
            assertEquals(EXPECTED_LONG, (long) model.getValueSupplier().getValue());
        }

        prepareRequest(context.request(), EXPECTED_STRING);
        Longs model = context.request().adaptTo(Longs.class);
        assertNotNull(model);
        assertNotNull(model.getValue());
        assertEquals(0L, model.getValue().longValue());
        assertEquals(0L, model.getConstructorValue().longValue());
        assertEquals(0L, model.getValueSupplier().getValue().longValue());
    }

    void shouldInjectDefaultLong() {
        Longs model = context.request().adaptTo(Longs.class);
        assertNotNull(model);
        assertNotNull(model.getDefaultValue());
        assertEquals(EXPECTED_DEFAULT_LONG, model.getDefaultValue().longValue());

        prepareRequest(context.request(), EXPECTED_STRING);
        model = context.request().adaptTo(Longs.class);
        assertNotNull(model);
        assertNotNull(model.getDefaultValue());
        assertEquals(EXPECTED_DEFAULT_LONG, model.getDefaultValue().longValue());
    }

    void shouldInjectLongArray() {
        shouldInjectLongArray(RequestPropertyInjectorTestBase::assertObjectValueEquals, false);
    }

    void shouldInjectLongArray(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker, boolean skipStringified) {
        Long[] objectArray = EXPECTED_LONG_ARRAY;
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            EXPECTED_INTEGER_LIST,
            EXPECTED_LONG_ARRAY,
            EXPECTED_LONG_LIST,
            EXPECTED_DOUBLE_ARRAY,
            EXPECTED_DOUBLE_LIST)) {

            prepareRequest(context.request(), payload);
            LongArrays model = context.request().adaptTo(LongArrays.class);
            assertNotNull(model);
            assertArrayEquals(objectArray, model.getValue());
            objectValueChecker.accept(model, payload);
            assertArrayEquals(objectArray, model.getConstructorValue());
            assertArrayEquals(objectArray, model.getValueSupplier().getValue());
            if (skipStringified) {
                continue;
            }
            if (EXPECTED_INTEGER_ARRAY.equals(payload) || EXPECTED_LONG_LIST.equals(payload)) {
                assertEquals(EXPECTED_INTEGER_ARRAY_STRING, model.getStringValue());
            } else if (EXPECTED_DOUBLE_ARRAY.equals(payload) || EXPECTED_DOUBLE_LIST.equals(payload)) {
                assertEquals(EXPECTED_DOUBLE_ARRAY_STRING, model.getStringValue());
            }
        }

        prepareRequest(context.request(), EXPECTED_INTEGER);
        LongArrays model = context.request().adaptTo(LongArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertEquals(EXPECTED_LONG, (long) model.getValue()[0]);
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(EXPECTED_LONG, (long) model.getConstructorValue()[0]);
    }

    void shouldInjectDefaultLongArray() {
        LongArrays model = context.request().adaptTo(LongArrays.class);
        assertNotNull(model);
        assertArrayEquals(EXPECTED_LONG_ARRAY, model.getDefaultValue());

        prepareRequest(context.request(), EXPECTED_STRING);
        model = context.request().adaptTo(LongArrays.class);
        assertNotNull(model);
        assertArrayEquals(EXPECTED_LONG_ARRAY, model.getDefaultValue());
    }

    void shouldInjectLongCollection() {
        shouldInjectLongCollection(RequestPropertyInjectorTestBase::assertObjectValueEquals, false);
    }

    void shouldInjectLongCollection(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker, boolean skipStringified) {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            EXPECTED_INTEGER_LIST,
            EXPECTED_LONG_ARRAY,
            EXPECTED_LONG_LIST,
            EXPECTED_DOUBLE_ARRAY,
            EXPECTED_DOUBLE_LIST)) {

            prepareRequest(context.request(), payload);
            LongCollections model = context.request().adaptTo(LongCollections.class);
            assertNotNull(model);
            assertNotNull(model.getValue());
            assertNotNull(model.getConstructorValue());
            assertNotNull(model.getValueSupplier().getValue());
            assertTrue(collectionsAreEqual(EXPECTED_LONG_LIST, model.getValue()));
            objectValueChecker.accept(model, payload);
            assertTrue(collectionsAreEqual(EXPECTED_LONG_LIST, model.getConstructorValue()));
            assertTrue(collectionsAreEqual(EXPECTED_LONG_LIST, model.getValueSupplier().getValue()));
            if (skipStringified) {
                continue;
            }
            if (EXPECTED_INTEGER_ARRAY.equals(payload) || EXPECTED_LONG_LIST.equals(payload)) {
                assertEquals(EXPECTED_INTEGER_ARRAY_STRING, model.getStringValue());
            } else if (EXPECTED_DOUBLE_ARRAY.equals(payload) || EXPECTED_DOUBLE_LIST.equals(payload)) {
                assertEquals(EXPECTED_DOUBLE_ARRAY_STRING, model.getStringValue());
            }
        }

        prepareRequest(context.request(), EXPECTED_BYTE);
        LongCollections model = context.request().adaptTo(LongCollections.class);
        assertNotNull(model);
        assertNotNull(model.getValue());
        assertEquals(1, model.getValue().size());
        assertEquals(EXPECTED_LONG, model.getValue().toArray()[0]);
        assertEquals(1, model.getConstructorValue().size());
        assertEquals(EXPECTED_LONG, model.getConstructorValue().toArray()[0]);
    }

    void shouldInjectDefaultLongCollection() {
        LongCollections model = context.request().adaptTo(LongCollections.class);
        assertNotNull(model);
        assertTrue(collectionsAreEqual(EXPECTED_LONG_LIST, model.getDefaultValue()));
    }

    void shouldInjectUnparseableLongCollection() {
        shouldInjectUnparseableIntegerCollection(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectUnparseableLongCollection(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        prepareRequest(context.request(), HALF_PARSEABLE_INTEGERS);
        LongCollections model = context.request().adaptTo(LongCollections.class);
        assertNotNull(model);
        assertNotNull(model.getValue());
        assertNotNull(model.getConstructorValue());
        assertNotNull(model.getValueSupplier().getValue());
        assertTrue(collectionsAreEqual(EXPECTED_HALF_PARSED_LONG_LIST, model.getValue()));
        objectValueChecker.accept(model, HALF_PARSEABLE_INTEGERS);
        assertTrue(collectionsAreEqual(EXPECTED_HALF_PARSED_LONG_LIST, model.getConstructorValue()));
        assertTrue(collectionsAreEqual(EXPECTED_HALF_PARSED_LONG_LIST, model.getValueSupplier().getValue()));
    }
    // endregion

    // region Doubles
    void shouldInjectDouble(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE,
            EXPECTED_INTEGER,
            EXPECTED_LONG,
            EXPECTED_FLOAT,
            EXPECTED_FLOAT_STRING,
            EXPECTED_DOUBLE)) {

            double delta = getDelta(payload);
            prepareRequest(context.request(), payload);
            Doubles model = context.request().adaptTo(Doubles.class);
            assertNotNull(model);
            assert model.getValue() != null;
            assertEquals(EXPECTED_DOUBLE, model.getValue(), delta);
            objectValueChecker.accept(model, payload);
            assertEquals(EXPECTED_DOUBLE, model.getConstructorValue(), delta);
            assertEquals(EXPECTED_DOUBLE, model.getValueSupplier().getValue(), delta);
        }

        prepareRequest(context.request(), EXPECTED_STRING);
        Doubles model = context.request().adaptTo(Doubles.class);
        assertNotNull(model);
        assertNotNull(model.getValue());
        assertEquals(0, model.getValue().intValue());
        assertEquals(0, model.getConstructorValue().intValue());
        assertEquals(0, model.getValueSupplier().getValue().intValue());
    }

    void shouldInjectDefaultDouble() {
        Doubles model = context.request().adaptTo(Doubles.class);
        assertNotNull(model);
        assertNotNull(model.getDefaultValue());
        assertEquals(EXPECTED_DEFAULT_DOUBLE, model.getDefaultValue(), 0.0001);

        prepareRequest(context.request(), EXPECTED_STRING);
        model = context.request().adaptTo(Doubles.class);
        assertNotNull(model);
        assertNotNull(model.getDefaultValue());
        assertEquals(EXPECTED_DEFAULT_DOUBLE, model.getDefaultValue(), 0.0001);
    }

    void shouldInjectDoubleArray() {
        shouldInjectDoubleArray(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectDoubleArray(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            EXPECTED_INTEGER_LIST,
            EXPECTED_LONG_ARRAY,
            EXPECTED_LONG_LIST,
            EXPECTED_DOUBLE_ARRAY,
            EXPECTED_DOUBLE_LIST)) {

            double delta = getDelta(payload);
            prepareRequest(context.request(), payload);
            DoubleArrays model = context.request().adaptTo(DoubleArrays.class);
            assertNotNull(model);
            assertTrue(doubleArraysAreEqual(EXPECTED_DOUBLE_ARRAY, model.getValue(),delta));
            objectValueChecker.accept(model, payload);
            assertTrue(doubleArraysAreEqual(EXPECTED_DOUBLE_ARRAY, model.getConstructorValue(), delta));
            assertTrue(doubleArraysAreEqual(EXPECTED_DOUBLE_ARRAY, model.getValueSupplier().getValue(), delta));
            if (EXPECTED_INTEGER_ARRAY.equals(payload) || EXPECTED_LONG_LIST.equals(payload)) {
                assertEquals(EXPECTED_INTEGER_ARRAY_STRING, model.getStringValue());
            } else if (EXPECTED_DOUBLE_ARRAY.equals(payload) || EXPECTED_DOUBLE_LIST.equals(payload)) {
                assertEquals(EXPECTED_DOUBLE_ARRAY_STRING, model.getStringValue());
            }
        }

        prepareRequest(context.request(), EXPECTED_DOUBLE);
        DoubleArrays model = context.request().adaptTo(DoubleArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertEquals(EXPECTED_DOUBLE, model.getValue()[0], 0.0d);
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(EXPECTED_DOUBLE, model.getConstructorValue()[0], 0.0d);
    }

    void shouldInjectDefaultDoubleArray() {
        DoubleArrays model = context.request().adaptTo(DoubleArrays.class);
        assertNotNull(model);
        assertTrue(doubleArraysAreEqual(EXPECTED_DOUBLE_ARRAY, model.getDefaultValue(), 0.0001d));

        prepareRequest(context.request(), EXPECTED_STRING_ARRAY);
        model = context.request().adaptTo(DoubleArrays.class);
        assertNotNull(model);
        assertTrue(doubleArraysAreEqual(EXPECTED_DOUBLE_ARRAY, model.getDefaultValue(), 0.0001d));
    }

    void shouldInjectDoubleCollection() {
        shouldInjectDoubleCollection(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectDoubleCollection(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            EXPECTED_INTEGER_LIST,
            EXPECTED_LONG_ARRAY,
            EXPECTED_LONG_LIST,
            EXPECTED_DOUBLE_ARRAY,
            EXPECTED_DOUBLE_LIST)) {

            double delta = getDelta(payload);
            prepareRequest(context.request(), payload);
            DoubleCollections model = context.request().adaptTo(DoubleCollections.class);
            assertNotNull(model);
            assertTrue(doubleCollectionsAreEqual(EXPECTED_DOUBLE_LIST, model.getValue(), delta));
            objectValueChecker.accept(model, payload);
            assertTrue(doubleCollectionsAreEqual(EXPECTED_DOUBLE_LIST, model.getConstructorValue(), delta));
            assertTrue(doubleCollectionsAreEqual(EXPECTED_DOUBLE_LIST, model.getValueSupplier().getValue(), delta));
            if (EXPECTED_BYTE_ARRAY.equals(payload) || EXPECTED_LONG_LIST.equals(payload)) {
                assertEquals(EXPECTED_INTEGER_ARRAY_STRING, model.getStringValue());
            } else if (EXPECTED_DOUBLE_ARRAY.equals(payload) || EXPECTED_DOUBLE_LIST.equals(payload)) {
                assertEquals(EXPECTED_DOUBLE_ARRAY_STRING, model.getStringValue());
            }
        }

        prepareRequest(context.request(), EXPECTED_FLOAT);
        DoubleCollections model = context.request().adaptTo(DoubleCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertEquals(EXPECTED_DOUBLE, (double) model.getValue().toArray()[0], 0.01d);
        assertEquals(1, model.getConstructorValue().size());
        assertEquals(EXPECTED_DOUBLE, (double) model.getConstructorValue().toArray()[0], 0.01d);
    }

    void shouldInjectDefaultDoubleCollection() {
        DoubleCollections model = context.request().adaptTo(DoubleCollections.class);
        assertNotNull(model);
        assertTrue(collectionsAreEqual(
            EXPECTED_DOUBLE_LIST,
            model.getDefaultValue(),
            (first, second) -> Math.abs(first - (double) second) < 0.0001));
        assertEquals(EXPECTED_DOUBLE_ARRAY_STRING, model.getDefaultStringValue());
    }

    void shouldInjectUnparseableDoubleCollection() {
        shouldInjectUnparseableDoubleCollection(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectUnparseableDoubleCollection(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        prepareRequest(context.request(), HALF_PARSEABLE_FLOATING_POINT_VALUES);
        DoubleCollections model = context.request().adaptTo(DoubleCollections.class);
        assertNotNull(model);
        assertNotNull(model.getValue());
        assertNotNull(model.getConstructorValue());
        assertNotNull(model.getValueSupplier().getValue());
        assertTrue(doubleCollectionsAreEqual(EXPECTED_HALF_PARSED_DOUBLE_LIST, model.getValue(), 0.01d));
        objectValueChecker.accept(model, HALF_PARSEABLE_FLOATING_POINT_VALUES);
        assertTrue(doubleCollectionsAreEqual(EXPECTED_HALF_PARSED_DOUBLE_LIST, model.getConstructorValue(), 0.01d));
        assertTrue(doubleCollectionsAreEqual(EXPECTED_HALF_PARSED_DOUBLE_LIST, model.getValueSupplier().getValue(), 0.01d));
    }
    // endregion

    // region Booleans
    void shouldInjectBoolean() {
        shouldInjectBoolean(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectBoolean(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        prepareRequest(context.request(), true);
        Booleans model = context.request().adaptTo(Booleans.class);
        assertNotNull(model);
        assertTrue(model.getValue());
        objectValueChecker.accept(model, true);
        assertTrue(model.getConstructorValue());
        assertTrue(model.getValueSupplier().getValue());
    }

    void shouldInjectDefaultBoolean() {
        Booleans model = context.request().adaptTo(Booleans.class);
        assertNotNull(model);
        assertTrue(model.getDefaultValue());
        assertEquals(Boolean.TRUE.toString(), model.getDefaultStringValue());

        prepareRequest(context.request(), EXPECTED_STRING);
        model = context.request().adaptTo(Booleans.class);
        assertNotNull(model);
        assertTrue(model.getDefaultValue());
    }

    void shouldInjectBooleanArray() {
        shouldInjectBooleanArray(RequestPropertyInjectorTestBase::assertObjectValueEquals, false);
    }

    void shouldInjectBooleanArray(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker, boolean skipStringified) {
        for (Object payload : Arrays.asList(
            EXPECTED_BOOLEAN_ARRAY,
            ArrayUtils.toPrimitive(EXPECTED_BOOLEAN_ARRAY))) {

            prepareRequest(context.request(), payload);
            BooleanArrays model = context.request().adaptTo(BooleanArrays.class);
            assertNotNull(model);
            assertArrayEquals(EXPECTED_BOOLEAN_ARRAY, model.getValue());
            objectValueChecker.accept(model, payload);
            assertArrayEquals(EXPECTED_BOOLEAN_ARRAY, model.getConstructorValue());
            assertArrayEquals(EXPECTED_BOOLEAN_ARRAY, model.getValueSupplier().getValue());
            if (skipStringified) {
                continue;
            }
            assertEquals(EXPECTED_BOOLEAN_ARRAY_STRING, model.getStringValue());
            assertEquals(EXPECTED_BOOLEAN_ARRAY_STRING, model.getDefaultStringValue());
        }

        prepareRequest(context.request(), true);
        BooleanArrays model = context.request().adaptTo(BooleanArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertTrue(model.getValue()[0]);
        assertEquals(1, model.getConstructorValue().length);
        assertTrue(model.getConstructorValue()[0]);
    }

    void shouldInjectDefaultBooleanArray() {
        BooleanArrays model = context.request().adaptTo(BooleanArrays.class);
        assertNotNull(model);
        assertTrue(arraysAreEqual(EXPECTED_BOOLEAN_ARRAY, model.getDefaultValue()));

        prepareRequest(context.request(), EXPECTED_STRING_ARRAY);
        model = context.request().adaptTo(BooleanArrays.class);
        assertNotNull(model);
        assertTrue(arraysAreEqual(EXPECTED_BOOLEAN_ARRAY, model.getDefaultValue()));
    }

    void shouldInjectBooleanCollection() {
        shouldInjectBooleanCollection(RequestPropertyInjectorTestBase::assertObjectValueEquals, false);
    }

    void shouldInjectBooleanCollection(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker, boolean skipStringified) {
        for (Object payload : Arrays.asList(
            EXPECTED_BOOLEAN_ARRAY,
            EXPECTED_BOOLEAN_LIST)) {

            prepareRequest(context.request(), payload);
            BooleanCollections model = context.request().adaptTo(BooleanCollections.class);
            assertNotNull(model);
            assertNotNull(model.getValue());
            assertNotNull(model.getConstructorValue());
            assertNotNull(model.getValueSupplier().getValue());
            assertTrue(collectionsAreEqual(EXPECTED_BOOLEAN_LIST, model.getValue()));
            objectValueChecker.accept(model, payload);
            assertTrue(collectionsAreEqual(EXPECTED_BOOLEAN_LIST, model.getConstructorValue()));
            assertTrue(collectionsAreEqual(EXPECTED_BOOLEAN_LIST, model.getValueSupplier().getValue()));
            if (skipStringified) {
                continue;
            }
            assertEquals(EXPECTED_BOOLEAN_ARRAY_STRING, model.getStringValue());
            assertEquals(EXPECTED_BOOLEAN_ARRAY_STRING, model.getDefaultStringValue());
        }

        prepareRequest(context.request(), true);
        BooleanCollections model = context.request().adaptTo(BooleanCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertTrue((boolean) model.getValue().toArray()[0]);
        assertEquals(1, model.getConstructorValue().size());
        assertTrue((boolean) model.getConstructorValue().toArray()[0]);
    }

    void shouldInjectDefaultBooleanCollection() {
        BooleanCollections model = context.request().adaptTo(BooleanCollections.class);
        assertNotNull(model);
        assertTrue(collectionsAreEqual(EXPECTED_BOOLEAN_LIST, model.getDefaultValue()));
    }
    //endregion

    void shouldNotCauseExceptionWhenPayloadMissing() {
        prepareRequest(context.request(), null);
        for (Class<? extends RequestAdapterBase<?>> testClass : GENERIC_TEST_CASES) {
            RequestAdapterBase<?> model = context.request().adaptTo(testClass);
            assertNotNull(model);
        }
    }

    /* ----------
       Assertions
       ---------- */

    private static void assertObjectValueEquals(RequestAdapterBase<?> model, Object payload) {
        assertNotNull(model.getObjectValue());
        assertEquals(payload, model.getObjectValue());
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> boolean arraysAreEqual(T[] expected, T[] actual) {
        return arraysAreEqual(expected, actual, (first, second) -> first == second);
    }

    private static <T> boolean arraysAreEqual(T[] expected, T[] actual, BiPredicate<T, T> comparator) {
        if (expected == null || actual == null || expected.length != actual.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (!comparator.test(expected[i], actual[i])) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean doubleArraysAreEqual(Double[] expected, Double[] actual, double delta) {
        return arraysAreEqual(
            expected,
            actual,
            (first, second) -> Math.abs(first - (double) second) < delta);
    }

    private static <T> boolean collectionsAreEqual(Collection<T> expected, Collection<T> actual) {
        return collectionsAreEqual(expected, actual, Objects::equals);
    }

    private static <T> boolean collectionsAreEqual(Collection<T> expected, Collection<T> actual, BiPredicate<T, T> comparator) {
        if (expected == null || actual == null || expected.size() != actual.size()) {
            return false;
        }
        Iterator<T> expectedIterator = expected.iterator();
        Iterator<T> actualIterator = actual.iterator();
        while (expectedIterator.hasNext() && actualIterator.hasNext()) {
            T expectedEntry = expectedIterator.next();
            T actualEntry = actualIterator.next();
            if (!comparator.test(expectedEntry, actualEntry)) {
                return false;
            }
        }
        return true;
    }

    private static boolean doubleCollectionsAreEqual(Collection<Double> expected, Collection<Double> actual, double delta) {
        return collectionsAreEqual(
            expected,
            actual,
            (first, second) -> Math.abs(first - (double) second) < delta);
    }

    /* ---------------
       Service methods
       --------------- */

    private static <T> Object[] toObjectArray(T[] value) {
        Object[] result = new Object[value.length];
        System.arraycopy(value, 0, result, 0, value.length);
        return result;
    }

    private static double getDelta(Object payload) {
        double result = 0.11d;
        if (ClassUtils.isAssignable(payload.getClass(), Float.class)) {
            result = 0.01d;
        } else if (ClassUtils.isAssignable(payload.getClass(), Double.class)) {
            result = 0d;
        }
        return result;
    }
}
