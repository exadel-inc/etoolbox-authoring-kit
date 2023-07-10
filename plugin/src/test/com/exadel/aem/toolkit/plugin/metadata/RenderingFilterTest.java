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
package com.exadel.aem.toolkit.plugin.metadata;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent1;

public class RenderingFilterTest {

    @Test
    public void testPropertyMapping() throws NoSuchMethodException {
        AemComponent annotation = ComplexComponent1.class.getAnnotation(AemComponent.class);
        RenderingFilter filter = new RenderingFilter(annotation);

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
