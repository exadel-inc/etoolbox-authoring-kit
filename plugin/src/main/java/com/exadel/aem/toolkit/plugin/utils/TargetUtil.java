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
package com.exadel.aem.toolkit.plugin.utils;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.handlers.Target;

/**
 * Contains utility methods for managing {@link Target} instances used in the rendering of Granite UI entities
 */
public class TargetUtil {

    /**
     * Default (instantiation-restricting) constructor
     */
    private TargetUtil() {
    }

    /**
     * Stores the reference to an extra client library into the {@code Target} object representing a Touch UI dialog
     * @param target    Accumulating {@code Target} object
     * @param clientLib A string representing the client library to add to the current {@code Target}
     */
    public static void populateClientLibrary(Target target, String clientLib) {
        Target dialogRoot = target.findParent(t -> DialogConstants.NN_ROOT.equals(t.getName()));
        if (dialogRoot == null) {
            return;
        }
        String extraClientlibs = dialogRoot.getAttribute(DialogConstants.PN_EXTRA_CLIENTLIBS);
        if (StringUtils.isEmpty(extraClientlibs)) {
            dialogRoot.attribute(DialogConstants.PN_EXTRA_CLIENTLIBS, new String[]{clientLib});
        } else {
            Set<String> extraClientlibSet = StringUtil.parseSet(extraClientlibs);
            extraClientlibSet.add(clientLib);
            dialogRoot.attribute(DialogConstants.PN_EXTRA_CLIENTLIBS, StringUtil.format(extraClientlibSet, String.class));
        }
    }
}
