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

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.markers._Default;

public class OrderingUtil {

    public static <T> List<T> sortHandlers(List<T> handlers) {
        if (handlers.size() < 2) {
            return handlers;
        }

        List<Orderable<T>> list = new ArrayList<>(handlers.size());
        for (T handler : handlers) {
            list.add(new Orderable<>(handler.getClass().getName(), handler));
        }

        for (int i = 0; i < handlers.size(); i++) {
            Handles handles = handlers.get(i).getClass().getDeclaredAnnotation(Handles.class);
            if (!_Default.class.equals(handles.before())) {
                Orderable<T> before = find(handles.before().getName(), list);
                list.get(i).setBefore(before);
            }
            if (!_Default.class.equals(handles.after())) {
                Orderable<T> after = find(handles.after().getName(), list);
                list.get(i).setAfter(after);
            }
        }

        return new TopologicalSorter<>(list).topologicalSort().stream()
            .map(Orderable::getValue)
            .collect(Collectors.toList());
    }

    public static List<Source> sortMembers(List<Source> sources) {
        if (sources.size() < 2) {
            return sources;
        }

        List<Orderable<Source>> list = new ArrayList<>(sources.size());
        for (Source source : sources) {
            list.add(new Orderable<>(createName(source), source));
        }

        for (int i = 0; i < sources.size(); i++) {
            Place place = sources.get(i).adaptTo(Place.class);
            if (place != null) {
                ClassMember classMemberBefore = place.before();
                if (StringUtils.isNotBlank(classMemberBefore.value())) {
                    Orderable<Source> before = find(createName(classMemberBefore, sources.get(i).getDeclaringClass()), list);
                    list.get(i).setBefore(before);
                }
                ClassMember classMemberAfter = place.after();
                if (StringUtils.isNotBlank(classMemberAfter.value())) {
                    Orderable<Source> after = find(createName(classMemberAfter, sources.get(i).getDeclaringClass()), list);
                    list.get(i).setAfter(after);
                }
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

    private static String createName(Source source) {
        return createName(source.getDeclaringClass(), source.getName());
    }

    private static String createName(ClassMember classMember, Class<?> defaultClass) {
        if (_Default.class.equals(classMember.source())) {
            return createName(defaultClass, classMember.value());
        }
        return createName(classMember.source(), classMember.value());
    }

    private static String createName(Class<?> cls, String name) {
        return cls.getName() + "." + name;
    }

    private OrderingUtil() {

    }
}
