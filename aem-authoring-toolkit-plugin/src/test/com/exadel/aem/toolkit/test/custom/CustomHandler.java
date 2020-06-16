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

package com.exadel.aem.toolkit.test.custom;

import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.MemberWrapper;
import com.exadel.aem.toolkit.api.runtime.Injected;
import com.exadel.aem.toolkit.api.runtime.RuntimeContext;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import org.w3c.dom.Element;

@SuppressWarnings("unused")
public class CustomHandler implements DialogWidgetHandler {
    @Injected
    @SuppressWarnings("UnusedDeclaration")
    private RuntimeContext runtimeContext;

    @Override
    public String getName() {
        return "testCustomAnnotation";
    }

    @Override
    public void accept(Element element, MemberWrapper memberWrapper) {
        CustomAnnotation testCustomAnnotation = PluginReflectionUtility.getMemberAnnotation(memberWrapper.getMember(), CustomAnnotation.class);
        if (testCustomAnnotation == null) {
            return;
        }
        runtimeContext.getXmlUtility().setAttribute(element, "customField", testCustomAnnotation.customField());
    }
}
