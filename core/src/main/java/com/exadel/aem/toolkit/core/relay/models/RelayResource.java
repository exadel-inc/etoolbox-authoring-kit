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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceWrapper;

import com.exadel.aem.toolkit.core.relay.utils.ResourceHelper;

public class RelayResource extends ResourceWrapper {

    private final String path;
    private final ResourceMetadata resourceMetadata;

    public RelayResource(@Nonnull Resource original, @Nonnull String path) {
        super(original);
        this.path = path;
        // We must create a copy of the resource metadata to avoid the "{@code JcrNodeResourceMetadata is locked}" exception
        // because the {@code original} resource is already locked by Sling
        this.resourceMetadata = new ResourceMetadata();
        this.resourceMetadata.putAll((ResourceMetadata) original.getResourceMetadata().clone());
        if (this.resourceMetadata.containsKey(ResourceMetadata.RESOLUTION_PATH)) {
            this.resourceMetadata.put(ResourceMetadata.RESOLUTION_PATH, path);
        }
    }

    @Override
    @Nonnull
    public String getPath() {
        return path;
    }

    @Override
    @Nonnull
    public ResourceMetadata getResourceMetadata() {
        return resourceMetadata;
    }

    @Override
    public @Nonnull Iterator<Resource> listChildren() {
        return ResourceHelper.listChildren(getResource(), path);
    }
}

