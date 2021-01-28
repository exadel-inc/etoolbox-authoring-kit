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

package com.exadel.aem.toolkit.plugin.handlers.lists;

import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.annotations.lists.ListItem;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;

/**
 * The {@code Handler} for adding listItem property to a TouchUI dialog
 */
public class ListItemHandler implements BiConsumer<Target, Class<?>> {
    @Override
    public void accept(Target target, Class<?> componentClass) {
        if (componentClass.isAnnotationPresent(ListItem.class)) {
            target.attribute(DialogConstants.PN_AAT_LIST_ITEM, true);
        }
    }
}
