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
package com.exadel.aem.toolkit.test.dependson;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

@AemComponent(
    path = TestConstants.DEFAULT_COMPONENT_NAME,
    title = TestConstants.DEFAULT_COMPONENT_TITLE
)
@Dialog
@SuppressWarnings("unused")
public class DependsOnSemicolon {
    @DialogField
    @TextField
    @DependsOn(query = "@field1 === ';'")
    private String literalEscapingTest;

    @DialogField
    @TextField
    @DependsOn(query = "(function() { var sum = @a + @b; return sum * sum; })()")
    private String iifeQueryTest;

    @DialogField
    @TextField
    @DependsOn(query = "';' === ';'")
    @DependsOn(query = "';' === ';'", action = DependsOnActions.REQUIRED)
    private String multipleActions;
}
