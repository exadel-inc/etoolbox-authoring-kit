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

import java.util.Optional;
import java.util.function.Consumer;

import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.test.custom.annotation.CustomDialogAnnotation;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.DialogHandler;

@Handles(before = CustomProcessingHandler.class, after = CustomWidgetHandler.class, value = CustomDialogAnnotation.class)
@SuppressWarnings("unused")
public class CustomDialogHandler implements DialogHandler {

    @Override
    public void accept(Class<?> aClass, Target element) {
        visitElements(element, elt -> {
            if (StringUtils.equals(elt.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, String.class), ResourceTypes.MULTIFIELD)
                    && isTopLevelMultifield(elt)) {
                elt.attribute("multifieldSpecial", "This is added to top-level Multifields");
            }
        });
        //element.getFirstChild();

    }

    private static void visitElements(Target root, Consumer<Target> visitor) {
        visitor.accept(root);
        if (root.listChildren().isEmpty()) {
            return;
        }
        root.listChildren()
                .forEach(elt -> visitElements(elt, visitor));
    }

    private static boolean isTopLevelMultifield(Target target) {
        String resourceType = Optional.ofNullable(target.parent())
                .map(Target::parent)
                .map(Target::parent)
                .map(node -> node.getValueMap().get(DialogConstants.PN_SLING_RESOURCE_TYPE))
                .orElse(StringUtils.EMPTY);
        return !resourceType.equals(ResourceTypes.MULTIFIELD);
    }
}
