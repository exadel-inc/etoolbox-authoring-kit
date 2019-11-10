/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package com.exadel.aem.toolkit.core.handlers.widget;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;

public class SelectHandler implements Handler, BiConsumer<Element, Field> {
    @Override
    public void accept(Element element, Field field) {
        Select select = field.getAnnotation(Select.class);
        if(ArrayUtils.isNotEmpty(select.options())){
            Element items = (Element) element.appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));
            for (Option option: select.options()) {
                String elementName = getXmlUtil().getUniqueName(option.value(),
                        DialogConstants.INVALID_NODE_NAME_PATTERN,
                        DialogConstants.NN_ITEM,
                        items);
                Element item = (Element) items.appendChild(getXmlUtil().createNodeElement(elementName));
                getXmlUtil().mapProperties(item, option);
            }
        }
        getXmlUtil().appendAcsCommonsList(element, select.acsListPath(), select.acsListResourceType());
    }
}
