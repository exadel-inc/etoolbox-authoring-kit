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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Before;
import org.junit.Rule;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    private static final String MODELS_PACKAGE_NAME = "com.exadel.aem.toolkit.core.injectors.models.requestproperty";

    private static final String EXPECTED_STRING = "Hello World";
    static final String[] EXPECTED_STRING_ARRAY = {"Hello", "World"};
    private static final List<String> EXPECTED_STRING_LIST = Arrays.asList(EXPECTED_STRING_ARRAY);

    private static final byte EXPECTED_BYTE = 42;
    private static final byte[] EXPECTED_BYTE_ARRAY = {42, 43, 44};

    private static final Integer EXPECTED_INTEGER = 42;
    private static final int[] EXPECTED_INTEGER_ARRAY = {42, 43, 44};
    private static final List<Integer> EXPECTED_INTEGER_LIST = Arrays.asList(42, 43, 44);

    private static final long EXPECTED_LONG = 42L;
    private static final long[] EXPECTED_LONG_ARRAY = {42L, 43L, 44};
    private static final List<Long> EXPECTED_LONG_LIST = Arrays.asList(42L, 43L, 44L);

    private static final float EXPECTED_FLOAT = 42.1f;
    private static final String EXPECTED_FLOAT_STRING = "42.1f";

    private static final double EXPECTED_DOUBLE = 42.1d;
    private static final double[] EXPECTED_DOUBLE_ARRAY = {42.1d, 43.1d, 44.1d};
    static final List<Double> EXPECTED_DOUBLE_LIST = Arrays.asList(42.1d, 43.1d, 44.1d);

    private static final boolean[] EXPECTED_BOOLEAN_ARRAY = {true, true, false};
    private static final List<Boolean> EXPECTED_BOOLEAN_LIST = Arrays.asList(true, true, false);

    @Rule
    public final AemContext context = new AemContext();

    /* -----------
       Preparation
       ----------- */

    @Before
    public void beforeTest() {
        context.addModelsForPackage(MODELS_PACKAGE_NAME);
        context.registerInjectActivateService(new DelegateInjector(prepareInjector()));
    }

    abstract BaseInjector<?> prepareInjector();

    abstract void prepareRequest(MockSlingHttpServletRequest request, Object payload);

    /* -----
       Tests
       ----- */

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

    void shouldInjectStringCollection() {
        shouldInjectStringArray((model, payload) -> {
            if (EXPECTED_STRING_LIST.equals(payload)) {
                assert model.getObjectValue() != null;
                assertTrue(CollectionUtils.isEqualCollection(EXPECTED_STRING_LIST, (Collection<?>) model.getObjectValue()));
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
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_STRING_LIST, model.getValue()));
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_STRING_LIST, model.getConstructorValue()));
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_STRING_LIST, model.getValueSupplier().getValue()));
        }
        prepareRequest(context.request(), EXPECTED_STRING);
        StringCollections model = context.request().adaptTo(StringCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertEquals(EXPECTED_STRING, model.getValue().toArray()[0]);
        assertEquals(1, model.getConstructorValue().size());
        assertEquals(EXPECTED_STRING, model.getConstructorValue().toArray()[0]);
    }

    void shouldInjectInteger() {
        shouldInjectInteger(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectInteger(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE,
            EXPECTED_INTEGER,
            EXPECTED_LONG,
            EXPECTED_FLOAT,
            EXPECTED_FLOAT_STRING ,
            EXPECTED_DOUBLE)) {

            prepareRequest(context.request(), payload);
            Integers model = context.request().adaptTo(Integers.class);
            assertNotNull(model);
            assertEquals(EXPECTED_INTEGER, model.getValue());
            objectValueChecker.accept(model, payload);
            assertEquals(EXPECTED_INTEGER, model.getConstructorValue());
            assertEquals(EXPECTED_INTEGER, model.getValueSupplier().getValue());
        }
    }

    void shouldInjectIntegerArray() {
        shouldInjectIntegerArray(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectIntegerArray(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        Integer[] objectArray = ArrayUtils.toObject(EXPECTED_INTEGER_ARRAY);
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            ArrayUtils.toObject(EXPECTED_INTEGER_ARRAY),
            toObjectArray(ArrayUtils.toObject(EXPECTED_INTEGER_ARRAY)),
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
        }

        prepareRequest(context.request(), EXPECTED_INTEGER);
        IntegerArrays model = context.request().adaptTo(IntegerArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertEquals(EXPECTED_INTEGER, model.getValue()[0]);
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(EXPECTED_INTEGER, model.getConstructorValue()[0]);
    }

    void shouldInjectIntegerCollection() {
        shouldInjectIntegerCollection(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectIntegerCollection(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
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
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_INTEGER_LIST, model.getValue()));
            objectValueChecker.accept(model, payload);
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_INTEGER_LIST, model.getConstructorValue()));
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_INTEGER_LIST, model.getValueSupplier().getValue()));
        }

        prepareRequest(context.request(), EXPECTED_BYTE);
        IntegerCollections model = context.request().adaptTo(IntegerCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertEquals(EXPECTED_INTEGER, model.getValue().toArray()[0]);
        assertEquals(1, model.getConstructorValue().size());
        assertEquals(EXPECTED_INTEGER, model.getConstructorValue().toArray()[0]);
    }

    void shouldInjectLong() {
        shouldInjectLong(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

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
    }

    void shouldInjectLongArray() {
        shouldInjectLongArray(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectLongArray(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        Long[] objectArray = ArrayUtils.toObject(EXPECTED_LONG_ARRAY);
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            ArrayUtils.toObject(EXPECTED_INTEGER_ARRAY),
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
        }

        prepareRequest(context.request(), EXPECTED_INTEGER);
        LongArrays model = context.request().adaptTo(LongArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertEquals(EXPECTED_LONG, (long) model.getValue()[0]);
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(EXPECTED_LONG, (long) model.getConstructorValue()[0]);
    }

    void shouldInjectLongCollection() {
        shouldInjectLongCollection(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectLongCollection(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
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
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_LONG_LIST, model.getValue()));
            objectValueChecker.accept(model, payload);
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_LONG_LIST, model.getConstructorValue()));
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_LONG_LIST, model.getValueSupplier().getValue()));
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

    void shouldInjectDouble() {
        shouldInjectDouble(RequestPropertyInjectorTestBase::assertObjectValueEquals);

    }

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
            assertArrayEquals(EXPECTED_DOUBLE_ARRAY, ArrayUtils.toPrimitive(model.getValue()), delta);
            objectValueChecker.accept(model, payload);
            assertArrayEquals(EXPECTED_DOUBLE_ARRAY, ArrayUtils.toPrimitive(model.getConstructorValue()), delta);
            assertArrayEquals(EXPECTED_DOUBLE_ARRAY, ArrayUtils.toPrimitive(model.getValueSupplier().getValue()), delta);
        }

        prepareRequest(context.request(), EXPECTED_DOUBLE);
        DoubleArrays model = context.request().adaptTo(DoubleArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertEquals(EXPECTED_DOUBLE, model.getValue()[0], 0.0d);
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(EXPECTED_DOUBLE, model.getConstructorValue()[0], 0.0d);
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
            assertTrue(isEqualCollection(EXPECTED_DOUBLE_LIST, model.getValue(), delta));
            objectValueChecker.accept(model, payload);
            assertTrue(isEqualCollection(EXPECTED_DOUBLE_LIST, model.getConstructorValue(), delta));
            assertTrue(isEqualCollection(EXPECTED_DOUBLE_LIST, model.getValueSupplier().getValue(), delta));
        }

        prepareRequest(context.request(), EXPECTED_FLOAT);
        DoubleCollections model = context.request().adaptTo(DoubleCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertEquals(EXPECTED_DOUBLE, (double) model.getValue().toArray()[0], 0.01d);
        assertEquals(1, model.getConstructorValue().size());
        assertEquals(EXPECTED_DOUBLE, (double) model.getConstructorValue().toArray()[0], 0.01d);
    }


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

    void shouldInjectBooleanArray() {
        shouldInjectBooleanArray(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectBooleanArray(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        for (Object payload : Arrays.asList(
            EXPECTED_BOOLEAN_ARRAY,
            ArrayUtils.toObject(EXPECTED_BOOLEAN_ARRAY))) {

            prepareRequest(context.request(), payload);
            BooleanArrays model = context.request().adaptTo(BooleanArrays.class);
            assertNotNull(model);
            assertArrayEquals(EXPECTED_BOOLEAN_ARRAY, ArrayUtils.toPrimitive(model.getValue()));
            objectValueChecker.accept(model, payload);
            assertArrayEquals(EXPECTED_BOOLEAN_ARRAY, ArrayUtils.toPrimitive(model.getConstructorValue()));
            assertArrayEquals(EXPECTED_BOOLEAN_ARRAY, ArrayUtils.toPrimitive(model.getValueSupplier().getValue()));
        }

        prepareRequest(context.request(), true);
        BooleanArrays model = context.request().adaptTo(BooleanArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertTrue(model.getValue()[0]);
        assertEquals(1, model.getConstructorValue().length);
        assertTrue(model.getConstructorValue()[0]);
    }

    void shouldInjectBooleanCollection() {
        shouldInjectBooleanCollection(RequestPropertyInjectorTestBase::assertObjectValueEquals);
    }

    void shouldInjectBooleanCollection(BiConsumer<RequestAdapterBase<?>, Object> objectValueChecker) {
        for (Object payload : Arrays.asList(
            EXPECTED_BOOLEAN_ARRAY,
            EXPECTED_BOOLEAN_LIST)) {

            prepareRequest(context.request(), payload);
            BooleanCollections model = context.request().adaptTo(BooleanCollections.class);
            assertNotNull(model);
            assertNotNull(model.getValue());
            assertNotNull(model.getConstructorValue());
            assertNotNull(model.getValueSupplier().getValue());
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_BOOLEAN_LIST, model.getValue()));
            objectValueChecker.accept(model, payload);
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_BOOLEAN_LIST, model.getConstructorValue()));
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_BOOLEAN_LIST, model.getValueSupplier().getValue()));
        }

        prepareRequest(context.request(), true);
        BooleanCollections model = context.request().adaptTo(BooleanCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertTrue((boolean) model.getValue().toArray()[0]);
        assertEquals(1, model.getConstructorValue().size());
        assertTrue((boolean) model.getConstructorValue().toArray()[0]);
    }

    void shouldNotCauseExceptionWhenPayloadMissing() {
        prepareRequest(context.request(), null);
        for (Class<? extends RequestAdapterBase<?>> testClass : GENERIC_TEST_CASES) {
            RequestAdapterBase<?> model = context.request().adaptTo(testClass);
            assertNotNull(model);
        }
    }

    /* ---------------
       Service methods
       --------------- */

    private static void assertObjectValueEquals(RequestAdapterBase<?> model, Object payload) {
        assertNotNull(model.getObjectValue());
        assertEquals(payload, model.getObjectValue());
    }

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

    @SuppressWarnings("SameParameterValue")
    private static boolean isEqualCollection(Collection<Double> expected, Collection<Double> actual, double delta) {
        if (expected == null || actual == null || expected.size() != actual.size()) {
            return false;
        }
        for (Double value : expected) {
            if (actual.stream().filter(a -> Math.abs(a - value) < delta).count() != 1) {
                return false;
            }
        }
        return true;
    }
}
