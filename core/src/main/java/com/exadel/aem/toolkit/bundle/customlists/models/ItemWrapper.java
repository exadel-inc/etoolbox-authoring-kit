package com.exadel.aem.toolkit.bundle.customlists.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED_BY;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LASTMODIFIED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LAST_MODIFIED_BY;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;

/**
 * A wrapper model for a custom list item
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ItemWrapper {

    private static final String FIELD_ITEM_RES_TYPE = "itemResourceType";
    private static final String SLING_RESOURCE_TYPE = "sling:resourceType";
    private static final List<String> SYSTEM_PROPERTIES = new ArrayList<>(Arrays.asList(JCR_CREATED, JCR_CREATED_BY,
        JCR_LASTMODIFIED, JCR_LAST_MODIFIED_BY, JCR_PRIMARYTYPE, SLING_RESOURCE_TYPE));

    @Self
    private Resource currentResource;

    private String itemResType;

    private Map<String, Object> properties;

    @PostConstruct
    private void init() {
        properties = currentResource.getValueMap()
            .entrySet()
            .stream()
            .filter(entry -> !SYSTEM_PROPERTIES.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Resource pageRes = getPageResource();
        if (pageRes == null) {
            return;
        }
        itemResType = pageRes.getValueMap().get(FIELD_ITEM_RES_TYPE, StringUtils.EMPTY);
    }

    private Resource getPageResource() {
        return Optional.of(currentResource)
            .map(Resource::getParent)
            .map(Resource::getParent)
            .orElse(null);
    }

    public String getItemResType() {
        return itemResType;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

}
