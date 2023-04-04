/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.aem.toolkit.core.injectors.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Represents a {@link Resource} properties and children of which are filtered against the provided {@code prefix}
 * and/or {@code postfix}
 */
class FilteredResourceDecorator extends SlingAdaptable implements Resource {

    private final Resource base;
    private final String prefix;
    private final String postfix;
    private ValueMap filteredValueMap;

    /**
     * Constructs a new {@link FilteredResourceDecorator} instance
     * @param base    Original resource; must be non-null
     * @param prefix  Optional prefix value
     * @param postfix Optional postfix value
     */
    FilteredResourceDecorator(@Nonnull Resource base, String prefix, String postfix) {
        this.base = base;
        this.prefix = prefix;
        this.postfix = postfix;
    }

    /* ----------------
       Resource members
       ---------------- */

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getPath() {
        return base.getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getName() {
        return base.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Resource getParent() {
        return base.getParent();
    }

    /**
     * {@inheritDoc} Specific for this implementation, enumerates only the children that have names matched by the given
     * {@code prefix} and/or {@code postfix}
     */
    @Nonnull
    @Override
    public Iterator<Resource> listChildren() {
        return StreamSupport.stream(this.base.getChildren().spliterator(), false)
            .filter(resource -> isMatchByPrefixOrPostfix(resource.getName(), prefix, postfix))
            .iterator();
    }

    /**
     * {@inheritDoc} Specific for this implementation, enumerates only the children that have names matched by the given
     * {@code prefix} and/or {@code postfix}
     */
    @Nonnull
    @Override
    public Iterable<Resource> getChildren() {
        return this::listChildren;
    }

    /**
     * {@inheritDoc} Specific for the current implementation, attempts to complete the given name with the specified
     * {@code prefix} and/or {@code postfix}
     */
    @Nullable
    @Override
    public Resource getChild(@Nonnull String name) {
        String normalizedName = normalizeChildName(name);
        String finalName = StringUtils.defaultIfEmpty(injectPrefixAndPostfix(normalizedName), CoreConstants.SELF_PATH);
        return base.getChild(finalName);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getResourceType() {
        return base.getResourceType();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String getResourceSuperType() {
        return base.getResourceSuperType();
    }

    /**
     * {@inheritDoc} Specific for the current implementation, returns {@code true} if only there are children having
     * names matched by the given {@code prefix} and/or {@code postfix}
     */
    @Override
    public boolean hasChildren() {
        return listChildren().hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResourceType(String value) {
        return base.isResourceType(value);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ResourceMetadata getResourceMetadata() {
        return base.getResourceMetadata();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ResourceResolver getResourceResolver() {
        return base.getResourceResolver();
    }

    /**
     * {@inheritDoc} Specific for the current implementation, exposes only those properties of the resource that match
     * the given {@code prefix} and/or {@code postfix}
     */
    @Nonnull
    @Override
    public ValueMap getValueMap() {
        if (filteredValueMap == null) {
            filteredValueMap = filter(base.getValueMap(), prefix, postfix);
        }
        return filteredValueMap;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public <T> T adaptTo(@Nonnull Class<T> adapterClass) {
        if (ValueMap.class.equals(adapterClass)) {
            return adapterClass.cast(getValueMap());
        }
        return super.adaptTo(adapterClass);
    }

    /* ---------------
       Utility methods
       --------------- */

    /**
     * Called from {@link FilteredResourceDecorator#getChild(String)} to produce a normalized child address: the one
     * which has all {@code ..}-s and {@code ./}-s, etc. replaced with actual path elements
     * @param name String argument as passed to {@link Resource#getChild(String)}
     * @return A nullable string value
     */
    private String normalizeChildName(String name) {
        if (!StringUtils.contains(name, CoreConstants.SEPARATOR_SLASH)
            && !StringUtils.equalsAny(name, CoreConstants.SELF_PATH, CoreConstants.PARENT_PATH)) {
            return name;
        }
        String normalizedName = StringUtils.startsWith(name, CoreConstants.SEPARATOR_SLASH)
            ? name
            : base.getPath() + CoreConstants.SEPARATOR_SLASH + name;
        return ResourceUtil.normalize(normalizedName);
    }

    /**
     * Called from {@link FilteredResourceDecorator#getChild(String)} to inject a possible prefix and/or postfix
     * specified for the current decorator, into the path (a path chunk) to the child resource. If the {@code name}
     * argument is a complex (slash-separated) path, the method takes care of prefixing only the chunk of the path that
     * refers to an immediate child of the current resource
     * @param name String argument as passed to {@link Resource#getChild(String)}
     * @return A nullable string value
     */
    private String injectPrefixAndPostfix(String name) {
        if (StringUtils.startsWith(name, base.getPath() + CoreConstants.SEPARATOR_SLASH)) {
            String childChunk = StringUtils.substring(name, base.getPath().length() + 1);
            String descendantChunk = StringUtils.EMPTY;
            if (StringUtils.contains(childChunk, CoreConstants.SEPARATOR_SLASH)) {
                descendantChunk = childChunk.substring(childChunk.indexOf(CoreConstants.SEPARATOR_SLASH));
                childChunk = StringUtils.substringBefore(childChunk, CoreConstants.SEPARATOR_SLASH);
            }
            return base.getPath() + CoreConstants.SEPARATOR_SLASH + injectPrefixAndPostfix(childChunk) + descendantChunk;
        }
        if (StringUtils.contains(name, CoreConstants.SEPARATOR_SLASH)) {
            return name;
        }
        String result = name;
        if (StringUtils.isNotEmpty(prefix) && !StringUtils.startsWith(name, prefix)) {
            result = prefix + name;
        }
        if (StringUtils.isNotEmpty(postfix) && !StringUtils.endsWith(name, postfix)) {
            result += postfix;
        }
        return result;
    }

    /**
     * Based on the given {@link ValueMap}, produces a new {@code ValueMap} containing only keys matched by the given
     * {@code prefix} and/or {@code postfix}. The prefix and postfix substrings are removed from the keys if the
     * resulting value map
     * @param source  The original value map
     * @param prefix  String value representing an optional prefix
     * @param postfix String value representing an optional postfix
     * @return A new {@code ValueMap} instance
     */
    private static ValueMap filter(ValueMap source, String prefix, String postfix) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            if (!isMatchByPrefixOrPostfix(entry.getKey(), prefix, postfix)) {
                continue;
            }
            result.put(clearPrefixOrPostfix(entry.getKey(), prefix, postfix), entry.getValue());
        }
        result.put(
            JcrConstants.JCR_PRIMARYTYPE,
            source.getOrDefault(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED));

        if (source.containsKey(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY)) {
            result.put(
                JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
                source.get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY));
        }
        return new ValueMapDecorator(result);
    }

    /**
     * Returns whether the given property name is matched by the provided prefix or postfix
     * @param property String value representing the property name
     * @param prefix   String value representing an optional prefix
     * @param postfix  String value representing an optional postfix
     * @return True or false
     */
    private static boolean isMatchByPrefixOrPostfix(String property, String prefix, String postfix) {
        if (StringUtils.isNotEmpty(prefix) && StringUtils.isNotEmpty(postfix)) {
            return isMatchByPrefix(property, prefix) && isMatchByPostfix(property, postfix);
        }
        if (StringUtils.isNotEmpty(prefix) && StringUtils.isEmpty(postfix)) {
            return isMatchByPrefix(property, prefix);
        }
        if (StringUtils.isEmpty(prefix) && StringUtils.isNotEmpty(postfix)) {
            return isMatchByPostfix(property, postfix);
        }
        return true;
    }

    /**
     * Returns whether the given property name is matched by the provided prefix. The possible presence of a namespace
     * in the property name is taken into account
     * @param property String value representing the property name
     * @param prefix   String value representing an optional prefix
     * @return True or false
     */
    private static boolean isMatchByPrefix(String property, String prefix) {
        if (StringUtils.contains(property, CoreConstants.SEPARATOR_COLON)) {
            return StringUtils.startsWith(
                StringUtils.substringAfter(property, CoreConstants.SEPARATOR_COLON),
                prefix);
        }
        return StringUtils.startsWith(property, prefix);
    }

    /**
     * Returns whether the given property name is matched by the provided postfix
     * @param property String value representing the property name
     * @param postfix  String value representing an optional postfix
     * @return True or false
     */
    private static boolean isMatchByPostfix(String property, String postfix) {
        return StringUtils.endsWith(property, postfix);
    }

    /**
     * Removes the given prefix and/or postfix from the provided property name if they are present
     * @param property String value representing the property name
     * @param prefix   String value representing an optional prefix
     * @param postfix  String value representing an optional postfix
     * @return String value
     */
    private static String clearPrefixOrPostfix(String property, String prefix, String postfix) {
        String result = property;
        if (StringUtils.isNotEmpty(prefix)) {
            result = clearPrefix(result, prefix);
        }
        if (StringUtils.isNotEmpty(postfix)) {
            result = clearPostfix(result, postfix);
        }
        return result;
    }

    /**
     * Removes the given prefix from the provided property name. The presence of the possible namespace is taken into
     * account
     * @param property String value representing the property name
     * @param prefix   String value representing an optional prefix
     * @return String value
     */
    private static String clearPrefix(String property, String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return property;
        }
        if (StringUtils.contains(property, CoreConstants.SEPARATOR_COLON)
            && StringUtils.startsWith(StringUtils.substringAfter(property, CoreConstants.SEPARATOR_COLON), prefix)) {

            return StringUtils.substringBefore(property, CoreConstants.SEPARATOR_COLON)
                + CoreConstants.SEPARATOR_COLON
                + StringUtils.substringAfter(property, CoreConstants.SEPARATOR_COLON + prefix);
        }
        return StringUtils.removeStart(property, prefix);
    }

    /**
     * Removes the given postfix from the provided property name
     * @param property String value representing the property name
     * @param postfix  String value representing an optional postfix
     * @return String value
     */
    private static String clearPostfix(String property, String postfix) {
        return StringUtils.removeEnd(property, postfix);
    }

}
