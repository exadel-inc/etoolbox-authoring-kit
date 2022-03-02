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

public class RequestParamInjectorTest {
    private static final String EXPECTED_PARAM1_VALUE = "value1";
    private static final String EXPECTED_PARAM2_VALUE = "value2";

    @Rule
    public final AemContext context = new AemContext();

    private TestModelRequestParam testModel;

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelRequestParam.class);
        context.registerInjectActivateService(new RequestParamInjector());

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("param1", EXPECTED_PARAM1_VALUE);
        requestMap.put("param2", EXPECTED_PARAM2_VALUE);
        context.request().setParameterMap(requestMap);

        testModel = context.request().adaptTo(TestModelRequestParam.class);
    }

    @Test
    public void shouldInjectValue() {
        assertNotNull(testModel);
        assertEquals(EXPECTED_PARAM1_VALUE, testModel.getParam1());
    }

    @Test
    public void shouldInjectNamedValue() {
        assertEquals(EXPECTED_PARAM2_VALUE, testModel.getNamedParam());
    }

    @Test
    public void shouldInjectValueObject() {
        assertEquals(EXPECTED_PARAM2_VALUE, testModel.getParamObjectType());
    }

    @Test
    public void shouldInjectRequestParameter() {
        RequestParameter expected = context.request().getRequestParameter("param1");
        assertEquals(expected, testModel.getRequestParameter());
    }

    @Test
    public void shouldInjectRequestParameterArray() {
        RequestParameter[] expected = context.request().getRequestParameters("param2");
        assertArrayEquals(expected, testModel.getRequestParameterArray());
    }

    @Test
    public void shouldInjectRequestParameterStringArray() {
        RequestParameter[] expected = context.request().getRequestParameters("param2");
        String[] actual = testModel.getRequestParameterStringArray();
        assertNotNull(expected);
        assertNotNull(actual);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].getString(), actual[i]);
        }
    }

    @Test
    public void shouldInjectRequestParameterList() {
        List<RequestParameter> expected = context.request().getRequestParameterList();
        List<RequestParameter> actual = testModel.getRequestParameterList();
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getString(), actual.get(i).getString());
        }
    }

    @Test
    public void shouldInjectRequestParameterStringList() {
        RequestParameter[] expected = context.request().getRequestParameters("param2");
        List<String> actual = testModel.getRequestParameterStringList();
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].getString(), actual.get(i));
        }
    }
    @Test
    public void shouldInjectRequestParameterMap() {
        RequestParameterMap expected = context.request().getRequestParameterMap();
        assertEquals(expected, testModel.getRequestParameterMap());
    }

    @Test
    public void shouldInjectIntoMethodParameter() {
        assertEquals(EXPECTED_PARAM2_VALUE, testModel.getParamRequestFromMethodParameter());
    }

    @Test
    public void shouldInjectToMethod() {
        ITestModelRequestParam testInterface = context.request().adaptTo(ITestModelRequestParam.class);
        assertNotNull(testInterface);
        assertEquals(EXPECTED_PARAM2_VALUE, testInterface.getRequestParamFromMethod());
    }

    @Test
    public void shouldNotInjectIfParamMissingOrWrongType() {
        assertNull(testModel.getParamStringWrongName());
        assertNull(testModel.getParamSet());
        assertNull(testModel.getParamListWrongType());
    }
}
