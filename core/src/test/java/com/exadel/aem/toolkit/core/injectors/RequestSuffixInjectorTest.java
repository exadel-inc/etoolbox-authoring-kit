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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.models.requestproperty.Resources;

public class RequestSuffixInjectorTest extends RequestPropertyInjectorTestBase {
    private static final String DEFAULT_SUFFIX = "/content/jcr:content/foo";

    /* -----------
       Preparation
       ----------- */

    @Before
    public void beforeTest() {
        super.beforeTest();
        context.load().json("/com/exadel/aem/toolkit/core/injectors/suffixInjector.json", "/content");
    }

    @Override
    BaseInjector<?> prepareInjector() {
        return new RequestSuffixInjector();
    }

    @Override
    void prepareRequest(MockSlingHttpServletRequest request, Object payload) {
        ((MockRequestPathInfo) request.getRequestPathInfo()).setSuffix(payload != null ? payload.toString() : StringUtils.EMPTY);
    }

    /* -----
       Tests
       ----- */

    @Test
    public void shouldInjectString() {
        super.shouldInjectString(model -> assertNull(model.getObjectValue()));
    }

    @Test
    public void shouldInjectResource() {
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(DEFAULT_SUFFIX);
        Resources model = context.request().adaptTo(Resources.class);
        assertNotNull(model);

        assertNotNull(model.getValue());
        assertEquals(DEFAULT_SUFFIX, model.getValue().getPath());
        assertEquals("This is a test", model.getValue().getValueMap().get(CoreConstants.PN_TEXT, String.class));

        assertNotNull(model.getConstructorValue());
        assertEquals(1, model.getConstructorValue().length);
        assertEquals(model.getValue().getPath(), model.getConstructorValue()[0].getPath());

        assertNotNull(model.getValueSupplier().getValue());
        assertEquals(1, model.getValueSupplier().getValue().size());
        assertEquals(model.getValue().getPath(), model.getValueSupplier().getValue().iterator().next().getPath());
    }

    @Test
    public void shouldNotCauseExceptionWhenPayloadMissing() {
        super.shouldNotCauseExceptionWhenPayloadMissing();
    }

/*
    @Test
    public void shouldInjectSuffix() {
        context.requestPathInfo().setSuffix(SUFFIX_1);
        TestModelSuffix testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertEquals(SUFFIX_1, testModel.getSuffix());
    }

    @Test
    public void shouldInjectSuffixObject() {
        context.requestPathInfo().setSuffix(SUFFIX_2);
        TestModelSuffix testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertEquals(SUFFIX_2, testModel.getSuffixObject());
    }

    @Test
    public void shouldInjectSuffixResource() {
        context.requestPathInfo().setSuffix("/content/test/jcr:content/foo");
        TestModelSuffix testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertNotNull(testModel.getSuffixResource());
        assertEquals("This is a test", testModel.getSuffixResource().getValueMap().get("text", StringUtils.EMPTY));
    }

    @Test
    public void shouldInjectIntoMethodParameter() {
        context.requestPathInfo().setSuffix(SUFFIX_2);
        TestModelSuffix testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertEquals(SUFFIX_2, testModel.getSuffixFromParameter());
    }

    @Test
    public void shouldInjectIntoMethod() {
        context.requestPathInfo().setSuffix(SUFFIX_1);
        ITestModelSuffix testModel = context.request().adaptTo(ITestModelSuffix.class);
        assertNotNull(testModel);

        String actualSuffix = testModel.getSuffixFromMethod();
        assertEquals(SUFFIX_1, actualSuffix);
    }

    @Test
    public void shouldNotInjectIfSuffixMissing() {
        TestModelSuffix testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertNull(testModel.getSuffix());
    }

    @Test
    public void shouldNotInjectIfResourceMissing() {
        context.requestPathInfo().setSuffix("/nonexistent");
        TestModelSuffix testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertNull(testModel.getSuffixResource());
    }

    @Test
    public void shouldNotInjectIfWrongType() {
        context.requestPathInfo().setSuffix("/type");
        TestModelSuffix testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertNull(testModel.getSuffixList());
        assertEquals(0, testModel.getSuffixInt());
    }*/
}
