package com.exadel.aem.toolkit.api.annotations.injectors;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

/**
 * Common methods for the injectors
 */
public class InjectorUtils {

    public static Resource getResource(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource();
        }
        if (adaptable instanceof Resource) {
            return (Resource) adaptable;
        }
        return null;
    }

    public static SlingHttpServletRequest getSlingHttpServletRequest(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable);
        }
        return null;
    }

    public static ValueMap getValueMap(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource().adaptTo(ValueMap.class);
        } else if (adaptable instanceof ValueMap) {
            return ((ValueMap) adaptable);
        }
        return ValueMap.EMPTY;
    }
}
