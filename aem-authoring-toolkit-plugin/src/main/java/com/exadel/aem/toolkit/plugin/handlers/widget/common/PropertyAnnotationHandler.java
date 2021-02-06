package com.exadel.aem.toolkit.plugin.handlers.widget.common;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginNamingUtility;

public class PropertyAnnotationHandler implements BiConsumer<Source, Target> {

    @Override
    public void accept(Source source, Target target) {
        Arrays.stream(source.adaptTo(Property[].class))
            .forEach(p -> acceptProperty(p, target));
    }

    private void acceptProperty(Property property, Target target) {
        String propertyName;
        String propertyPath;
        if (property.name().contains(DialogConstants.PATH_SEPARATOR)) {
            propertyName = StringUtils.substringAfterLast(property.name(), DialogConstants.PATH_SEPARATOR);
            propertyPath = StringUtils.substringBeforeLast(property.name(), DialogConstants.PATH_SEPARATOR);
        } else {
            propertyName = property.name();
            propertyPath = null;
        }
        Target effectiveTarget = StringUtils.isNotBlank(propertyPath) ? target.getOrCreateTarget(propertyPath) : target;
        effectiveTarget.attribute(PluginNamingUtility.getValidFieldName(propertyName), property.value());
    }
}
