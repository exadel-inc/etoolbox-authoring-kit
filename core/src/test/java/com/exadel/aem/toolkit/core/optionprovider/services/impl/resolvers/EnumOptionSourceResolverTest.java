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
package com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import com.day.cq.commons.jcr.JcrConstants;
import io.wcm.testing.mock.aem.junit.AemContext;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.enums.TestEnum;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.enums.TestEnumWithoutParams;

public class EnumOptionSourceResolverTest {
    @Rule
    public final AemContext context = new AemContext();

    public OptionSourceResolver resolver;

    @Test
    public void shouldResolveEnumWithParams() {
        String path = "com.exadel.aem.toolkit.core.optionprovider.services.impl.enums.TestEnum";
        String attributeMember = "getAttributeMember";
        String textMember = "getTextMember";
        String valueMember = "getValueMember";

        String queryString = "path1=" + path
            + "&attributeMembers=" + attributeMember
            + "&textMember1=" + textMember
            + "&valueMember1=" + valueMember;

        context.request().setQueryString(queryString);

        resolver = new ClassOptionSourceResolver(TestEnum.class);

        Resource options = resolver.resolve(context.request(), null, path).getDataResource();

        int i = 0;
        for (Resource child : options.getChildren()) {
            ValueMap valueMap = child.getValueMap();
            TestEnum value = TestEnum.values()[i];
            Assert.assertEquals(value.getTextMember(), valueMap.get(textMember, String.class));
            Assert.assertEquals(value.getValueMember(), valueMap.get(valueMember, String.class));
            Assert.assertEquals(value.getAttributeMember(), valueMap.get(attributeMember, String.class));
            i++;
        }
    }

    @Test
    public void shouldResolveEnumWithDefaultParams() {
        String path = "com.exadel.aem.toolkit.core.optionprovider.services.impl.enums.TestEnumWithoutParams";
        String attributeMember = "unexistingMethodName";

        String queryString = "path1=" + path
            + "&attributeMembers=" + attributeMember;

        context.request().setQueryString(queryString);

        resolver = new ClassOptionSourceResolver(TestEnumWithoutParams.class);

        Resource options = resolver.resolve(context.request(), null, path).getDataResource();

        int i = 0;
        for (Resource child : options.getChildren()) {
            ValueMap valueMap = child.getValueMap();
            TestEnumWithoutParams value = TestEnumWithoutParams.values()[i];
            Assert.assertEquals(value.name(), valueMap.get(JcrConstants.JCR_TITLE, String.class));
            Assert.assertEquals(value.toString(), valueMap.get(CoreConstants.PN_VALUE, String.class));
            Assert.assertNull(valueMap.get(attributeMember, String.class));
            i++;
        }
    }
}
