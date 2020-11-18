package com.exadel.aem.toolkit.api.handlers;

import org.w3c.dom.Element;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

public interface DialogWidgetHandlerLegacy extends BiConsumer<Element, Field> {

    String getName();
}
