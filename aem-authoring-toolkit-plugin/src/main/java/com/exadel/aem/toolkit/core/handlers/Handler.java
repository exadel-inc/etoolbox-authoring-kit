package com.exadel.aem.toolkit.core.handlers;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.handlers.widget.DialogComponent;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;

public interface Handler {
    default PluginXmlUtility getXmlUtil() {
        return PluginRuntime.context().getXmlUtility();
    }

    static void appendContainer(List<Field> fields, Element containerElement) {
        appendContainer(fields, containerElement, true);
    }

    static void appendContainer(List<Field> fields, Element containerElement, boolean setResourceType){
        if (setResourceType) {
            containerElement.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER);
        }
        Element itemsElement = PluginRuntime.context().getXmlUtility().createNodeElement(DialogConstants.NN_ITEMS);
        containerElement.appendChild(itemsElement);

        fields.forEach(field -> DialogComponent.fromField(field).ifPresent(comp -> comp.append(itemsElement, field)));
    }
}
