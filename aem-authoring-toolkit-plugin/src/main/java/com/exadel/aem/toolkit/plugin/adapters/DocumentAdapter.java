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

package com.exadel.aem.toolkit.plugin.adapters;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.exadel.aem.toolkit.api.handlers.Adaptable;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.PluginXmlUtility;

@Adaptable(Target.class)
public class DocumentAdapter {

    private final Document document;

    public DocumentAdapter(Target target) {
        this.document = createDocument(target);
    }

    public Document getDocument() {
        return document;
    }

    private static Document createDocument(Target target) {
        try {
            return PluginXmlUtility.toDocument(target);
        } catch (ParserConfigurationException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
            return null;
        }
    }
}
