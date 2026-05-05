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
package com.exadel.aem.toolkit.core.relay.models;

import java.util.Iterator;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceWrapper;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.relay.utils.RelayResourceHelper;

/**
 * An implementation of {@link Resource} used by the relay provider to expose resources at a mapped source path.
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class RelayResource extends ResourceWrapper {

    private final String path;
    private final ResourceMetadata resourceMetadata;

    /**
     * Creates a new {@code RelayResource} wrapping the provided resource and overriding its path
     * @param original A non-null original {@link Resource} to wrap
     * @param path     A non-null JCR path to expose for this resource
     */
    public RelayResource(@Nonnull Resource original, @Nonnull String path) {
        super(original);
        this.path = StringUtils.stripEnd(path, CoreConstants.SEPARATOR_SLASH);
        // We must create a copy of the resource metadata to avoid the "{@code JcrNodeResourceMetadata is locked}" exception
        // because the {@code original} resource is already locked by Sling
        this.resourceMetadata = new ResourceMetadata();
        this.resourceMetadata.putAll((ResourceMetadata) original.getResourceMetadata().clone());
        this.resourceMetadata.put(ResourceMetadata.RESOLUTION_PATH, this.path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getChild(@Nonnull String relativePath) {
        String fullPath = path + CoreConstants.SEPARATOR_SLASH + StringUtils.strip(relativePath, CoreConstants.SEPARATOR_SLASH);
        return getResourceResolver().getResource(fullPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getName() {
        return path.contains(CoreConstants.SEPARATOR_SLASH)
            ? StringUtils.substringAfterLast(path, CoreConstants.SEPARATOR_SLASH)
            : path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getParent() {
        if (!StringUtils.contains(path, CoreConstants.SEPARATOR_SLASH)) {
            return null;
        }
        String parentPath = StringUtils.substringBeforeLast(path, CoreConstants.SEPARATOR_SLASH);
        if (parentPath.isEmpty()) {
            parentPath = CoreConstants.SEPARATOR_SLASH;
        }
        return getResourceResolver().getResource(parentPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public ResourceMetadata getResourceMetadata() {
        return resourceMetadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull Iterator<Resource> listChildren() {
        return RelayResourceHelper.listChildren(getResource(), path);
    }
}

