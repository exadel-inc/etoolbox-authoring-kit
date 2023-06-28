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
package com.exadel.aem.toolkit.plugin.sources;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.policies.AllowedChildren;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent1;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ScriptedFieldset;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.viewpattern.component1.views.DesignDialogView;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.policies.AllowedChildrenTestCases;

public class SourcesTest {

    @Test
    public void testCacheMetadata1() {
        Source source = Sources.fromClass(ComplexComponent1.class);
        Assert.assertNotNull(source);

        Annotation[] annotations = source.adaptTo(Annotation[].class);
        Assert.assertNotNull(annotations);
        Assert.assertArrayEquals(
            new String[] {"AemComponent", "Dialog", "EditConfig", "CustomEditConfigAnnotation", "Tabs", "Accordion"},
            getAnnotationNames(annotations));

        annotations = source.adaptTo(AemComponent[].class);
        Assert.assertNotNull(annotations);
        Assert.assertEquals(1, annotations.length);

        AemComponent standalone = source.adaptTo(AemComponent.class);
        Assert.assertNotNull(standalone);
    }

    @Test
    public void testCacheMetadata2() throws NoSuchMethodException {
        Source source = Sources.fromMember(DesignDialogView.class.getDeclaredMethod("getDropdown2"));
        Assert.assertNotNull(source);

        Annotation[] annotations = source.adaptTo(Annotation[].class);
        Assert.assertNotNull(annotations);
        Assert.assertArrayEquals(
            new String[] {"DialogField", "TextField", "Place"},
            getAnnotationNames(annotations));

        annotations = source.adaptTo(AemComponent[].class);
        Assert.assertNotNull(annotations);
        Assert.assertEquals(0, annotations.length);
    }

    @Test
    public void testCacheMetadata3() {
        Source source = Sources.fromClass(AllowedChildrenTestCases.SimpleContainer.class);
        Assert.assertNotNull(source);

        Annotation[] annotations = source.adaptTo(Annotation[].class);
        Assert.assertNotNull(annotations);
        Assert.assertArrayEquals(
            new String[] {"AemComponent", "AllowedChildrenConfig", "AllowedChildren", "AllowedChildren"},
            getAnnotationNames(annotations));

        annotations = source.adaptTo(AllowedChildren[].class);
        Assert.assertNotNull(annotations);
        Assert.assertEquals(2, annotations.length);

        AllowedChildren standalone = source.adaptTo(AllowedChildren.class);
        Assert.assertNull(standalone); // You cannot cast to a single instance of a repeatable annotation
    }

    @Test
    public void testInterpolateMetadata() throws NoSuchMethodException {
        Source source = Sources.fromClass(ScriptedFieldset.class);
        Assert.assertNotNull(source);

        Data[] classLevelData = source.adaptTo(Data[].class);
        Assert.assertNotNull(classLevelData);
        Assert.assertEquals("scripted{@value}", classLevelData[0].value());

        source = Sources.fromMember(ScriptedFieldset.class.getDeclaredMethod("getHeading"));
        Property[] properties = source.adaptTo(Property[].class);
        Assert.assertNotNull(properties);
        Assert.assertEquals(2, properties.length);
        Assert.assertEquals("/subnode_1", properties[0].name());
        Assert.assertEquals("/subnode_2", properties[1].name());
    }

    private static String[] getAnnotationNames(Annotation[] values) {
        return Arrays.stream(values)
            .map(annotation -> annotation.annotationType().getSimpleName())
            .toArray(String[]::new);
    }
}
