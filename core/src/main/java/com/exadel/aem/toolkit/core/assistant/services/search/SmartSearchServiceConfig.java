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
package com.exadel.aem.toolkit.core.assistant.services.search;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "EToolbox Authoring Assistant: SmartSearch Service")
public @interface SmartSearchServiceConfig {

    @AttributeDefinition(name = "Text Search - Root")
    String textsRoot() default "/content";

    @AttributeDefinition(name = "Text Search - Scan Fields")
    String[] textSearchFields() default {"jcr:title", "title", "pageTitle", "jcr:description", "description", "value"};

    @AttributeDefinition(name = "Text Search - Output Sources")
    String[] textOutputFields() default {"jcr:description", "description", "value"};

    @AttributeDefinition(name = "Image Search - Root")
    String imagesRoot() default "/content/dam";

    @AttributeDefinition(name = "Max Options")
    int maxOptions() default 10;
}
