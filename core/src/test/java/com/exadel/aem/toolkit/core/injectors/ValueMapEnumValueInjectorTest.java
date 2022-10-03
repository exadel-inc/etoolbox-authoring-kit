package com.exadel.aem.toolkit.core.injectors;

import com.exadel.aem.toolkit.core.injectors.models.ITestModelValueMapEnumValue;

import io.wcm.testing.mock.aem.junit.AemContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.exadel.aem.toolkit.core.injectors.models.TestModelValueMapEnumValueInjector;

public class ValueMapEnumValueInjectorTest {

    @Rule
    public final AemContext context = new AemContext();

    private TestModelValueMapEnumValueInjector testModel;

    @Before
    public void beforeTest() {
        context.registerInjectActivateService(new ValueMapEnumValueInjector());
        context.addModelsForClasses(ValueMapEnumValueInjector.class);

        context.load().json("/com/exadel/aem/toolkit/core/injectors/enumInjector.json", "/content");
        context.request().setResource(context.resourceResolver().getResource("/content/page/jcr:content/resource"));

        testModel = context.request().adaptTo(TestModelValueMapEnumValueInjector.class);
    }

    @Test
    public void shouldInjectEnumConstant(){
        String expected = "VAL1";

        String actual = String.valueOf(testModel.getPlainOldEnumField());
        assertEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantWithNameAttribute(){
        String expected = "VAL2";

        String actual = String.valueOf(testModel.getEnumValueWithCustomName());
        assertEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantWithNameAttribute2(){
        String expected = "VAL2";

        String actual = String.valueOf(testModel.getEnumValueWithCustomName2());
        assertEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantByEnumFieldName(){
        String expected = "VAL1";

        String actual = String.valueOf(testModel.getEnumValueByFieldName());
        assertEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantByEnumFieldNameWithNameAttribute(){
        String expected = "VAL2";

        String actual = String.valueOf(testModel.getEnumValueWithCustomNameAndValue());
        assertEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantWithDefaultValueString() {
        String expected = "VAL1";

        String actual = String.valueOf(testModel.getWithDefaultValueString());
        assertEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantWithDefaultValuesArray(){
        TestModelValueMapEnumValueInjector.TestingNeededEnum[] expected = {
            TestModelValueMapEnumValueInjector.TestingNeededEnum.VAL1,
            TestModelValueMapEnumValueInjector.TestingNeededEnum.VAL2
        };


        TestModelValueMapEnumValueInjector.TestingNeededEnum[] actual = testModel.getWithDefaultValuesStringArray();
        assertArrayEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantByEnumIntegerFieldName(){
        String expected = "VAL3";

        String actual = String.valueOf(testModel.getEnumValueWithIntegerField());
        assertEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantByEnumIntFieldName(){
        String expected = "VAL3";

        String actual = String.valueOf(testModel.getEnumValueWithIntField());
        assertEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantByEnumIntFieldNameDefaultValue(){
        String expected = "VAL3";

        String actual = String.valueOf(testModel.getEnumValueWithIntegerFieldAndDefaultValue());
        assertEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantWithDefaultValuesIntArray(){
        TestModelValueMapEnumValueInjector.TestingNeededEnumWithIntegerField[] expected = {
            TestModelValueMapEnumValueInjector.TestingNeededEnumWithIntegerField.VAL2,
            TestModelValueMapEnumValueInjector.TestingNeededEnumWithIntegerField.VAL3
        };

        TestModelValueMapEnumValueInjector.TestingNeededEnumWithIntegerField[] actual = testModel.getWithDefaultValuesIntArray();

        assertArrayEquals(expected,actual);
    }

    @Test
    public void shouldInjectToMethod() {
        ITestModelValueMapEnumValue.TestingNeededEnumFromInterface expected = ITestModelValueMapEnumValue.TestingNeededEnumFromInterface.VAL1;
        String expectedStringValue = "VAL1";

        ITestModelValueMapEnumValue testInterface = context.request().adaptTo(ITestModelValueMapEnumValue.class);
        assertNotNull(testInterface);
        assertEquals(expected, testInterface.getEnumFromConstructor());
        assertEquals(expectedStringValue, testInterface.getEnumFromConstructor().toString());
    }

    @Test
    public void shouldInjectToConstructor() {
        ITestModelValueMapEnumValue.TestingNeededEnumFromInterface expected = ITestModelValueMapEnumValue.TestingNeededEnumFromInterface.VAL2;
        String expectedStringValue = "VAL2";

        ITestModelValueMapEnumValue testInterface = context.request().adaptTo(ITestModelValueMapEnumValue.class);
        assertNotNull(testInterface);
        assertEquals(expected, testInterface.getEnumFromMethod());
        assertEquals(expectedStringValue, testInterface.getEnumFromMethod().toString());
    }
}
