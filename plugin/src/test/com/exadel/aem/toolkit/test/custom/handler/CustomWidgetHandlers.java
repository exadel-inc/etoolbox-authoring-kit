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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.HandlesWidgets;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.api.runtime.Injected;
import com.exadel.aem.toolkit.api.runtime.RuntimeContext;
import com.exadel.aem.toolkit.test.custom.annotation.CustomNonMappingWidgetAnnotation;
import com.exadel.aem.toolkit.test.custom.annotation.CustomScopedNonMappingWidgetAnnotation;
import com.exadel.aem.toolkit.test.custom.annotation.CustomWidgetAnnotation;

@SuppressWarnings("unused")
public class CustomWidgetHandlers {

    private static final List<String> ATTRIBUTE_LIST = new ArrayList<>(2);

    static {
        ATTRIBUTE_LIST.add("customWidgetHandler2");
        ATTRIBUTE_LIST.add("customWidgetHandler3");
    }

    @Handles(value = CustomWidgetAnnotation.class)
    @SuppressWarnings("deprecation") // References to DialogWidgetHandler, RuntimeContext are retained for compatibility
    public static class CustomWidgetHandler1 implements DialogWidgetHandler {

        @Override
        public String getName() {
            return "testCustomAnnotation";
        }

        @Injected
        private RuntimeContext runtimeContext;

        @Override
        public void accept(Element element, Field field) {
            CustomWidgetAnnotation testCustomAnnotation = field.getDeclaredAnnotation(CustomWidgetAnnotation.class);
            Element customElement = runtimeContext.getXmlUtility().createNodeElement("customElement");
            element.appendChild(customElement);
            customElement.setAttribute("customField", testCustomAnnotation.customField());
        }
    }

    @Handles(value = CustomWidgetAnnotation.class, after = CustomWidgetHandler1.class)
    public static class CustomWidgetHandler2 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target
                .attribute(ATTRIBUTE_LIST.get(0), ATTRIBUTE_LIST.get(0))
                .attribute(ATTRIBUTE_LIST.get(1), ATTRIBUTE_LIST.get(0));
        }
    }

    @Handles(value = CustomWidgetAnnotation.class, after = CustomWidgetHandler2.class)
    public static class CustomWidgetHandler3 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target
                .attribute(ATTRIBUTE_LIST.get(1), ATTRIBUTE_LIST.get(1));
        }
    }

    @HandlesWidgets(value = CustomNonMappingWidgetAnnotation.class)
    @SuppressWarnings("deprecation") // Reference to HandlesWidgets is retained for compatibility testing
    public static class CustomNonMappingWidgetHandler1 implements DialogWidgetHandler {

        @Override
        public String getName() {
            return "testCustomProcessing";
        }

        @Override
        public void accept(Element element, Field field) {
            element.setAttribute("customProcessing", "turned on");
        }
    }

    @Handles(value = CustomScopedNonMappingWidgetAnnotation.class)
    public static class CustomNonMappingWidgetHandler2 implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target.attribute("customProcessing", "turned on");
        }
    }

    @Handles(value = MultiField.class, before = CustomMultifieldHandler.class, after = CustomNonMappingWidgetHandler1.class)
    public static class CustomMultifieldHandler implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target.attribute("multifieldSpecial", "This is added to Multifields");
        }
    }

    @Handles(value = PathField.class, scope = Scopes.CQ_DESIGN_DIALOG)
    public static class CustomScopeLimitedHandler implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target.attribute("designDialogSpecial", "This is added to PathField@DesignDialog");
        }
    }

    @Handles(value = Nullable.class, scope = Scopes.CQ_DESIGN_DIALOG)
    public static class ThirdPartyAnnotationHandler implements Handler {

        @Override
        public void accept(Source source, Target target) {
            target.attribute("designDialogSpecial", "This is added to Nullable@DesignDialog");
        }
    }
}
