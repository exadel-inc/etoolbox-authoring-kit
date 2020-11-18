package com.exadel.aem.toolkit.api.handlers;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import org.w3c.dom.Element;

import java.util.function.BiConsumer;

@Handles(Dialog.class)
@SuppressWarnings("unused")
public interface DialogHandlerLegacy extends BiConsumer<Element, Class<?>> {
    /**
     * Identifies this DialogHandler for binding to a specific {@code DialogAnnotation}
     * @return String value, non-blank
     */
    String getName();
}
