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
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.DesignDialog;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.Scope;
import com.exadel.aem.toolkit.api.handlers.DialogHandler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.plugin.handlers.assets.dependson.DependsOnTabHandler;
import com.exadel.aem.toolkit.plugin.handlers.layouts.DialogContainer;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginAnnotationUtility;
import com.exadel.aem.toolkit.plugin.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.plugin.util.ordering.OrderingUtil;

/**
 * The {@link PackageEntryWriter} implementation for storing AEM TouchUI dialog definition (writes data to the
 * {@code _cq_dialog.xml} file within the current component folder before package is uploaded
 */
class CqDialogWriter extends PackageEntryWriter {
    private static final String TITLE_MISSING_EXCEPTION_MESSAGE = "Title property is missing for dialog in class ";

    private final Scope scope;

    /**
     * Basic constructor
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
        populateTitleProperty(componentClass, target);

        DialogLayout dialogLayout = getLayout(componentClass, scope);
        DialogContainer.getContainer(dialogLayout).build(componentClass, target);

        new DependsOnTabHandler().accept(componentClass, target);

        List<DialogHandler> handlers = PluginRuntime.context().getReflectionUtility().getCustomDialogHandlers().stream()
                .filter(dialogHandler -> dialogHandler.getClass().isAnnotationPresent(Handles.class)
                && Arrays.stream(dialogHandler.getClass().getDeclaredAnnotation(Handles.class).value()).anyMatch(componentClass::isAnnotationPresent))
            .collect(Collectors.toList());

        OrderingUtil.sortHandlers(handlers).forEach(handler -> handler.accept(componentClass, target));
    }

    /**
     * Called by {@link CqDialogWriter#populateTarget(Class, Target)} to settle the dialog {@code title} issue.
     * If {@code title} is set at {@code AemComponent} level, one does not need to specify it separately for a {@code Dialog}
     * or {@code DesignDialog}. However if {@code AemComponent} is missing, {@code title} must be set to a non-blank value
     * in the dialog annotation
     * @param componentClass The {@code Class} being processed
     * @param target         The root element of DOM {@link Document} to feed data to
     */
    private static void populateTitleProperty(Class<?> componentClass, Target target) {
        String currentTitleValue = null;
        if (componentClass.isAnnotationPresent(Dialog.class)) {
            currentTitleValue = componentClass.getDeclaredAnnotation(Dialog.class).title();
        }
        if (StringUtils.isEmpty(currentTitleValue) && componentClass.isAnnotationPresent(DesignDialog.class)) {
            currentTitleValue = componentClass.getDeclaredAnnotation(DesignDialog.class).title();
        }
        if (StringUtils.isEmpty(currentTitleValue) && !componentClass.isAnnotationPresent(AemComponent.class)) {
            ValidationException ex = new ValidationException(TITLE_MISSING_EXCEPTION_MESSAGE + componentClass.getName());
            PluginRuntime.context().getExceptionHandler().handle(ex);
        } else if (StringUtils.isEmpty(currentTitleValue)) {
            target.attribute(DialogConstants.PN_JCR_TITLE, componentClass.getDeclaredAnnotation(AemComponent.class).title());
        }
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
}
