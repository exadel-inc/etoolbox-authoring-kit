package com.exadel.aem.toolkit.core.optionprovider.services.impl;

import com.day.cq.commons.jcr.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Rule;
import org.junit.Test;
import io.wcm.testing.mock.aem.junit.AemContext;

import com.exadel.aem.toolkit.core.optionprovider.services.impl.enums.TestEnum;

public class EnumOptionSourceResolverTest {
    @Rule
    public final AemContext context = new AemContext();

    public void shouldParsePathParameters() {
        String path = "com.exadel.aem.toolkit.core.optionprovider.services.impl.enums.TestEnum";
        String attributeMember = "getAttributeMember";
        String textMember = "getTextMember";
        String valueMember = "getValueMember";

        String queryString = "path1=" + path
            + "&attributeMembers=" + attributeMember
            + "&textMember1=" + textMember
            + "&valueMember1=" + valueMember;

        context.request().setQueryString(queryString);

        OptionSourceResolver resolver = new EnumOptionSourceResolver();

        PathParameters pathParameters = PathParameters.builder()
                                                .path(path)
                                                .attributeMembers(new String[]{attributeMember}, new String[]{""})
                                                .textMember(textMember, "")
                                                .valueMember(valueMember, "").build();

        Resource options = resolver.pathResolve(context.request(), pathParameters);
        int i = 0;

        for (Resource child : options.getChildren()) {
            ValueMap valueMap = child.getValueMap();
            TestEnum value = TestEnum.values()[i];
            assert value.name().equals(valueMap.get(JcrConstants.JCR_TITLE, String.class));
            assert value.toString().equals(valueMap.get("value", String.class));
            assert value.getTextMember().equals(valueMap.get(textMember, String.class));
            assert value.getValueMember().equals(valueMap.get(valueMember, String.class));
            assert value.getAttributeMember().equals(valueMap.get(attributeMember, String.class));
            i++;
        }

    }
}
