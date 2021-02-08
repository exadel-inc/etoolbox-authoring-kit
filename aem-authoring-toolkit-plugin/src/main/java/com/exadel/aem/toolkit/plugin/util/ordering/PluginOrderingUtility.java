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

package com.exadel.aem.toolkit.plugin.util.ordering;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.markers._Default;

public class PluginOrderingUtility {

    public static <T> List<T> sort(List<T> handlers) {
        if (handlers.size() < 2) {
            return handlers;
        }

        List<Orderable<T>> list = new ArrayList<>();
        for (T handler : handlers) {
            list.add(new Orderable<>(handler.getClass().getName(), handler));
        }

        for (int i = 0; i < handlers.size(); i++) {
            list.get(i).setValue(handlers.get(i));

            // This lines should be changed, after "before/after" rules appear in the PlaceOn annotation
            Handles handles = handlers.get(i).getClass().getDeclaredAnnotation(Handles.class);
            if (!_Default.class.equals(handles.before())) {
                Orderable<T> before = find(handles.before().getSimpleName(), list);
                list.get(i).setBefore(before);
            }
            if (!_Default.class.equals(handles.after())) {
                Orderable<T> after = find(handles.after().getSimpleName(), list);
                list.get(i).setAfter(after);
            }
        }

        return new TopologicalSorter<>(list).topologicalSort().stream()
            .map(Orderable::getValue)
            .collect(Collectors.toList());
    }

    // Gets the Orderable object from list to store valid links in before/after fields
    private static <T> Orderable<T> find(String find, List<Orderable<T>> list) {
        for (Orderable<T> orderable : list) {
            if (orderable.getName().equals(find)) {
                return orderable;
            }
        }
        return null;
    }

    private PluginOrderingUtility() {

    }
}
