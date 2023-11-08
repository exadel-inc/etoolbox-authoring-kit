package com.exadel.aem.toolkit.core.injectors;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.models.RequestAdapterBase;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.Booleans;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.CalendarArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.CalendarCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.Calendars;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.DoubleArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.DoubleCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.Doubles;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.IntegerArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.IntegerCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.Integers;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.LongArrays;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.LongCollections;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.Longs;

public class RequestAttributeInjectorTest extends RequestPropertyInjectorTestBase {

    private static final String[] STRINGIFIED_FLOAT_ARRAY = new String[] {"-42.1", "43.2d", "NaN", "44"};
    private static final List<String> STRINGIFIED_FLOAT_COLLECTION = Arrays.asList(STRINGIFIED_FLOAT_ARRAY);

    private static final Integer[] EXPECTED_INTEGER_ARRAY = new Integer[] {-42, 43, 44};
    private static final List<Integer> EXPECTED_INTEGER_COLLECTION = Arrays.asList(EXPECTED_INTEGER_ARRAY);

    /* -----------
       Preparation
       ----------- */

    @Override
    BaseInjector<?> prepareInjector() {
        return new RequestAttributeInjector();
    }

    @Override
    void prepareRequest(MockSlingHttpServletRequest request, Object payload) {
        if (payload != null) {
            request.setAttribute(CoreConstants.PN_VALUE, payload);
        } else {
            request.removeAttribute(CoreConstants.PN_VALUE);
        }
    }

    /* -----
       Tests
       ----- */

    @Test
    public void shouldInjectString() {
        super.shouldInjectString();
    }

    @Test
    public void shouldInjectDefaultString() {
        super.shouldInjectDefaultString();
    }

    @Test
    public void shouldInjectStringArray() {
        super.shouldInjectStringArray();
    }

    @Test
    public void shouldInjectDefaultStringArray() {
        super.shouldInjectDefaultStringArray();
    }

    @Test
    public void shouldInjectStringCollection() {
        super.shouldInjectStringCollection();
    }

    @Test
    public void shouldInjectInteger() {
        super.shouldInjectInteger(RequestAttributeInjectorTest::assertStringifiedObjectValueEquals);
    }

    @Test
    public void shouldInjectDefaultInteger() {
        super.shouldInjectDefaultInteger();
    }

    @Test
    public void shouldInjectIntegerArray() {
        super.shouldInjectIntegerArray();
    }

    @Test
    public void shouldInjectDefaultIntegerArray() {
        super.shouldInjectDefaultIntegerArray();
    }

    @Test
    public void shouldInjectIntegerCollection() {
        super.shouldInjectIntegerCollection();
    }

    @Test
    public void shouldInjectUnparseableIntegerCollection() {
        super.shouldInjectUnparseableIntegerCollection();
    }

    @Test
    public void shouldInjectLong() {
        super.shouldInjectLong(RequestAttributeInjectorTest::assertStringifiedObjectValueEquals);
    }

    @Test
    public void shouldInjectDefaultLong() {
        super.shouldInjectDefaultLong();
    }

    @Test
    public void shouldInjectLongArray() {
        super.shouldInjectLongArray();
    }

    @Test
    public void shouldInjectDefaultLongArray() {
        super.shouldInjectDefaultLongArray();
    }

    @Test
    public void shouldInjectLongCollection() {
        super.shouldInjectLongCollection();
    }

    @Test
    public void shouldInjectUnparseableLongCollection() {
        super.shouldInjectUnparseableLongCollection();
    }

    @Test
    public void shouldInjectDouble() {
        super.shouldInjectDouble(RequestAttributeInjectorTest::assertStringifiedObjectValueEquals);
    }

    @Test
    public void shouldInjectDefaultDouble() {
        super.shouldInjectDefaultDouble();
    }

    @Test
    public void shouldInjectDoubleArray() {
        super.shouldInjectDoubleArray();
    }

    @Test
    public void shouldInjectDefaultDoubleArray() {
        super.shouldInjectDefaultDoubleArray();
    }

    @Test
    public void shouldInjectDoubleCollection() {
        super.shouldInjectDoubleCollection();
    }

    @Test
    public void shouldInjectUnparseableDoubleCollection() {
        super.shouldInjectUnparseableDoubleCollection();
    }

    @Test
    public void shouldInjectBoolean() {
        super.shouldInjectBoolean();
    }

    @Test
    public void shouldInjectDefaultBoolean() {
        super.shouldInjectDefaultBoolean();
    }

    @Test
    public void shouldInjectBooleanArray() {
        super.shouldInjectBooleanArray();
    }

    @Test
    public void shouldInjectDefaultBooleanArray() {
        super.shouldInjectDefaultBooleanArray();
    }

    @Test
    public void shouldInjectBooleanCollection() {
        super.shouldInjectBooleanCollection();
    }

