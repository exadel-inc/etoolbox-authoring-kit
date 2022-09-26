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
package com.exadel.aem.toolkit.plugin.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.WriteMode;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent1;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent2;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.RichTextEditorWidget;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRule;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

public class AnnotationUtilTest {

    @Rule
    public final PluginContextRule pluginContext = new PluginContextRule();

    @Test
    public void testIsNotDefault() throws NoSuchFieldException {
        RichTextEditor rteAnnotation = RichTextEditorWidget.class.getDeclaredField("text").getDeclaredAnnotation(RichTextEditor.class);
        Assert.assertTrue(AnnotationUtil.isNotDefault(rteAnnotation.htmlLinkRules()));

        rteAnnotation = ComplexComponent2.class.getDeclaredField("description").getDeclaredAnnotation(RichTextEditor.class);
        Assert.assertFalse(AnnotationUtil.isNotDefault(rteAnnotation.htmlLinkRules()));
    }

    @Test
    public void testGetProperty() throws NoSuchMethodException {
        Annotation aemComponentAnnotation = ComplexComponent1.class.getAnnotation(AemComponent.class);

        Method existingMethod = aemComponentAnnotation.annotationType().getDeclaredMethod("resourceSuperType");
        Object value = AnnotationUtil.getProperty(aemComponentAnnotation, existingMethod);

        Assert.assertNotNull(value);
        Assert.assertEquals("resource/super/type", value.toString());
    }

    @Test
    public void testGetPropertyDefaultValue() throws NoSuchMethodException {
        Annotation aemComponentAnnotation = ComplexComponent1.class.getAnnotation(AemComponent.class);
        Annotation dialogAnnotation = ComplexComponent1.class.getAnnotation(Dialog.class);

        Method nonExistentMethod = dialogAnnotation.annotationType().getDeclaredMethod("width");
        Object value = AnnotationUtil.getProperty(aemComponentAnnotation, nonExistentMethod, "fallback");

        Assert.assertNotNull(value);
        Assert.assertEquals("fallback", value.toString());
    }

    @Test
    public void testPropertyIsNotDefault() throws NoSuchMethodException {
        Annotation dialogAnnotation = ComplexComponent1.class.getAnnotation(Dialog.class);
        Method widthProperty = dialogAnnotation.annotationType().getDeclaredMethod("width");
        Method heightProperty = dialogAnnotation.annotationType().getDeclaredMethod("height");
        Assert.assertTrue(AnnotationUtil.propertyIsNotDefault(dialogAnnotation, widthProperty));
        Assert.assertFalse(AnnotationUtil.propertyIsNotDefault(dialogAnnotation, heightProperty));
    }

    @Test
    public void testGetNonDefaultProperties() {
        Annotation dialogAnnotation = ComplexComponent1.class.getAnnotation(Dialog.class);
        Map<String, Object> properties = AnnotationUtil.getNonDefaultProperties(dialogAnnotation);
        Assert.assertArrayEquals(
            new String[]{
                "extraClientlibs",
                "width"
            },
            properties.keySet().stream().sorted().toArray(String[]::new)
        );
    }

    @Test
    public void testCreateInstance() {
        Map<String, Object> properties = ImmutableMap
            .<String, Object>builder()
            .put("path", TestConstants.DEFAULT_COMPONENT_NAME)
            .put("title", TestConstants.DEFAULT_COMPONENT_TITLE)
            .put("writeMode", WriteMode.CREATE)
            .build();
        AemComponent annotation = AnnotationUtil.createInstance(AemComponent.class, properties);

        Assert.assertNotNull(annotation);
        Assert.assertEquals(TestConstants.DEFAULT_COMPONENT_NAME, annotation.path());
        Assert.assertEquals(TestConstants.DEFAULT_COMPONENT_TITLE, annotation.title());
        Assert.assertEquals(WriteMode.CREATE, annotation.writeMode());
        Assert.assertEquals(StringUtils.EMPTY, annotation.description());
        Assert.assertArrayEquals(
            new String[]{
                "path",
                "title",
                "writeMode"
            },
            AnnotationUtil.getNonDefaultProperties(annotation).keySet().stream().sorted().toArray(String[]::new)
        );
    }

    @Test
    public void testCreateInstanceWithSource() {
        AemComponent source = ComplexComponent1.class.getAnnotation(AemComponent.class);
        Map<String, Object> properties = ImmutableMap
            .<String, Object>builder()
            .put("path", "test-path-2")
            .put("title", "Test Component 2")
            .build();
        AemComponent annotation = AnnotationUtil.createInstance(AemComponent.class, source, properties);

        Assert.assertNotNull(annotation);
        Assert.assertEquals("test-path-2", annotation.path());
        Assert.assertEquals("Test Component 2", annotation.title());
        Assert.assertEquals("test component", annotation.description());
        Assert.assertEquals(WriteMode.OPEN, annotation.writeMode());
    }

    @Test
    public void testFilterInstance() {
        AemComponent source = ComplexComponent1.class.getAnnotation(AemComponent.class);
        AemComponent annotation = AnnotationUtil.filterInstance(
            source,
            Arrays.asList("title", "description", "disableTargeting"));

        Assert.assertNotNull(annotation);
        Assert.assertEquals(TestConstants.DEFAULT_COMPONENT_NAME, annotation.path());
        Assert.assertEquals(StringUtils.EMPTY, annotation.title());
        Assert.assertEquals(StringUtils.EMPTY, annotation.description());
        Assert.assertFalse(annotation.disableTargeting());
        Assert.assertTrue(annotation.isContainer());
    }

    @Test
    public void testGetPropertyMappingFilter() throws NoSuchMethodException {
        AemComponent annotation = ComplexComponent1.class.getAnnotation(AemComponent.class);
        Predicate<Method> filter = AnnotationUtil.getPropertyMappingFilter(annotation);

        Method titleProperty = annotation.annotationType().getDeclaredMethod("title");
        Method isContainerProperty = annotation.annotationType().getDeclaredMethod("isContainer");
        Method pathProperty = annotation.annotationType().getDeclaredMethod("path");
        Method writeModeProperty = annotation.annotationType().getDeclaredMethod("writeMode");

        Assert.assertTrue(filter.test(titleProperty));
        Assert.assertTrue(filter.test(isContainerProperty));
        Assert.assertFalse(filter.test(pathProperty));
        Assert.assertFalse(filter.test(writeModeProperty));
    }
}

