package com.exadel.aem.toolkit.bundle.customlists.datasource;

import com.adobe.granite.ui.components.ExpressionResolver;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ChildResourcesDatasourceTest {
    private static final String SIMPLE_LIST_PATH = "/content/simpleList";
    private static final String CUSTOM_LIST_PATH = "/content/aem-custom-lists/customList";
    private static final String DATASOURCE_PATH = "/datasource";

    @Rule
    public AemContext context = new AemContext(ResourceResolverType.JCR_OAK);

    @Mock
    private ExpressionResolver expressionResolver;

    @InjectMocks
    private ChildResourcesDatasource servlet;

    @Before
    public void setUp() {
        context.load().json("/com/exadel/aem/toolkit/bundle/customlists/util/simpleList.json", SIMPLE_LIST_PATH);
        context.load().json("/com/exadel/aem/toolkit/bundle/customlists/util/customList.json", CUSTOM_LIST_PATH);
        context.load().json("/com/exadel/aem/toolkit/bundle/customlists/datasource/datasource.json", DATASOURCE_PATH);

        Mockito.when(expressionResolver.resolve("${requestPathInfo.suffix}", Locale.US, String.class, context.request())).thenReturn("/content");
        Mockito.when(expressionResolver.resolve("${requestPathInfo.selectors[0]}", Locale.US, Integer.class, context.request())).thenReturn(0);
        Mockito.when(expressionResolver.resolve("${empty requestPathInfo.selectors[1] ? &quot;41&quot; : requestPathInfo.selectors[1] + 1}", Locale.US, Integer.class, context.request())).thenReturn(100);
    }

    @Test
    public void shouldReturnDatasourceFromContent() {
        List<String> expected = Arrays.asList("simpleList", "aem-custom-lists");

        context.request().setResource(context.resourceResolver().getResource(DATASOURCE_PATH));
        servlet.doGet(context.request(), context.response());

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        List<String> actualList = new ArrayList<>();
        Iterator<Resource> it = dataSource.iterator();
        while (it.hasNext()) {
            actualList.add(it.next().getName());
        }
        assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
        assertEquals(expected, actualList);
    }
}
