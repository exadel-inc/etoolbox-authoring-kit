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

package com.exadel.aem.toolkit.plugin.util.writer;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import javax.xml.transform.Transformer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.DesignDialog;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.meta.DialogAnnotation;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.Scope;
import com.exadel.aem.toolkit.api.handlers.DialogHandler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.assets.dependson.DependsOnTabHandler;
import com.exadel.aem.toolkit.plugin.handlers.container.DialogContainer;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginAnnotationUtility;
import com.exadel.aem.toolkit.plugin.util.PluginReflectionUtility;

/**
 * The {@link PackageEntryWriter} implementation for storing AEM TouchUI dialog definition (writes data to the
 * {@code _cq_dialog.xml} file within the current component folder before package is uploaded
 */
class CqDialogWriter extends PackageEntryWriter {

    private final Scope scope;
    /**
     * Basic constructor
     *
     * @param transformer {@code Transformer} instance used to serialize XML DOM document to an output stream
     * @param scope Current XmlScope
     */
    CqDialogWriter(Transformer transformer, Scope scope) {
        super(transformer);
        this.scope = scope;
    }

    /**
     * Gets {@code XmlScope} value of current {@code PackageEntryWriter} implementation
     * @return {@link Scope} value
     */
    @Override
    Scope getScope() {
        return scope;
    }

    /**
     * Gets whether current {@code Class} is eligible for populating {@code _cq_dialog.xml} structure
     * @param componentClass The {@code Class} under consideration
     * @return True if current {@code Class} is annotated with {@link Dialog} or {@link DesignDialog}; otherwise, false
     */
    @Override
    boolean canProcess(Class<?> componentClass) {
        return Scope.CQ_DIALOG.equals(scope)
            ? componentClass.isAnnotationPresent(Dialog.class)
            : componentClass.isAnnotationPresent(DesignDialog.class);
    }

    /**
     * Overrides {@link PackageEntryWriter#populateTarget(Class, Target)} abstract method to write down contents
     * of {@code _cq_dialog.xml} file. To the targetFacade node, several XML building routines are applied in sequence: the predefined
     * dialog container builder, the common properties' writer, {@code DependsOn} handlers and any {@code CustomHandler}s defined for
     * this component class
     *
     * @param componentClass The {@code Class} being processed
     * @param target   The targetFacade element of DOM {@link Document} to feed data to
     */
    @Override
    void populateTarget(Class<?> componentClass, Target target) {

        Annotation dialogAnnotation = Scope.CQ_DIALOG.equals(scope)
            ? componentClass.getDeclaredAnnotation(Dialog.class)
            : componentClass.getDeclaredAnnotation(DesignDialog.class);

        target
            .attributes(
                dialogAnnotation,
                PluginAnnotationUtility
                    .getPropertyMappingFilter(dialogAnnotation)
                    .and(member -> fitsInScope(member, scope)))
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.DIALOG);

        DialogLayout dialogLayout = getLayout(componentClass, scope);
        DialogContainer.getContainer(dialogLayout).build(componentClass, target);

