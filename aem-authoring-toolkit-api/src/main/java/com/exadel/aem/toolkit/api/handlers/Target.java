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
package com.exadel.aem.toolkit.api.handlers;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;

public interface Target {

    Target create(String s);

    Target create(Supplier<String> s);

    Target getOrCreate(String s);

    Target get(String s);

    default Target mapProperties(Annotation annotation) {
        return mapProperties(annotation, Collections.emptyList());
    }

    Target mapProperties(Annotation annotation, List<String> skipped);

    Target mapProperties(Element element);

    Target attribute(String name, String value);

    Target attribute(String name, boolean value);

    Target attribute(String name, long value);

    Target attribute(String name, double value);

    Target attribute(String name, Date value);

    Target attributes(Map<String, Object> map);

    Target prefix(String prefix);

    Target postfix(String postfix);

    Target scope(XmlScope scope);

    XmlScope getScope();

    String getPrefix();

    String getPostfix();

    void delete();

    Map<String, String> getAttributes();

    List<Target> getChildren();

    String getName();

    Target parent();

    boolean hasChild(String path);

    Document buildXml() throws ParserConfigurationException;
}
