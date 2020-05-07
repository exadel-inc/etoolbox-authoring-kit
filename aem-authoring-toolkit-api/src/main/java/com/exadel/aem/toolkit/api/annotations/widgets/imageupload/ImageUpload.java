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

import com.exadel.aem.toolkit.api.annotations.meta.EnumValue;
import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up Coral 2
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/foundation/form/fileupload/index.html">
 * FileUpload element</a> in TouchUI dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.FILEUPLOAD)
@PropertyMapping
@SuppressWarnings("unused")
public @interface ImageUpload {
    /**
     * When set to a non-blank string, maps to the 'id' attribute of this TouchUI dialog component's node.
     * Renders as the HTML 'id' attribute
     * @return String value
     */
    String id() default "";

    /**
     * When set to a non-blank string, maps to the 'rel' attribute of this TouchUI dialog component's node.
     * Renders as the HTML 'rel' attribute
     * @return String value
     */
    String rel() default "";

    /**
     * When set to a non-blank string, maps to the 'class' attribute of this TouchUI dialog component's node.
     * Renders as the HTML 'class' attribute
     * @return String value
     *
     * @deprecated This will be removed starting from version 2.0.0. Please use {@link ImageUpload#className()} instead
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
     */
    @PropertyRendering(name = "class")
    String className() default "";

    /**
     * When set to a non-blank string, maps to the 'title' attribute of this TouchUI dialog component's node.
     * Renders as the HTML 'title' attribute
     * @return String value
     */
    String title() default "";

    /**
     * When set to a non-blank string, maps to the 'emptyText' attribute of this TouchUI dialog component's node.
     * Used to define text hint for an empty ImageUpload
     * @return String value
     */
    String emptyText() default "";

    /**
     * Maps to the 'text' attribute of this TouchUI dialog component's node.
     * Used to define default button text
     * @return String value
     */
    @PropertyRendering(name = "text")
    String buttonText() default ImageUploadConstants.DEFAULT_BUTTON_TEXT;
    /**
     * When set to a non-blank string, maps to the 'text' attribute of this TouchUI dialog component's node.
     * Used to define component's icon
     * @return String value
     */
    String icon() default "";

    /**
     * Maps to the 'variant' attribute of this TouchUI dialog component's node.
     * Used to define button variant
     * @see ButtonVariant
     * @return One of {@code ButtonVariant} values
     */
    @PropertyRendering(name = "variant")
    @EnumValue(transformation = StringTransformation.LOWERCASE)
    ButtonVariant buttonVariant() default ButtonVariant.SECONDARY;
    /**
     * Maps to the 'multiple' attribute of this TouchUI dialog component's node.
     * Used to set possibility for multiple files upload
     * @return True or false
     */
    boolean multiple() default false;

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
     * Maps to the 'uploadUrl' attribute of this TouchUI dialog component's node.
     * Used to determine the URL where to upload the file
     * @return String value representing valid JCR path
     */
    @ValueRestriction(ValueRestrictions.JCR_PATH)
    String uploadUrl() default ImageUploadConstants.DEFAULT_UPLOAD_URL;

    /**
     * When set to a non-blank string, maps to the 'uploadUrlBuilder' attribute of this TouchUI dialog component's node.
     * Used to determine the upload URL builder
     * @return String value
     */
    String uploadUrlBuilder() default "";

    /**
     * When set to a positive number, maps to the 'sizeLimit' attribute of this TouchUI dialog component's node.
     * Used to determine the file size limit
     * @return Long value
     */
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    @PropertyRendering(ignoreValues = "0")
    long sizeLimit() default 0;

    /**
     * When set to a non-blank string, maps to the 'autoStart' attribute of this TouchUI dialog component's node.
     * If 'true' assigned, the upload starts automatically once the file is selected
     * @return String value
     */
    String autoStart() default "";

    /**
     * Maps to the 'useHTML5' attribute of this TouchUI dialog component's node.
     * Defines whether HTML5 is preferred for uploading files if browser allows it
     * @return True or false
     */
    boolean useHTML5() default true;

    /**
     * When set to a non-blank string, maps to the 'dropZone' attribute of this TouchUI dialog component's node.
     * Defines the drop zone selector to upload files from file system directly (if browser allows it)
     * @return String value
     */
    String dropZone() default "";

    /**
     * Maps to the 'mimeTypes' attribute of this TouchUI dialog component's node.
     * Defines the browse and selection filter for file selection
     * @return String value, or an array of strings
     */
    String[] mimeTypes() default {ImageUploadConstants.DEFAULT_MIME_TYPE};

    /**
     * Maps to the 'chunkUploadSupported' attribute of this TouchUI dialog component's node.
     * Defines whether chunked uploading is supported
     * @return True or false
     */
    boolean chunkUploadSupported() default false;

    /**
     * When set to a positive number, maps to the 'chunkSize' attribute of this TouchUI dialog component's node.
     * Used to determine the chunk size of an uploaded file
     * @return Long value
     */
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    @PropertyRendering(ignoreValues = "0")
    long chunkSize() default 0;

    /**
     * When set to a positive number, maps to the 'chunkUploadMinFileSize' attribute of this TouchUI dialog component's node.
     * Used to determine the minimal size of an uploaded file to be split in chunks
     * @return Long value
     */
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    @PropertyRendering(ignoreValues = "0")
    long chunkUploadMinFileSize() default 0;

    /**
     * Maps to the 'allowUpload' attribute of this TouchUI dialog component's node.
     * Defines whether file uploading (otherwise only file picking) is allowed
     * @return True or false
     */
    boolean allowUpload() default false;
}
