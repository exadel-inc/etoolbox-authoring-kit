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

package com.exadel.aem.toolkit.api.annotations.widgets.imageupload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up Coral 3 FileUpload element based on {@code cq/gui/components/authoring/dialog/fileupload} Granite component in TouchUI dialog
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.FILEUPLOAD)
@PropertyMapping
public @interface ImageUpload {

    /**
     * When set to a non-blank string, maps to the 'icon' attribute of this TouchUI dialog component's node.
     * Used to define component's icon
     * @return String value
     */
    String icon() default "";

    /**
     * When set to a non-blank string, maps to the 'fileNameParameter' attribute of this TouchUI dialog component's node.
     * Used to determine the (relative) location where to store the name of the file
     * @return String value
     */
    String fileNameParameter() default "";

    /**
     * When set to a non-blank string, maps to the 'fileReferenceParameter' attribute of this TouchUI dialog component's node.
     * Used to determine where to store the reference of the file (when a file already uploaded on the server)
     * @return String value
     */
    String fileReferenceParameter() default "";

    /**
     * When set to a positive number, maps to the 'sizeLimit' attribute of this TouchUI dialog component's node.
     * Used to determine the file size limit
     * @return Long value
     */
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    @PropertyRendering(ignoreValues = "0")
    long sizeLimit() default 0;

    /**
     * Maps to the 'mimeTypes' attribute of this TouchUI dialog component's node.
     * Defines the browse and selection filter for file selection
     * @return String value, or an array of strings
     */
    String[] mimeTypes() default {ImageUploadConstants.DEFAULT_MIME_TYPE};

    /**
     * Maps to the 'allowUpload' attribute of this TouchUI dialog component's node.
     * Defines whether file uploading is allowed. Otherwise, only file picking is allowed
     * @return True or false
     */
    boolean allowUpload() default false;

    /**
     * When set to a non-blank string, maps to the 'viewInAdminURI' attribute of this TouchUI dialog component's node.
     * Used to determine the URI Template used for editing the referenced DAM file
     * @return String value
     *
     */
    String viewInAdminURI() default "";
}
