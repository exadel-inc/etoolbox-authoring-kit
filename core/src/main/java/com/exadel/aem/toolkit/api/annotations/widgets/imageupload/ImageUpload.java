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
package com.exadel.aem.toolkit.api.annotations.widgets.imageupload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up an image uploader in Granite UI based on {@code cq/gui/components/authoring/dialog/fileupload}
 * Granite component
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.FILEUPLOAD)
@AnnotationRendering(properties = "all")
public @interface ImageUpload {

    /**
     * When set to a non-blank string, maps to the {@code icon} attribute of this Granite UI component's node.
     * Used to define the icon of the component
     * @return String value
     */
    String icon() default "";

    /**
     * When set to a non-blank string, maps to the {@code fileNameParameter} attribute of this Granite UI component's node.
     * Used to determine the name of JCR attribute which holds the name of the file
     * @return String value
     */
    String fileNameParameter() default "";

    /**
     * When set to a non-blank string, maps to the {@code fileReferenceParameter} attribute of this Granite UI component's
     * node. Used to determine the name of JCR attribute which holds the address of the file in DAM
     * @return String value
     */
    String fileReferenceParameter() default "";

    /**
     * When set to a positive number, maps to the {@code sizeLimit} attribute of this Granite UI component's node.
     * Used to determine the file size limit
     * @return Long value
     */
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    @PropertyRendering(ignoreValues = "0")
    long sizeLimit() default 0;

    /**
     * Maps to the {@code mimeTypes} attribute of this Granite UI component's node.
     * Defines the browse and selection filter for file selection
     * @return String value, or an array of strings
     */
    String[] mimeTypes() default {ImageUploadConstants.DEFAULT_MIME_TYPE};

    /**
     * Maps to the {@code allowUpload} attribute of this Granite UI component's node.
     * Defines whether file uploading is allowed. If not, only file picking is allowed
     * @return True or false
     */
    boolean allowUpload() default false;

    /**
     * When set to a non-blank string, maps to the {@code viewInAdminURI} attribute of this Granite UI component's node.
     * Used to determine the URI Template used for editing the referenced DAM file
     * @return String value
     *
     */
    String viewInAdminURI() default "";
}
