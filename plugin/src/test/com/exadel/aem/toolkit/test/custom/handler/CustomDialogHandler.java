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
package com.exadel.aem.toolkit.test.custom.handler;

import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.test.custom.annotation.CustomLegacyDialogAnnotation;

@Handles(value = CustomLegacyDialogAnnotation.class)
@SuppressWarnings("unused") // Used by ToolKit Plugin logic
public class CustomDialogHandler implements Handler {

    @Override
    public void accept(Source source, Target target) {
        String field1Value = target.getAttribute("field1");
        target.removeAttribute("field1");
        target
            .attribute("autoField1", field1Value)
            .attribute("fullyQualifiedClassName", source.adaptTo(Class.class).getCanonicalName());
    }
}
