package com.exadel.aem.toolkit.core.injectors;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.injectors.models.requestattribute.BooleanArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.BooleanCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.Booleans;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.CalendarArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.CalendarCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.Calendars;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.DoubleArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.DoubleCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.Doubles;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.IntegerArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.IntegerCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.Integers;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.LongArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.LongCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.Longs;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.StringArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.StringCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestattribute.Strings;

public class RequestAttributeInjectorTest {

    private static final String ATTRIBUTE_VALUE = "value";

    private static final String EXPECTED_STRING = "Hello World";
    private static final String[] EXPECTED_STRING_ARRAY = {"Hello", "World"};
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
    private static final double EXPECTED_DOUBLE = 42.1d;
    private static final double[] EXPECTED_DOUBLE_ARRAY = {42.1d, 43.1d, 44.1d};
    private static final List<Double> EXPECTED_DOUBLE_LIST = Arrays.asList(42.1d, 43.1d, 44.1d);

    private static final boolean[] EXPECTED_BOOLEAN_ARRAY = {true, true, false};
    private static final List<Boolean> EXPECTED_BOOLEAN_LIST = Arrays.asList(true, true, false);

    @Rule
    public final AemContext context = new AemContext();

    @Before
    public void beforeTest() {
        context.addModelsForPackage("com.exadel.aem.toolkit.core.injectors.models.requestattribute");
        context.registerInjectActivateService(new RequestAttributeInjector());
    }

    @Test
    public void shouldInjectString() {
        context.request().setAttribute(ATTRIBUTE_VALUE, EXPECTED_STRING);
        Strings model = context.request().adaptTo(Strings.class);
        assertNotNull(model);
        assertEquals(EXPECTED_STRING, model.getValue());
        assertEquals(EXPECTED_STRING, model.getObjectValue());
        assertEquals(EXPECTED_STRING, model.getConstructorValue());
        assertEquals(EXPECTED_STRING, model.getValueSupplier().getValue());
    }