    @Test
    public void shouldInjectToWideningType() {
        GregorianCalendar calendar = new GregorianCalendar();
        prepareRequest(context.request(), calendar);
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

        prepareRequest(context.request(), calendars);
        CalendarArrays model = context.request().adaptTo(CalendarArrays.class);
        assertNotNull(model);
        assertArrayEquals(calendars, model.getValue());
        assertEquals(calendars, model.getObjectValue());
        assertArrayEquals(calendars, model.getConstructorValue());
        assertArrayEquals(calendars, model.getValueSupplier().getValue());

        prepareRequest(context.request(), calendar1);
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

        prepareRequest(context.request(), calendars);
        CalendarCollections model = context.request().adaptTo(CalendarCollections.class);
        assertNotNull(model);
        assertTrue(CollectionUtils.isEqualCollection(calendars, model.getValue()));
        assertEquals(calendars, model.getObjectValue());
        assertTrue(CollectionUtils.isEqualCollection(calendars, model.getConstructorValue()));
        assertTrue(CollectionUtils.isEqualCollection(calendars, model.getValueSupplier().getValue()));

        prepareRequest(context.request(), calendar1);
        model = context.request().adaptTo(CalendarCollections.class);
        assertNotNull(model);
        assertEquals(calendar1, model.getValue().toArray()[0]);
        assertEquals(calendar1, model.getObjectValue());
        assertEquals(calendar1, model.getConstructorValue().toArray()[0]);
    }

    @Test
    public void shouldInterpretStringIntoPrimitive() {
        prepareRequest(context.request(), STRINGIFIED_FLOAT_ARRAY[0]);
        for (Class<? extends RequestAdapterBase<? extends Number>> modelClass : Arrays.asList(Integers.class, Longs.class, Doubles.class)) {
            RequestAdapterBase<? extends Number> model = context.request().adaptTo(modelClass);
            assertNotNull(model);
            assertEquals((int) EXPECTED_INTEGER_ARRAY[0], model.getValue().intValue());
        }
        prepareRequest(context.request(), Boolean.TRUE.toString());
        RequestAdapterBase<Boolean> model = context.request().adaptTo(Booleans.class);
        assertNotNull(model);
        assertTrue(model.getValue());
    }

    @Test
    public void shouldInterpretStringsIntoNumberArray() {
        prepareRequest(context.request(), STRINGIFIED_FLOAT_ARRAY);
        for (Class<? extends RequestAdapterBase<? extends Number[]>> modelClass :
            Arrays.asList(IntegerArrays.class, LongArrays.class, DoubleArrays.class)) {

            RequestAdapterBase<? extends Number[]> model = context.request().adaptTo(modelClass);
            assertNotNull(model);
            Number[] values = model.getValue();
            Number[] constructorValues = model.getConstructorValue();
            for (int i = 0; i < values.length; i++) {
                assertEquals((int) EXPECTED_INTEGER_ARRAY[i], values[i].intValue());
                assertEquals((int) EXPECTED_INTEGER_ARRAY[i], constructorValues[i].intValue());
            }
        }
    }

    @Test
    public void shouldInterpretStringsIntoNumberCollection() {
        prepareRequest(context.request(), STRINGIFIED_FLOAT_COLLECTION);
        for (Class<? extends RequestAdapterBase<? extends Collection<? extends Number>>> modelClass :
            Arrays.asList(IntegerCollections.class, LongCollections.class, DoubleCollections.class)) {

            RequestAdapterBase<? extends Collection<? extends Number>> model = context.request().adaptTo(modelClass);
            assertNotNull(model);
            Collection<? extends Number> values = model.getValue();
            Collection<? extends Number> constructorValues = model.getConstructorValue();
            for (int i = 0; i < values.size(); i++) {
                int value = Optional.ofNullable(IterableUtils.get(values, i)).map(Number::intValue).orElse(0);
                Integer constructorValue = Optional
                    .ofNullable(IterableUtils.get(constructorValues, i))
                    .map(Number::intValue).orElse(null);
                assertTrue(
                    EXPECTED_INTEGER_COLLECTION.get(i) == null
                        && value == 0
                        || EXPECTED_INTEGER_COLLECTION.get(i) == value);
                assertEquals(EXPECTED_INTEGER_COLLECTION.get(i), constructorValue);
            }
        }
    }

    @Test
    public void shouldNotCauseExceptionWhenPayloadMissing() {
        super.shouldNotCauseExceptionWhenPayloadMissing();
        assertNotNull(context.request().adaptTo(Calendars.class));
        assertNotNull(context.request().adaptTo(CalendarArrays.class));
        assertNotNull(context.request().adaptTo(CalendarCollections.class));
    }

    /* ---------------
       Service methods
       --------------- */

    private static void assertStringifiedObjectValueEquals(RequestAdapterBase<?> model, Object payload) {
        assertNotNull(model.getObjectValue());
        if (ClassUtils.isPrimitiveOrWrapper(model.getObjectValue().getClass()) && payload instanceof String) {
            assertEquals(NumberUtils.createNumber(payload.toString()), model.getObjectValue());
        } else {
            assertEquals(payload, model.getObjectValue());
        }
    }
}
