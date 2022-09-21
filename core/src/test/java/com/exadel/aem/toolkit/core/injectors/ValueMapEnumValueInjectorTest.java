package com.exadel.aem.toolkit.core.injectors;

import io.wcm.testing.mock.aem.junit.AemContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    public void shouldInjectEnumConstantByEnumFieldName(){
        String expected = "VAL1";

        String actual = String.valueOf(testModel.getEnumValueByFieldName());
        assertEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantWithNameAttribute(){
        String expected = "VAL2";

        String actual = String.valueOf(testModel.getEnumValueWithCustomName());
        assertEquals(expected,actual);
    }

    @Test
    public void shouldInjectEnumConstantByEnumFieldNameWithNameAttribute(){
        String expected = "VAL2";

        String actual = String.valueOf(testModel.getEnumValueWithCustomNameAndValue());
        assertEquals(expected,actual);
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
}