        new DependsOnTabHandler().accept(componentClass, target);
        if (!classHasCustomDialogAnnotation(componentClass)) {
            return;
        }
        List<DialogAnnotation> customAnnotations = getCustomDialogAnnotations(componentClass);
        PluginRuntime.context().getReflectionUtility().getCustomDialogHandlers().stream()
                .filter(handler -> customAnnotations.stream()
                        .anyMatch(annotation -> customAnnotationMatchesHandler(annotation, handler)))
                .forEach(handler -> handler.accept(componentClass, target));
    }

    /**
     * Retrieves the {@link DialogLayout} for the component class provided. The value is computed based
     * on layout annotations provided for the component class. If no specific annotations found, the default layout,
     * which is {@link DialogLayout#FIXED_COLUMNS}, returned
     * @param componentClass The {@code Class} being processed
     * @param scope Indicates whether to look for a {@code Dialog}, or {@code DesignDialog} annotation as a possible
     *              source of layout
     * @return {@code DialogLayout} value
     */
    private static DialogLayout getLayout(Class<?> componentClass, Scope scope) {
        DialogLayout result = DialogLayout.FIXED_COLUMNS;

        List<Class<?>> hierarchy = PluginReflectionUtility.getClassHierarchy(componentClass, true);
        ListIterator<Class<?>> hierarchyIterator = hierarchy.listIterator(hierarchy.size());

        while (hierarchyIterator.hasPrevious()) {
            Class<?> current = hierarchyIterator.previous();
            if (scope.equals(Scope.CQ_DIALOG)
                && current.isAnnotationPresent(Dialog.class)
                && current.getDeclaredAnnotation(Dialog.class).tabs().length > 0) {
                result = DialogLayout.TABS;
                break;
            }
            Tabs tabsAnnotation = current.getDeclaredAnnotation(Tabs.class);
            if (tabsAnnotation != null && tabsAnnotation.value().length > 0) {
                result = DialogLayout.TABS;
                break;
            }
            Accordion accordionAnnotation = current.getDeclaredAnnotation(Accordion.class);
            if (accordionAnnotation != null && accordionAnnotation.value().length > 0) {
                result = DialogLayout.ACCORDION;
                break;
            }
            result = getLayoutFromNestedClasses(componentClass);
            if (!result.equals(DialogLayout.FIXED_COLUMNS)) {
                break;
            }
        }

        return result;
    }

    /**
     * Called from {@link CqDialogWriter#getLayout(Class, Scope)} to search for an appropriate {@code DialogLayout} value
     * in case it is defined as an annotation to nested class. If such annotation is found, the appropriate value is
     * immediately returned; otherwise the fallback value {@link DialogLayout#FIXED_COLUMNS} is returned
     * @param componentClass The {@code Class} being processed
     * @return {@code DialogLayout} value
     */
    private static DialogLayout getLayoutFromNestedClasses(Class<?> componentClass) {
        if (ArrayUtils.isEmpty(componentClass.getDeclaredClasses())) {
            return DialogLayout.FIXED_COLUMNS;
        }
        for (Class<?> nested : componentClass.getDeclaredClasses()) {
            if (nested.isAnnotationPresent(Tab.class)
                || nested.isAnnotationPresent(com.exadel.aem.toolkit.api.annotations.container.Tab.class)) {
                return DialogLayout.TABS;
            }
            if (nested.isAnnotationPresent(AccordionPanel.class)) {
                return DialogLayout.ACCORDION;
            }
        }
        return DialogLayout.FIXED_COLUMNS;
    }

    /**
     * Gets whether current {@code Class} has a custom dialog annotation attached
     *
     * @param componentClass The {@code Class} being processed
     * @return True or false
     */
    private static boolean classHasCustomDialogAnnotation(Class<?> componentClass) {
        return Arrays.stream(componentClass.getDeclaredAnnotations())
            .anyMatch(a -> a.annotationType().getDeclaredAnnotation(DialogAnnotation.class) != null);
    }

    /**
     * Retrieves list of {@link DialogAnnotation} instances defined for the current {@code Class}
     *
     * @param componentClass The {@code Class} being processed
     * @return List of values, empty or non-empty
     */
    public static List<DialogAnnotation> getCustomDialogAnnotations(Class<?> componentClass) {
        return Arrays.stream(componentClass.getDeclaredAnnotations())
                .filter(annotation -> annotation.annotationType().getDeclaredAnnotation(DialogAnnotation.class) != null)
                .map(annotation -> annotation.annotationType().getDeclaredAnnotation(DialogAnnotation.class))
                .collect(Collectors.toList());
    }

    /**
     * Used while enumerating available {@code CustomDialogHandler}s to set matching between a handler and a {@code CustomDialogAnnotation},
     * since one handler may serve for several annotations, and, optionally, vice versa
     *
     * @param annotation {@link DialogAnnotation} instance
     * @param handler    {@link DialogHandler} instance
     * @return True if the two arguments are "matching" via their properties, otherwise, false
     */
    private static boolean customAnnotationMatchesHandler(DialogAnnotation annotation, DialogHandler handler) {
        if (handler.getClass().isAnnotationPresent(Handles.class)) {
            return Arrays.stream(handler.getClass().getDeclaredAnnotation(Handles.class).value())
                .anyMatch(aClass -> aClass.equals(annotation.getClass()));
        } else {
            return StringUtils.equals(annotation.source(), handler.getName());
        }
    }
}
