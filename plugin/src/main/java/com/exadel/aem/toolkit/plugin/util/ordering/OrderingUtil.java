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

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Streams;

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.plugin.adapters.MemberRankingSetting;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;

public class OrderingUtil {

    private static final String BUILTIN_HANDLERS_ROOT = "com.exadel.aem.toolkit.plugin.handlers";

    private OrderingUtil() {
    }

    /* ---------------
       Sorting methods
       --------------- */

    public static <T> List<T> sortHandlers(List<T> handlers) {
        if (handlers.size() < 2) {
            return handlers;
        }

        List<Orderable<T>> orderableHandlers = new ArrayList<>();
        List<T> nonOrderableHandlers = new ArrayList<>();
        for (T handler : handlers) {
            if (handler.getClass().isAnnotationPresent(Handles.class)) {
                orderableHandlers.add(new Orderable<>(handler.getClass().getName(), handler));
            } else {
                nonOrderableHandlers.add(handler);
            }
        }

        for (int i = 0; i < orderableHandlers.size(); i++) {
            Handles handles = orderableHandlers.get(i).getValue().getClass().getDeclaredAnnotation(Handles.class);
            if (!_Default.class.equals(handles.before())) {
                Orderable<T> before = find(handles.before().getName(), orderableHandlers);
                orderableHandlers.get(i).setBefore(before);
            }
            if (!_Default.class.equals(handles.after())) {
                Orderable<T> after = find(handles.after().getName(), orderableHandlers);
                orderableHandlers.get(i).setAfter(after);
            }
        }

        Stream<T> sortedStream = new TopologicalSorter<>(orderableHandlers).topologicalSort().stream().map(Orderable::getValue);
        return Streams.concat(nonOrderableHandlers.stream(), sortedStream).collect(Collectors.toList());
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
                    Orderable<Source> before = find(createName(
                        classMemberBefore,
                        sources.get(i).adaptTo(MemberSource.class).getDeclaringClass()),
                        list);
                    list.get(i).setBefore(before);
                }
                ClassMember classMemberAfter = place.after();
                if (StringUtils.isNotBlank(classMemberAfter.value())) {
                    Orderable<Source> after = find(createName(
                        classMemberAfter,
                        sources.get(i).adaptTo(MemberSource.class).getDeclaringClass()),
                        list);
                    list.get(i).setAfter(after);
                }
            }
        }

        return new TopologicalSorter<>(list).topologicalSort().stream()
            .map(Orderable::getValue)
            .collect(Collectors.toList());
    }

    private static <T> Orderable<T> find(String find, List<Orderable<T>> list) {
        for (Orderable<T> orderable : list) {
            if (orderable.getName().equals(find)) {
                return orderable;
            }
        }
        return null;
    }

    private static String createName(Source source) {
        return createName(source.adaptTo(MemberSource.class).getDeclaringClass(), source.getName());
    }

    private static String createName(ClassMember classMember, Class<?> defaultClass) {
        if (_Default.class.equals(classMember.source())) {
            return createName(defaultClass, classMember.value());
        }
        return createName(classMember.source(), classMember.value());
    }

    private static String createName(Class<?> cls, String name) {
        return cls.getName() + DialogConstants.EXTENSION_SEPARATOR + name;
    }


    /* -----------------
       Comparing methods
       ----------------- */

    /**
     * Facilitates ordering {@code Member} instances according to their optional {@link DialogField} annotations'
     * ranking values and then their class affiliation
     * @param f1 First comparison member, non-null
     * @param f2 Second comparison member, non-null
     * @return Integer value per {@code Comparator#compare(Object, Object)} convention
     */
    public static int compareByRank(Source f1, Source f2)  {
        if (f1 != null && f2 == null) {
            return -1;
        } else if (f1 == null && f2 != null) {
            return 1;
        } else if (f1 == null) {
            return 0;
        }

        int rank1 = f1.adaptTo(MemberRankingSetting.class).getRanking();
        int rank2 = f2.adaptTo(MemberRankingSetting.class).getRanking();
        if (rank1 != rank2) {
            return Integer.compare(rank1, rank2);
        }
        if (f1.adaptTo(MemberSource.class).getDeclaringClass() != f2.adaptTo(MemberSource.class).getDeclaringClass()) {
            if (ClassUtils.isAssignable(
                f1.adaptTo(MemberSource.class).getDeclaringClass(),
                f2.adaptTo(MemberSource.class).getDeclaringClass())) {
                return 1;
            }
            if (ClassUtils.isAssignable(
                f2.adaptTo(MemberSource.class).getDeclaringClass(),
                f1.adaptTo(MemberSource.class).getDeclaringClass())) {
                return -1;
            }
        }
        return 0;
    }

    /**
     * Facilitates ordering {@code Source} instances according to their class affiliation (if both fields' classes
     * are of the same inheritance tree, a field from the senior class goes first)
     * @param f1 First comparison member, non-null
     * @param f2 Second comparison member, non-null
     * @return Integer value per {@code Comparator#compare(Object, Object)} convention
     */
    public static int compareByOrigin(Source f1, Source f2) {
        if (f1 != null && f2 == null) {
            return -1;
        } else if (f1 == null && f2 != null) {
            return 1;
        } else if (f1 == null) {
            return 0;
        }

        Class<?> f1Class = f1.adaptTo(Member.class).getDeclaringClass();
        Class<?> f2Class = f2.adaptTo(Member.class).getDeclaringClass();
        if (f1Class != f2Class) {
            if (ClassUtils.isAssignable(f1Class, f2Class)) {
                return 1;
            }
            if (ClassUtils.isAssignable(f2Class, f1Class)) {
                return -1;
            }
        }
        return 0;
    }

    /**
     * Facilitates ordering {@code Handler} instances in the way that handlers are sorted in the alphabetic order of
     * their names, and above this, built-handlers come before the custom ones
     * @param h1 First comparison member, non-null
     * @param h2 Second comparison member, non-null
     * @return Integer value per {@code Comparator#compare(Object, Object)} convention
     */
    public static int compareByOrigin(Handler h1, Handler h2) {
        if (h1 != null && h2 == null) {
            return -1;
        } else if (h1 == null && h2 != null) {
            return 1;
        } else if (h1 == null) {
            return 0;
        }

        if (h1.getClass().getPackage().getName().startsWith(BUILTIN_HANDLERS_ROOT)
            && !h2.getClass().getPackage().getName().startsWith(BUILTIN_HANDLERS_ROOT)) {
            return -1;
        } else if (!h1.getClass().getPackage().getName().startsWith(BUILTIN_HANDLERS_ROOT)
            && h2.getClass().getPackage().getName().startsWith(BUILTIN_HANDLERS_ROOT)) {
            return 1;
        }
        return h1.getClass().getName().compareTo(h2.getClass().getName());
    }

}
