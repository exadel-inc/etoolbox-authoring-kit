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

package com.exadel.aem.toolkit.samples.annotations.handlers;

import java.lang.reflect.Field;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.samples.annotations.FieldsetPostfix;

public class FieldsetPostfixAnnotationHandler implements DialogWidgetHandler {

    @Override
    public String getName() {
        return "postfix";
    }

    @Override
    public String before() {
        return null;
    }

    @Override
    public String after() {
        return null;
    }

    @Override
    public void accept(Element element, Field field) {

        Element currentElement;
        String currentNodeName;
        String newNodeName;
        String postfix = field.getAnnotation(FieldsetPostfix.class).postfix();
        NodeList fieldsetNodes = element.getChildNodes().item(0).getChildNodes();
        int numOfNodes = fieldsetNodes.getLength();

        for (int i = 0; i < numOfNodes; ++i) {
            currentElement = (Element) fieldsetNodes.item(i);
            currentNodeName = currentElement.getAttribute("name");
            newNodeName = (currentNodeName != null) ? currentNodeName.concat(postfix) : null;
            currentElement.setAttribute("name", newNodeName);
        }
    }
}
