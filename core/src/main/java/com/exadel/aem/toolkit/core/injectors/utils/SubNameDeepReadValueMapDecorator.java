package com.exadel.aem.toolkit.core.injectors.utils;

import com.exadel.aem.toolkit.core.CoreConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.DeepReadValueMapDecorator;

public class SubNameDeepReadValueMapDecorator extends DeepReadValueMapDecorator {
    private final ValueMap base;

    private final String prefix;

    private final String postfix;

    private final ResourceResolver resolver;

    private final String parentPath;

    public SubNameDeepReadValueMapDecorator(Resource resource, ValueMap base, String prefix, String postfix) {
        super(resource, base);
        this.base = base;
        this.prefix = prefix;
        this.postfix = postfix;
        this.resolver = resource.getResourceResolver();
        this.parentPath = resource.getPath() + CoreConstants.SEPARATOR_SLASH;
    }

    private ValueMap getValueMap(final String name) {
        final int position = name.lastIndexOf(CoreConstants.SEPARATOR_SLASH);
        if (position == -1) {
            return base;
        }
        final Resource resource = resolver.getResource(parentPath + getNameWithPrefixAndPostfix(name.substring(0, position)));
        if (resource != null) {
            return resource.getValueMap();
        }
        return ValueMap.EMPTY;
    }

    @Override
    public <T> T get(String name, Class<T> type) {
        return getValueMap(name).get(StringUtils.substringAfterLast(name, CoreConstants.SEPARATOR_SLASH), type);
    }

    private String getNameWithPrefixAndPostfix(String name) {
        return StringUtils.defaultString(prefix) + name + StringUtils.defaultString(postfix);
    }
}
