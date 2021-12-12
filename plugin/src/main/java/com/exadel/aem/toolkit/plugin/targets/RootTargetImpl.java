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
package com.exadel.aem.toolkit.plugin.targets;

import com.exadel.aem.toolkit.plugin.handlers.placement.registries.MembersRegistry;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@link RootTarget} to provide management of a "root" target entity for rendering AEM components and their
 * configurations
 */
class RootTargetImpl extends TargetImpl implements RootTarget {

    private MembersRegistry dialogMembers;

    /**
     * Default constructor. Creates an unattached {@code Target} instance with the {@code jcr:root} node name
     */
    public RootTargetImpl() {
        super(DialogConstants.NN_ROOT, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MembersRegistry getMembers() {
        return dialogMembers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMembers(MembersRegistry value) {
        dialogMembers = value;
    }
}
