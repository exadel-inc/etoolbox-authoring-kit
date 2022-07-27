package com.exadel.aem.toolkit.core.injectors;

import io.wcm.testing.mock.aem.junit.AemContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import com.exadel.aem.toolkit.core.injectors.models.TestModelRequestAttribute;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class RequestAttributeInjectorTest {
    private static final String EXPECTED_PARAM = "same old string value";

    private static final List<String> EXPECTED_STRINGS_LIST = Arrays.asList("s1", "s2", "s3");
    private static final List<Boolean> EXPECTED_BOOLEAN_LIST = Arrays.asList(true, true, false);
    private static final List<Object> EXPECTED_INTEGER_LIST = Arrays.asList(1, 2, 3);
    private static final List<Long> EXPECTED_LONG_LIST = Arrays.asList(1L, 2L, 3L);


    private static final Object[]  STRING_ARRAY_FROM_REQUEST_IMITATION = {"wake", "up", "Neo"};

    private static final Object[]  LONG_ARRAY_FROM_REQUEST_IMITATION = {1L, 2L, 3L};

    private static final Object[]  INTEGER_ARRAY_FROM_REQUEST_IMITATION = {1, 2, 3};

    private static final Object[]  BOOLEAN_ARRAY_FROM_REQUEST_IMITATION = {true, true, false};


    @Rule
    public final AemContext context = new AemContext();

    private TestModelRequestAttribute testModel;

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelRequestAttribute.class);
        context.registerInjectActivateService(new RequestAttributeInjector());
    }

    @Test
    public void shouldInjectValue() {

        context.request().setAttribute("sameOldStringParam", EXPECTED_PARAM);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertEquals(EXPECTED_PARAM, testModel.getSameOldStringParam());
    }

    @Test
    public void shouldInjectNamedValue() {
        context.request().setAttribute("sameOldStringParam", EXPECTED_PARAM);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);
        assertNotNull(testModel);
        assertEquals(EXPECTED_PARAM, testModel.getNamedParam());
    }
    @Test
    public void shouldInjectStringList() {

        context.request().setAttribute("stringsList", EXPECTED_STRINGS_LIST);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertEquals(EXPECTED_STRINGS_LIST, testModel.getRequestAttributeStringList());
    }

    @Test
    public void shouldInjectIntegerList() {
        context.request().setAttribute("integerList", EXPECTED_INTEGER_LIST);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertEquals(EXPECTED_INTEGER_LIST, testModel.getRequestAttributeIntegerList());
    }
    @Test
    public void shouldInjectLongList() {

        context.request().setAttribute("longList", EXPECTED_LONG_LIST);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertEquals(EXPECTED_LONG_LIST, testModel.getRequestParameterLongList());
    }
    @Test
    public void shouldInjectBooleanList() {

        context.request().setAttribute("booleanList", EXPECTED_BOOLEAN_LIST);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertEquals(EXPECTED_BOOLEAN_LIST, testModel.getRequestAttributeBooleanList());
    }


    @Test
    public void shouldInjectStringArray() {
        String[] EXPECTED_STRING_ARRAY = {"wake", "up", "Neo"};

        context.request().setAttribute("stringArray", STRING_ARRAY_FROM_REQUEST_IMITATION);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertArrayEquals(EXPECTED_STRING_ARRAY, testModel.getRequestAttributeStringArray());
    }

    @Test
    public void shouldInjectIntegerArrayWrapped() {
        Integer[] EXPECTED_INTEGER_ARRAY_WRAPPED = {1, 2, 3};

        context.request().setAttribute("wrappedIntegerArray", INTEGER_ARRAY_FROM_REQUEST_IMITATION);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertArrayEquals(EXPECTED_INTEGER_ARRAY_WRAPPED, testModel.getRequestAttributeIntegerArrayWrapped());
    }
    @Test
    public void shouldInjectIntegerArrayNotWrapped() {
        final int[] EXPECTED_INTEGER_ARRAY_NOT_WRAPPED = {1, 2, 3};

        context.request().setAttribute("notWrappedIntegerArray", INTEGER_ARRAY_FROM_REQUEST_IMITATION);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertArrayEquals(EXPECTED_INTEGER_ARRAY_NOT_WRAPPED, testModel.getRequestAttributeIntegerArrayNotWrapped());
    }
    @Test
    public void shouldInjectLongArrayWrapped() {
        final Long[] EXPECTED_LONG_ARRAY_WRAPPED = {1L, 2L, 3L};

        context.request().setAttribute("wrappedLongArray", LONG_ARRAY_FROM_REQUEST_IMITATION);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertArrayEquals(EXPECTED_LONG_ARRAY_WRAPPED, testModel.getRequestAttributeLongArrayWrapped());
    }
    @Test
    public void shouldInjectLongArrayNotWrapped() {
        final long[] EXPECTED_LONG_ARRAY_NO_WRAPPED = {1L, 2L, 3L};

        context.request().setAttribute("notWrappedLongArray", LONG_ARRAY_FROM_REQUEST_IMITATION);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertArrayEquals(EXPECTED_LONG_ARRAY_NO_WRAPPED, testModel.getRequestAttributeLongArrayNotWrapped());
    }

    @Test
    public void shouldInjectBooleanArrayWrapped() {
        final Boolean[] EXPECTED_BOOLEAN_ARRAY_WRAPPED = {true, true, false};

        context.request().setAttribute("wrappedBooleanArray", BOOLEAN_ARRAY_FROM_REQUEST_IMITATION);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertArrayEquals(EXPECTED_BOOLEAN_ARRAY_WRAPPED, testModel.getRequestAttributeBooleanArrayWrapped());
    }
    @Test
    public void shouldInjectBooleanArrayNotWrapped() {
        final boolean[] EXPECTED_BOOLEAN_ARRAY_NOT_WRAPPED = {true, true, false};

        context.request().setAttribute("notWrappedBooleanArray", BOOLEAN_ARRAY_FROM_REQUEST_IMITATION);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);

        assertNotNull(testModel);
        assertArrayEquals(EXPECTED_BOOLEAN_ARRAY_NOT_WRAPPED, testModel.getRequestAttributeBooleanArrayNotWrapped());
    }


}
