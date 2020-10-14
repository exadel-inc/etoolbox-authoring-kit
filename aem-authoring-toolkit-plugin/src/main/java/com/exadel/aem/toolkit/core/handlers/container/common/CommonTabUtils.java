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
package com.exadel.aem.toolkit.core.handlers.container.common;

import com.exadel.aem.toolkit.api.annotations.container.PlaceOn;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.core.exceptions.InvalidTabException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.PluginObjectPredicates;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommonTabUtils {
    private CommonTabUtils() {
    }

    /**
     * The predicate to match a {@code Field} against particular {@code Tab}
     *
     * @param field        {@link Field} instance to analyze
     * @param tab          {@link Tab} annotation to analyze
     * @param isDefaultTab True if the current tab accepts fields for which no tab was specified; otherwise, false
     * @return True or false
     */
    private static boolean isFieldForTab(Field field, Tab tab, boolean isDefaultTab) {
        if (!field.isAnnotationPresent(PlaceOnTab.class) && !field.isAnnotationPresent(PlaceOn.class)) {
            return isDefaultTab;
        }
        if (field.isAnnotationPresent(PlaceOn.class)) {
            return tab.title().equalsIgnoreCase(field.getAnnotation(PlaceOn.class).value());
        }
        return tab.title().equalsIgnoreCase(field.getAnnotation(PlaceOnTab.class).value());
    }

    public static void handleInvalidTabException(List<Field> allFields) {
        for (Field field : allFields) {
            if (field.isAnnotationPresent(PlaceOnTab.class)) {
                PluginRuntime.context().getExceptionHandler().handle(new InvalidTabException(field.isAnnotationPresent(PlaceOnTab.class) ? field.getAnnotation(PlaceOnTab.class).value() : StringUtils.EMPTY));
            }
            if (field.isAnnotationPresent(PlaceOn.class)) {
                PluginRuntime.context().getExceptionHandler().handle(new InvalidTabException(field.isAnnotationPresent(PlaceOn.class) ? field.getAnnotation(PlaceOn.class).value() : StringUtils.EMPTY));
            } else {
                PluginRuntime.context().getExceptionHandler().handle(new InvalidTabException(StringUtils.EMPTY));
            }
        }
    }

    public static List<List<Field>> getStoredCurrentTabFields(List<Field> allFields, TabInstance currentTabInstance, final boolean isFirstTab) {
        List<Field> storedCurrentTabFields = currentTabInstance.getFields();
        List<Field> moreCurrentTabFields = allFields.stream()
                .filter(field1 -> CommonTabUtils.isFieldForTab(field1, currentTabInstance.getTab(), isFirstTab))
                .collect(Collectors.toList());
        boolean needResort = !storedCurrentTabFields.isEmpty() && !moreCurrentTabFields.isEmpty();
        storedCurrentTabFields.addAll(moreCurrentTabFields);
        if (needResort) {
            storedCurrentTabFields.sort(PluginObjectPredicates::compareByRanking);
        }
        allFields.removeAll(moreCurrentTabFields);
        List<List<Field>> finalList = new ArrayList<>();
        finalList.add(storedCurrentTabFields);
        finalList.add(allFields);
        return finalList;
    }
}