    @Test
    public void shouldInjectStringArray() {
        for (Object payload : Arrays.asList(
            EXPECTED_STRING_ARRAY,
            toObjectArray(EXPECTED_STRING_ARRAY),
            EXPECTED_STRING_LIST)) {

            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            StringArrays model = context.request().adaptTo(StringArrays.class);
            assertNotNull(model);
            assertArrayEquals(EXPECTED_STRING_ARRAY, model.getValue());
            assertEquals(payload, model.getObjectValue());
            assertArrayEquals(EXPECTED_STRING_ARRAY, model.getConstructorValue());
            assertArrayEquals(EXPECTED_STRING_ARRAY, model.getValueSupplier().getValue());
        }

        context.request().setAttribute(ATTRIBUTE_VALUE, EXPECTED_STRING);
        StringArrays model = context.request().adaptTo(StringArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertEquals(EXPECTED_STRING, model.getValue()[0]);
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(EXPECTED_STRING, model.getConstructorValue()[0]);
    }

    @Test
    public void shouldInjectStringCollection() {
        for (Object payload : Arrays.asList(
            EXPECTED_STRING_ARRAY,
            toObjectArray(EXPECTED_STRING_ARRAY),
            EXPECTED_STRING_LIST)) {

            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            StringCollections model = context.request().adaptTo(StringCollections.class);
            assertNotNull(model);
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_STRING_LIST, model.getValue()));
            if (EXPECTED_STRING_LIST.equals(payload)) {
                assertTrue(CollectionUtils.isEqualCollection(EXPECTED_STRING_LIST, (Collection<?>) model.getObjectValue()));
            }
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_STRING_LIST, model.getConstructorValue()));
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_STRING_LIST, model.getValueSupplier().getValue()));
        }
        context.request().setAttribute(ATTRIBUTE_VALUE, EXPECTED_STRING);
        StringCollections model = context.request().adaptTo(StringCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertEquals(EXPECTED_STRING, model.getValue().toArray()[0]);
        assertEquals(1, model.getConstructorValue().size());
        assertEquals(EXPECTED_STRING, model.getConstructorValue().toArray()[0]);
    }

    @Test
    public void shouldInjectInteger() {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE,
            EXPECTED_INTEGER,
            EXPECTED_LONG,
            EXPECTED_FLOAT,
            EXPECTED_DOUBLE)) {

            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            Integers model = context.request().adaptTo(Integers.class);
            assertNotNull(model);
            assertEquals(EXPECTED_INTEGER, model.getValue());
            assertEquals(payload, model.getObjectValue());
            assertEquals(EXPECTED_INTEGER, model.getConstructorValue());
            assertEquals(EXPECTED_INTEGER, model.getValueSupplier().getValue());
        }
    }

    @Test
    public void shouldInjectIntegerArray() {
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
            EXPECTED_DOUBLE_LIST)) {

            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            IntegerArrays model = context.request().adaptTo(IntegerArrays.class);
            assertNotNull(model);
            assertArrayEquals(objectArray, model.getValue());
            assertEquals(payload, model.getObjectValue());
            assertArrayEquals(objectArray, model.getConstructorValue());
            assertArrayEquals(objectArray, model.getValueSupplier().getValue());
        }

        context.request().setAttribute(ATTRIBUTE_VALUE, EXPECTED_INTEGER);
        IntegerArrays model = context.request().adaptTo(IntegerArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertEquals(EXPECTED_INTEGER, model.getValue()[0]);
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(EXPECTED_INTEGER, model.getConstructorValue()[0]);
    }

    @Test
    public void shouldInjectIntegerCollection() {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            EXPECTED_LONG_ARRAY,
            EXPECTED_LONG_LIST,
            EXPECTED_DOUBLE_ARRAY,
            EXPECTED_DOUBLE_LIST)) {

            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            IntegerCollections model = context.request().adaptTo(IntegerCollections.class);
            assertNotNull(model);
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_INTEGER_LIST, model.getValue()));
            assertEquals(payload, model.getObjectValue());
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_INTEGER_LIST, model.getConstructorValue()));
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_INTEGER_LIST, model.getValueSupplier().getValue()));
        }

        context.request().setAttribute(ATTRIBUTE_VALUE, EXPECTED_BYTE);
        IntegerCollections model = context.request().adaptTo(IntegerCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertEquals(EXPECTED_INTEGER, model.getValue().toArray()[0]);
        assertEquals(1, model.getConstructorValue().size());
        assertEquals(EXPECTED_INTEGER, model.getConstructorValue().toArray()[0]);
    }

    @Test
    public void shouldInjectLong() {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE,
            EXPECTED_INTEGER,
            EXPECTED_LONG,
            EXPECTED_FLOAT,
            EXPECTED_DOUBLE)) {

            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            Longs model = context.request().adaptTo(Longs.class);
            assertNotNull(model);
            assertEquals(EXPECTED_LONG, (long) model.getValue());
            assertEquals(payload, model.getObjectValue());
            assertEquals(EXPECTED_LONG, (long) model.getConstructorValue());
            assertEquals(EXPECTED_LONG, (long) model.getValueSupplier().getValue());
        }
    }

    @Test
    public void shouldInjectLongArray() {
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

            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            LongArrays model = context.request().adaptTo(LongArrays.class);
            assertNotNull(model);
            assertArrayEquals(objectArray, model.getValue());
            assertEquals(payload, model.getObjectValue());
            assertArrayEquals(objectArray, model.getConstructorValue());
            assertArrayEquals(objectArray, model.getValueSupplier().getValue());
        }

        context.request().setAttribute(ATTRIBUTE_VALUE, EXPECTED_INTEGER);
        LongArrays model = context.request().adaptTo(LongArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertEquals(EXPECTED_LONG, (long) model.getValue()[0]);
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(EXPECTED_LONG, (long) model.getConstructorValue()[0]);
    }

    @Test
    public void shouldInjectLongCollection() {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            EXPECTED_INTEGER_LIST,
            EXPECTED_LONG_ARRAY,
            EXPECTED_LONG_LIST,
            EXPECTED_DOUBLE_ARRAY,
            EXPECTED_DOUBLE_LIST)) {

            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            LongCollections model = context.request().adaptTo(LongCollections.class);
            assertNotNull(model);
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_LONG_LIST, model.getValue()));
            assertEquals(payload, model.getObjectValue());
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_LONG_LIST, model.getConstructorValue()));
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_LONG_LIST, model.getValueSupplier().getValue()));
        }

        context.request().setAttribute(ATTRIBUTE_VALUE, EXPECTED_BYTE);
        LongCollections model = context.request().adaptTo(LongCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertEquals(EXPECTED_LONG, model.getValue().toArray()[0]);
        assertEquals(1, model.getConstructorValue().size());
        assertEquals(EXPECTED_LONG, model.getConstructorValue().toArray()[0]);
    }

    @Test
    public void shouldInjectDouble() {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE,
            EXPECTED_INTEGER,
            EXPECTED_LONG,
            EXPECTED_FLOAT,
            EXPECTED_DOUBLE)) {

            double delta = getDelta(payload);
            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            Doubles model = context.request().adaptTo(Doubles.class);
            assertNotNull(model);
            assertEquals(EXPECTED_DOUBLE, model.getValue(), delta);
            assertEquals(payload, model.getObjectValue());
            assertEquals(EXPECTED_DOUBLE, model.getConstructorValue(), delta);
            assertEquals(EXPECTED_DOUBLE, model.getValueSupplier().getValue(), delta);
        }
    }

    @Test
    public void shouldInjectDoubleArray() {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            EXPECTED_INTEGER_LIST,
            EXPECTED_LONG_ARRAY,
            EXPECTED_LONG_LIST,
            EXPECTED_DOUBLE_ARRAY,
            EXPECTED_DOUBLE_LIST)) {

            double delta = getDelta(payload);
            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            DoubleArrays model = context.request().adaptTo(DoubleArrays.class);
            assertNotNull(model);
            assertArrayEquals(EXPECTED_DOUBLE_ARRAY, ArrayUtils.toPrimitive(model.getValue()), delta);
            assertEquals(payload, model.getObjectValue());
            assertArrayEquals(EXPECTED_DOUBLE_ARRAY, ArrayUtils.toPrimitive(model.getConstructorValue()), delta);
            assertArrayEquals(EXPECTED_DOUBLE_ARRAY, ArrayUtils.toPrimitive(model.getValueSupplier().getValue()), delta);
        }

        context.request().setAttribute(ATTRIBUTE_VALUE, EXPECTED_DOUBLE);
        DoubleArrays model = context.request().adaptTo(DoubleArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertEquals(EXPECTED_DOUBLE, model.getValue()[0], 0.0d);
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(EXPECTED_DOUBLE, model.getConstructorValue()[0], 0.0d);
    }

    @Test
    public void shouldInjectDoubleCollection() {
        for (Object payload : Arrays.asList(
            EXPECTED_BYTE_ARRAY,
            EXPECTED_INTEGER_ARRAY,
            EXPECTED_INTEGER_LIST,
            EXPECTED_LONG_ARRAY,
            EXPECTED_LONG_LIST,
            EXPECTED_DOUBLE_ARRAY,
            EXPECTED_DOUBLE_LIST)) {

            double delta = getDelta(payload);
            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            DoubleCollections model = context.request().adaptTo(DoubleCollections.class);
            assertNotNull(model);
            assertTrue(isEqualCollection(EXPECTED_DOUBLE_LIST, model.getValue(), delta));
            assertEquals(payload, model.getObjectValue());
            assertTrue(isEqualCollection(EXPECTED_DOUBLE_LIST, model.getConstructorValue(), delta));
            assertTrue(isEqualCollection(EXPECTED_DOUBLE_LIST, model.getValueSupplier().getValue(), delta));
        }

        context.request().setAttribute(ATTRIBUTE_VALUE, EXPECTED_FLOAT);
        DoubleCollections model = context.request().adaptTo(DoubleCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertEquals(EXPECTED_DOUBLE, (double) model.getValue().toArray()[0], 0.01d);
        assertEquals(1, model.getConstructorValue().size());
        assertEquals(EXPECTED_DOUBLE, (double) model.getConstructorValue().toArray()[0], 0.01d);
    }

    @Test
    public void shouldInjectBoolean() {
        context.request().setAttribute(ATTRIBUTE_VALUE, true);
        Booleans model = context.request().adaptTo(Booleans.class);
        assertNotNull(model);
        assertTrue(model.getValue());
        assertEquals(Boolean.TRUE, model.getObjectValue());
        assertTrue(model.getConstructorValue());
        assertTrue(model.getValueSupplier().getValue());
    }

    @Test
    public void shouldInjectBooleanArray() {
        for (Object payload : Arrays.asList(
            EXPECTED_BOOLEAN_ARRAY,
            ArrayUtils.toObject(EXPECTED_BOOLEAN_ARRAY))) {

            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            BooleanArrays model = context.request().adaptTo(BooleanArrays.class);
            assertNotNull(model);
            assertArrayEquals(EXPECTED_BOOLEAN_ARRAY, ArrayUtils.toPrimitive(model.getValue()));
            assertEquals(payload, model.getObjectValue());
            assertArrayEquals(EXPECTED_BOOLEAN_ARRAY, ArrayUtils.toPrimitive(model.getConstructorValue()));
            assertArrayEquals(EXPECTED_BOOLEAN_ARRAY, ArrayUtils.toPrimitive(model.getValueSupplier().getValue()));
        }

        context.request().setAttribute(ATTRIBUTE_VALUE, true);
        BooleanArrays model = context.request().adaptTo(BooleanArrays.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().length);
        assertTrue(model.getValue()[0]);
        assertEquals(1, model.getConstructorValue().length);
        assertTrue(model.getConstructorValue()[0]);
    }

    @Test
    public void shouldInjectBooleanCollection() {
        for (Object payload : Arrays.asList(
            EXPECTED_BOOLEAN_ARRAY,
            EXPECTED_BOOLEAN_LIST)) {

            context.request().setAttribute(ATTRIBUTE_VALUE, payload);
            BooleanCollections model = context.request().adaptTo(BooleanCollections.class);
            assertNotNull(model);
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_BOOLEAN_LIST, model.getValue()));
            assertEquals(payload, model.getObjectValue());
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_BOOLEAN_LIST, model.getConstructorValue()));
            assertTrue(CollectionUtils.isEqualCollection(EXPECTED_BOOLEAN_LIST, model.getValueSupplier().getValue()));
        }

        context.request().setAttribute(ATTRIBUTE_VALUE, true);
        BooleanCollections model = context.request().adaptTo(BooleanCollections.class);
        assertNotNull(model);
        assertEquals(1, model.getValue().size());
        assertTrue((boolean) model.getValue().toArray()[0]);
        assertEquals(1, model.getConstructorValue().size());
        assertTrue((boolean) model.getConstructorValue().toArray()[0]);
    }

    @Test
    public void shouldInjectToWideningType() {
        GregorianCalendar calendar = new GregorianCalendar();
        context.request().setAttribute(ATTRIBUTE_VALUE, calendar);
        Calendars model = context.request().adaptTo(Calendars.class);
        assertNotNull(model);
        assertEquals(calendar, model.getValue());
        assertEquals(calendar, model.getObjectValue());
        assertEquals(calendar, model.getConstructorValue());
        assertEquals(calendar, model.getValueSupplier().getValue());
    }

    @Test
    public void shouldInjectToWideningTypeArray() {
        GregorianCalendar calendar1 = new GregorianCalendar();
        GregorianCalendar calendar2 = (GregorianCalendar) calendar1.clone();
        Calendar[] calendars = new Calendar[] {calendar1, calendar2};

        context.request().setAttribute(ATTRIBUTE_VALUE, calendars);
        CalendarArrays model = context.request().adaptTo(CalendarArrays.class);
        assertNotNull(model);
        assertArrayEquals(calendars, model.getValue());
        assertEquals(calendars, model.getObjectValue());
        assertArrayEquals(calendars, model.getConstructorValue());
        assertArrayEquals(calendars, model.getValueSupplier().getValue());

        context.request().setAttribute(ATTRIBUTE_VALUE, calendar1);
        model = context.request().adaptTo(CalendarArrays.class);
        assertNotNull(model);
        assertEquals(calendar1, model.getValue()[0]);
        assertEquals(calendar1, model.getConstructorValue()[0]);
    }

    @Test
    public void shouldInjectToWideningTypeCollection() {
        GregorianCalendar calendar1 = new GregorianCalendar();
        GregorianCalendar calendar2 = new GregorianCalendar();
        calendar2.add(Calendar.YEAR, 1);
        Collection<Calendar> calendars = Arrays.asList(calendar1, calendar2);

        context.request().setAttribute(ATTRIBUTE_VALUE, calendars);
        CalendarCollections model = context.request().adaptTo(CalendarCollections.class);
        assertNotNull(model);
        assertTrue(CollectionUtils.isEqualCollection(calendars, model.getValue()));
        assertEquals(calendars, model.getObjectValue());
        assertTrue(CollectionUtils.isEqualCollection(calendars, model.getConstructorValue()));
        assertTrue(CollectionUtils.isEqualCollection(calendars, model.getValueSupplier().getValue()));

        context.request().setAttribute(ATTRIBUTE_VALUE, calendar1);
        model = context.request().adaptTo(CalendarCollections.class);
        assertNotNull(model);
        assertEquals(calendar1, model.getValue().toArray()[0]);
        assertEquals(calendar1, model.getObjectValue());
        assertEquals(calendar1, model.getConstructorValue().toArray()[0]);
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
