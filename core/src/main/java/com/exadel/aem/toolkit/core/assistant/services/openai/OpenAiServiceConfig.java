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
package com.exadel.aem.toolkit.core.assistant.services.openai;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.exadel.aem.toolkit.core.utils.HttpClientFactory;

@ObjectClassDefinition(name = "EToolbox Authoring Kit - Assistant: OpenAI Integration")
public @interface OpenAiServiceConfig {

    String DEFAULT_COMPLETION_MODEL = "text-davinci-003";
    String DEFAULT_CHAT_MODEL = "gpt-3.5-turbo";

    String DEFAULT_IMAGE_SIZE = "512x512";
    String DEFAULT_EDIT_MODEL = "text-davinci-edit-001";
    double DEFAULT_TEMPERATURE = 0.8d;
    int DEFAULT_TEXT_LENGTH = 200;

    @AttributeDefinition(name = "Enabled")
    boolean enabled() default true;

    @AttributeDefinition(name = "Authorization Token")
    String token();

    @AttributeDefinition(name = "Chat Endpoint")
    String chatEndpoint() default "https://api.openai.com/v1/chat/completions";

    @AttributeDefinition(name = "Completions Endpoint")
    String completionsEndpoint() default "https://api.openai.com/v1/completions";

    @AttributeDefinition(name = "Edits Endpoint")
    String editsEndpoint() default "https://api.openai.com/v1/edits";

    @AttributeDefinition(name = "Images Endpoint")
    String imagesEndpoint() default "https://api.openai.com/v1/images/generations";

    @AttributeDefinition(name = "Default Completion Model")
    String completionModel() default DEFAULT_COMPLETION_MODEL;

    @AttributeDefinition(name = "Default Edit Model")
    String editModel() default DEFAULT_EDIT_MODEL;

    @AttributeDefinition(name = "Default Temperature")
    double temperature() default DEFAULT_TEMPERATURE;

    @AttributeDefinition(name = "Default Output Length (tokens)")
    int textLength() default DEFAULT_TEXT_LENGTH;

    @AttributeDefinition(name = "Default Output Image Size")
    String imageSize() default DEFAULT_IMAGE_SIZE;

    @AttributeDefinition(name = "Number of Choices")
    int choices() default 3;

    @AttributeDefinition(name = "Connection Timeout (ms)")
    int timeout() default HttpClientFactory.DEFAULT_TIMEOUT;

    @AttributeDefinition(name = "Connection Attempts")
    int connectionAttempts() default HttpClientFactory.DEFAULT_ATTEMPTS_COUNT;

    @AttributeDefinition(name = "Response Caching")
    boolean caching() default false;
}
