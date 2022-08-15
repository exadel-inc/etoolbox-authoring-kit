package com.exadel.aem.toolkit.api.annotations.widgets.slider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to define a slider item within the {@link Slider#items()} set
 * See documentation on <a href="https://javadoc.io/doc/com.exadel.etoolbox/etoolbox-authoring-kit-core/latest/com/exadel/aem/toolkit/api/annotations/widgets/slider/Slider.html">
 * Slider</a> component
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SliderItem {

    /**
     * Maps to the {@code value} attribute of this Granite UI component's node.
     * Used to define the corresponding value to the text that will be stored when this item is specified
     * @return long value
     */
    long value();

    /**
     * Maps to the {@code text} attribute of this Granite UI component's node.
     * Used to define the label item text of the tooltip
     * @return String value
     */
    String text();
}
