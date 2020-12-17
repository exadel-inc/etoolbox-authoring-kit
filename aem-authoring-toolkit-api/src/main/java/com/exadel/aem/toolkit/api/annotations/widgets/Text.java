package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

/**
 * Used to set up
 * https://helpx.adobe.com/experience-manager/6-3/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/text/index.html
 * A generic text component that is rendered as <span> in TouchUI dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.TEXT)
@PropertyMapping
@SuppressWarnings("unused")
public @interface Text {
    /**
     * Maps to the text of the component of this TouchUI dialog component's node
     * @return String value
     */
    String text();
}
