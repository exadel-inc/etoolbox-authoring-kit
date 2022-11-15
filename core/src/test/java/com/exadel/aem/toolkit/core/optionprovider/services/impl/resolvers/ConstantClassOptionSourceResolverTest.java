package com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import com.day.cq.commons.jcr.JcrConstants;
import io.wcm.testing.mock.aem.junit.AemContext;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionSourceResolutionResult;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.PathParameters;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.constantClasses.TestConstantClass;

public class ConstantClassOptionSourceResolverTest {
    @Rule
    public final AemContext context = new AemContext();

    public OptionSourceResolver resolver;

    @Test
    public void shouldResolveConstantClassWithParams() throws IllegalAccessException {
        String path = "com.exadel.aem.toolkit.core.optionprovider.services.impl.constantClasses.TestConstantEnum";
        String textMember = "TEXT_*";
        String valueMember = "VALUE_*";

        String queryString = "path1=" + path
            + "&textMember1=" + textMember
            + "&valueMember1=" + valueMember;

        context.request().setQueryString(queryString);

        PathParameters pathParameters = PathParameters.builder()
            .path(path)
            .textMember(textMember, StringUtils.EMPTY)
            .valueMember(valueMember, StringUtils.EMPTY)
            .build();

        resolver = new ClassOptionSourceResolver(TestConstantClass.class);

        OptionSourceResolutionResult result = resolver.resolve(context.request(), pathParameters, pathParameters.getPath());

        for (Resource resource : result.getDataResource().getChildren()) {
            ValueMap valueMap = resource.getValueMap();

            Field field = Arrays.stream(TestConstantClass.class.getDeclaredFields())
                .filter(f -> f.getName().equals(StringUtils.substringBeforeLast(textMember, CoreConstants.WILDCARD)
                    + valueMap.get(CoreConstants.PARAMETER_NAME)))
                .findFirst()
                .orElse(null);

            Assert.assertNotNull(field);
            Assert.assertEquals(field.get(null), valueMap.get(JcrConstants.JCR_TITLE));

            field = Arrays.stream(TestConstantClass.class.getDeclaredFields())
                .filter(f -> f.getName().equals(StringUtils.substringBeforeLast(valueMember, CoreConstants.WILDCARD)
                    + valueMap.get(CoreConstants.PARAMETER_NAME)))
                .findFirst()
                .orElse(null);

            Assert.assertNotNull(field);
            Assert.assertEquals(field.get(null), valueMap.get(CoreConstants.PN_VALUE));
        }
    }

    @Test
    public void shouldResolveConstantClassWithDefaultParams() throws IllegalAccessException {
        String path = "com.exadel.aem.toolkit.core.optionprovider.services.impl.constantClasses.TestConstantEnum";

        String queryString = "path1=" + path;

        context.request().setQueryString(queryString);

        PathParameters pathParameters = PathParameters.builder()
            .path(path)
            .build();

        resolver = new ClassOptionSourceResolver(TestConstantClass.class);

        OptionSourceResolutionResult result = resolver.resolve(context.request(), pathParameters, pathParameters.getPath());

        for (Resource resource : result.getDataResource().getChildren()) {
            ValueMap valueMap = resource.getValueMap();

            Field field = Arrays.stream(TestConstantClass.class.getDeclaredFields())
                .filter(f -> f.getName().equals(valueMap.get(CoreConstants.PARAMETER_NAME)))
                .findFirst()
                .orElse(null);

            Assert.assertNotNull(field);
            Assert.assertEquals(field.getName(), valueMap.get(JcrConstants.JCR_TITLE));
            Assert.assertEquals(field.get(null), valueMap.get(CoreConstants.PN_VALUE));
        }
    }
}
