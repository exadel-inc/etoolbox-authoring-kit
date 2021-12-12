package com.exadel.aem.toolkit.samples.models.filters;


import org.apache.sling.api.resource.Resource;

import java.util.function.Predicate;

public class ResourceTypeFilter implements Predicate<Resource> {
    /**
     * Evaluates this predicate on the given argument.
     *
     * @param resource the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(Resource resource) {
        return resource.isResourceType("etoolbox-authoring-kit/samples/components/content/abilities-component");
    }
}
