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
package com.exadel.aem.toolkit.test.common;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Properties;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

@AemComponent(
    path = TestConstants.DEFAULT_COMPONENT_NAME,
    title = "My AEM Component",
    description = "The most awesome AEM component ever",
    componentGroup = "my-brand-new-components",
    templatePath = "/some/absolute/jcr/path",
    resourceSuperType = "/path/to/resource",
    cellName = "some-cell-name",
    isContainer = true
)
@Dialog(
    helpPath = "https://www.google.com/search?q=my+aem+component",
    width = 800,
    height = 600
)
@SuppressWarnings("unused")
public class PropertiesAnnotation {
    @DialogField
    @TextField
    @Properties({
        @Property(name = "simpleProperty", value = "value"),
        @Property(name = "../emptyValueProperty", value = ""),
        @Property(name = "../siblingNode/newProperty", value = "value"),
        @Property(name = "ad/wrong f@ield! NamE", value = "non-latin символы"),
    })
    String field;
}
