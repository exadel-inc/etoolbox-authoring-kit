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

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up Coral 3 FileUpload element based on {@code cq/gui/components/authoring/dialog/fileupload} Granite component in TouchUI dialog
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.FILEUPLOAD)
@PropertyMapping
@SuppressWarnings("unused")
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
     * Defines whether file uploading (otherwise only file picking) is allowed
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

    /**
     * When set to a non-blank string, maps to the 'id' attribute of this TouchUI dialog component's node.
     * Renders as the HTML 'id' attribute
     * @return String value
     *
     * @deprecated This property will be removed starting from version 2.0.0. Please use {@link com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute#id()} instead
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    String id() default "";

    /**
     * When set to a non-blank string, maps to the 'rel' attribute of this TouchUI dialog component's node.
     * Renders as the HTML 'rel' attribute
     * @return String value
     *
     * @deprecated This property will be removed starting from version 2.0.0. Please use {@link com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute#rel()} instead
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    String rel() default "";

    /**
     * When set to a non-blank string, maps to the 'class' attribute of this TouchUI dialog component's node.
     * Renders as the HTML 'class' attribute
     * @return String value
     *
     * @deprecated This property will be removed starting from version 2.0.0. Please use {@link com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute#className()} instead
     */
    @PropertyRendering(name = "class")
    @IgnorePropertyMapping
    @Deprecated
    @SuppressWarnings("squid:S1133")
    String clas() default "";

    /**
     * When set to a non-blank string, maps to the 'class' attribute of this TouchUI dialog component's node.
     * Renders as the HTML 'class' attribute
     * @return String value
     *
     * @deprecated This property will be removed starting from version 2.0.0. Please use {@link com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute#className()} instead
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    @PropertyRendering(name = "class")
    String className() default "";

    /**
     * When set to a non-blank string, maps to the 'title' attribute of this TouchUI dialog component's node.
     * Renders as the HTML 'title' attribute
     * @return String value
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    String title() default "";

    /**
     * When set to a non-blank string, maps to the 'emptyText' attribute of this TouchUI dialog component's node.
     * Used to define text hint for an empty ImageUpload
     * @return String value
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    String emptyText() default "";

    /**
     * Maps to the 'text' attribute of this TouchUI dialog component's node.
     * Used to define default button text
     * @return String value
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @PropertyRendering(name = "text")
    @Deprecated
    @SuppressWarnings("squid:S1133")
    String buttonText() default ImageUploadConstants.DEFAULT_BUTTON_TEXT;

    /**
     * Maps to the 'variant' attribute of this TouchUI dialog component's node.
     * Used to define button variant
     * @see ButtonVariant
     * @return One of {@code ButtonVariant} values
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @PropertyRendering(
        name = "variant",
        transform = StringTransformation.LOWERCASE
    )
    @Deprecated
    @SuppressWarnings("squid:S1133")
    ButtonVariant buttonVariant() default ButtonVariant.SECONDARY;
    /**
     * Maps to the 'multiple' attribute of this TouchUI dialog component's node.
     * Used to set possibility for multiple files upload
     * @return True or false
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    boolean multiple() default false;

    /**
     * Maps to the 'uploadUrl' attribute of this TouchUI dialog component's node.
     * Used to determine the URL where to upload the file
     * @return String value representing valid JCR path
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @ValueRestriction(ValueRestrictions.JCR_PATH)
    @Deprecated
    @SuppressWarnings("squid:S1133")
    String uploadUrl() default ImageUploadConstants.DEFAULT_UPLOAD_URL;

    /**
     * When set to a non-blank string, maps to the 'uploadUrlBuilder' attribute of this TouchUI dialog component's node.
     * Used to determine the upload URL builder
     * @return String value
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    String uploadUrlBuilder() default "";

    /**
     * When set to a non-blank string, maps to the 'autoStart' attribute of this TouchUI dialog component's node.
     * If 'true' assigned, the upload starts automatically once the file is selected
     * @return String value
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    String autoStart() default "";

    /**
     * Maps to the 'useHTML5' attribute of this TouchUI dialog component's node.
     * Defines whether HTML5 is preferred for uploading files if browser allows it
     * @return True or false
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    boolean useHTML5() default true;

    /**
     * When set to a non-blank string, maps to the 'dropZone' attribute of this TouchUI dialog component's node.
     * Defines the drop zone selector to upload files from file system directly (if browser allows it)
     * @return String value
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    String dropZone() default "";

    /**
     * Maps to the 'chunkUploadSupported' attribute of this TouchUI dialog component's node.
     * Defines whether chunked uploading is supported
     * @return True or false
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    boolean chunkUploadSupported() default false;

    /**
     * When set to a positive number, maps to the 'chunkSize' attribute of this TouchUI dialog component's node.
     * Used to determine the chunk size of an uploaded file
     * @return Long value
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    @PropertyRendering(ignoreValues = "0")
    @Deprecated
    @SuppressWarnings("squid:S1133")
    long chunkSize() default 0;

    /**
     * When set to a positive number, maps to the 'chunkUploadMinFileSize' attribute of this TouchUI dialog component's node.
     * Used to determine the minimal size of an uploaded file to be split in chunks
     * @return Long value
     *
     * @deprecated This property will be removed starting from version 2.0.0
     */
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    @PropertyRendering(ignoreValues = "0")
    @Deprecated
    @SuppressWarnings("squid:S1133")
    long chunkUploadMinFileSize() default 0;
}
