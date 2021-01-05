package com.exadel.aem.toolkit.bundle.customlists.models;

import java.util.Objects;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import com.day.cq.commons.jcr.JcrConstants;

/**
 * A model that represents the simplest List item which contains of "jct:title" and "value" fields
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class GenericItem {

    @ValueMapValue(name = JcrConstants.JCR_TITLE)
    private String title;

    @ValueMapValue
    private String value;

    public String getTitle() {
        return title;
    }

    public String isValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericItem that = (GenericItem) o;
        return Objects.equals(title, that.title) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, value);
    }
}
