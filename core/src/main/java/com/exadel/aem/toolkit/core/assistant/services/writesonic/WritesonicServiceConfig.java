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
package com.exadel.aem.toolkit.core.assistant.services.writesonic;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.exadel.aem.toolkit.core.utils.HttpClientFactory;

@ObjectClassDefinition(name = "EToolbox Authoring Kit - Assistant: Writesonic Integration")
public @interface WritesonicServiceConfig {

    String DEFAULT_ENGINE = "good";
    String DEFAULT_LANGUAGE = "en";
    String DEFAULT_TONE = "professional";
    int DEFAULT_TEXTS_COUNT = 5;

    String DEFAULT_IMAGE_SIZE = "512x512";
    int DEFAULT_IMAGES_COUNT = 3;

    @AttributeDefinition(name = "Enabled")
    boolean enabled() default true;

    @AttributeDefinition(name = "Content Endpoint")
    String contentEndpoint() default "https://api.writesonic.com/v1/business/content/{command}?engine={engine}&language={language}";

    @AttributeDefinition(name = "Images Endpoint")
    String imagesEndpoint() default "https://api.writesonic.com/v1/business/photosonic/generate-image";

    @AttributeDefinition(name = "API (v.1) Key")
    String api1Key() default StringUtils.EMPTY;

    @AttributeDefinition(name = "API (v.2) Key")
    String api2Key() default StringUtils.EMPTY;

    @AttributeDefinition(name = "Default Engine")
    String engine() default DEFAULT_ENGINE;

    @AttributeDefinition(name = "Default Language")
    String language() default DEFAULT_LANGUAGE;

    @AttributeDefinition(name = "Default Tone of Voice")
    String tone() default DEFAULT_TONE;

    @AttributeDefinition(name = "Default Output Image Size")
    String imageSize() default DEFAULT_IMAGE_SIZE;

    @AttributeDefinition(name = "Number of image choices")
    int imagesCount() default DEFAULT_IMAGES_COUNT;

    @AttributeDefinition(name = "Connection Timeout (ms)")
    int timeout() default HttpClientFactory.DEFAULT_TIMEOUT;
}
