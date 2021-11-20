package com.exadel.aem.toolkit.core.injectors;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.core.injectors.models.ITestModelRequestParam;
import com.exadel.aem.toolkit.core.injectors.models.TestModelRequestParam;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RequestParameterInjectorTest {

    @Rule
    public final AemContext context = new AemContext();

    private TestModelRequestParam testModel;

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelRequestParam.class);
        context.registerInjectActivateService(new RequestParamInjector());
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("paramName", "value");
        requestMap.put("also", "another");
        context.request().setParameterMap(requestMap);
        testModel = context.request().adaptTo(TestModelRequestParam.class);
    }

    @Test
    public void getRequestParameterString_shouldReturnParamValue() {
        String expectedParamName = context.request().getParameter("paramName");

        assertNotNull(testModel);
        assertEquals(expectedParamName, testModel.getParamName());
    }

    @Test
    public void getParamNameFromAnnotationName_shouldReturnParamValue() {
        String expectedParamName = context.request().getParameter("also");

        assertNotNull(testModel);
        assertEquals(expectedParamName, testModel.getParamNameFromAnnotationName());
    }

    @Test
    public void getParamObjectType_shouldReturnParamValueObjectType() {
        Object expectedParam = context.request().getParameter("also");

        assertNotNull(testModel);
        assertEquals(expectedParam, testModel.getParamObjectType());
    }

    @Test
    public void getParamRequestParamType_shouldReturnParamValueRequestParamType() {
        RequestParameter expectedParam = context.request().getRequestParameter("also");

        assertNotNull(testModel);
        assertEquals(expectedParam, testModel.getParamRequestParamType());
    }

    @Test
    public void getParamRequestParamArray_shouldReturnRequestParamArray() {
        RequestParameter[] expectedParam = context.request().getRequestParameters("also");

        assertNotNull(testModel);
        assertArrayEquals(expectedParam, testModel.getParamRequestParamArray());
    }

    @Test
    public void getParamRequestParameterMapType_shouldReturnRequestParamMap() {
        RequestParameterMap expectedParam = context.request().getRequestParameterMap();

        assertNotNull(testModel);
        assertEquals(expectedParam, testModel.getParamRequestParameterMapType());
    }

    @Test
    public void getParamList_shouldReturnRequestParamList() {
        List<RequestParameter> expectedParam = context.request().getRequestParameterList();

        assertNotNull(testModel);
        assertEquals(expectedParam, testModel.getParamList());
    }

    @Test
    public void getRequestParamFromMethod_shouldReturnRequestParamList() {
        String expectedParamName = context.request().getParameter("also");

        assertNotNull(testModel);
        assertEquals(expectedParamName, testModel.getParamRequestFromMethodParameter());
    }

    @Test
    public void shouldReturnRequestParamString() {
        ITestModelRequestParam testModel = context.request().adaptTo(ITestModelRequestParam.class);
        String expectedParamName = context.request().getParameter("also");

        assertNotNull(testModel);
        assertEquals(expectedParamName, testModel.getRequestParamFromMethod());
    }

    @Test
    public void shouldReturnNullIfTypeIsWrong() {
        assertNotNull(testModel);
        assertNull(testModel.getParamListString());
        assertNull(testModel.getParamSet());
        assertNull(testModel.getParamRequestParamArrayString());
    }
}
