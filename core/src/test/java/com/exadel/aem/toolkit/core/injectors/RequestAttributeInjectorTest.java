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
    private static final String EXPECTED_PARAM1_VALUE = "value1";

    private static final List<String> EXPECTED_STRINGS_LIST = Arrays.asList("s1", "s2", "s3");
    private static final List<Boolean> EXPECTED_BOOLEAN_LIST = Arrays.asList(true, true, false);
    private static final List<Object> EXPECTED_INTEGER_LIST = Arrays.asList(1, 2, 3);
    private static final List<Long> EXPECTED_LONG_LIST = Arrays.asList(1L, 2L, 3L);


    private static final String[] EXPECTED_STRINGS_ARRAY = {"s1", "s2", "s3"};
    private static final Boolean[] EXPECTED_BOOLEAN_ARRAY = {true, true, false};
    private static final Integer[] EXPECTED_INTEGER_ARRAY = {1, 2, 3};
    private static final Long[] EXPECTED_LONG_ARRAY = {1L, 2L, 3L};

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
        context.request().setAttribute("sameOldStringParam", EXPECTED_PARAM1_VALUE);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);
        assertNotNull(testModel);
        assertEquals(EXPECTED_PARAM1_VALUE, testModel.getSameOldStringParam());
    }

    @Test
    public void shouldInjectNamedValue() {
        context.request().setAttribute("sameOldStringParam", EXPECTED_PARAM1_VALUE);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);
        assertNotNull(testModel);
        assertEquals(EXPECTED_PARAM1_VALUE, testModel.getNamedParam());
    }
   // @Test
    public void shouldInjectStringList() {
        context.request().setAttribute("stringsList", EXPECTED_STRINGS_LIST);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);
        assertNotNull(testModel);
        assertEquals(EXPECTED_STRINGS_LIST, testModel.getRequestAttributeStringList());
    }

   // @Test
    public void shouldInjectIntegerList() {
        context.request().setAttribute("integerList", EXPECTED_INTEGER_LIST);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);
        assertNotNull(testModel);
        assertEquals(EXPECTED_INTEGER_LIST, testModel.getRequestAttributeIntegerList());
    }
    //@Test
    public void shouldInjectLongList() {
        context.request().setAttribute("longList", EXPECTED_LONG_LIST);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);
        assertNotNull(testModel);
        assertEquals(EXPECTED_LONG_LIST, testModel.getRequestParameterLongList());
    }
   // @Test
    public void shouldInjectBooleanList() {
        context.request().setAttribute("booleanList", EXPECTED_BOOLEAN_LIST);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);
        assertNotNull(testModel);
        assertEquals(EXPECTED_BOOLEAN_LIST, testModel.getRequestAttributeBooleanList());
    }


    @Test
    public void shouldInjectStringArray() {
        context.request().setAttribute("stringArray", EXPECTED_STRINGS_ARRAY);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);
        assertNotNull(testModel);
        assertArrayEquals(EXPECTED_STRINGS_ARRAY, testModel.getRequestAttributeStringArray());
    }

    @Test
    public void shouldInjectIntegerArray() {
        context.request().setAttribute("integerArray", EXPECTED_INTEGER_ARRAY);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);
        assertNotNull(testModel);
        assertArrayEquals(EXPECTED_INTEGER_ARRAY, testModel.getRequestAttributeIntegerArray());
    }
    @Test
    public void shouldInjectLongArray() {
        context.request().setAttribute("longArray", EXPECTED_LONG_ARRAY);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);
        assertNotNull(testModel);
        assertArrayEquals(EXPECTED_LONG_ARRAY, testModel.getRequestAttributeLongArray());
    }
    @Test
    public void shouldInjectBooleanArray() {
        context.request().setAttribute("booleanArray", EXPECTED_BOOLEAN_ARRAY);
        testModel = context.request().adaptTo(TestModelRequestAttribute.class);
        assertNotNull(testModel);
        assertArrayEquals(EXPECTED_BOOLEAN_ARRAY, testModel.getRequestAttributeBooleanArray());
    }


}
