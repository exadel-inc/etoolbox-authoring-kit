package com.exadel.aem.toolkit.core.handlers.container.common;

import com.exadel.aem.toolkit.api.annotations.container.PlaceOn;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.core.exceptions.InvalidTabException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
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
            storedCurrentTabFields.sort(PluginReflectionUtility.Predicates::compareDialogFields);
        }
        allFields.removeAll(moreCurrentTabFields);
        List<List<Field>> finalList = new ArrayList<>();
        finalList.add(storedCurrentTabFields);
        finalList.add(allFields);
        return finalList;
    }
}
