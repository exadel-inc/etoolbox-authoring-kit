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
package com.exadel.aem.toolkit.plugin.handlers.common;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.DesignDialog;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.plugin.handlers.layouts.ContainerHandlers;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.ScopeUtil;
import com.exadel.aem.toolkit.plugin.writers.DialogLayout;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a
 * {@link Source} object that define the {@code cq:dialog} and {@code cq:design_dialog} settings nodes of an AEM component
 */
public class CqDialogHandler implements BiConsumer<Source, Target> {

    private static final String TITLE_MISSING_EXCEPTION_MESSAGE = "Title property is missing for dialog in class ";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        Annotation dialogAnnotation = Scopes.CQ_DIALOG.equals(target.getScope())
            ? source.adaptTo(Dialog.class)
            : source.adaptTo(DesignDialog.class);

        target
            .attributes(
                dialogAnnotation,
                AnnotationUtil
                    .getPropertyMappingFilter(dialogAnnotation)
                    .and(member -> ScopeUtil.fits(target.getScope(), member)))
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.DIALOG);
        populateTitleProperty(source, target);

        DialogLayout dialogLayout = getLayout(source, target.getScope());
        ContainerHandlers.forLayout(dialogLayout).accept(source, target);
    }

    /**
     * Called by {@link CqDialogHandler#accept(Source, Target)} to fulfill the dialog {@code title} source disambiguation.
     * If {@code title} is set at {@code AemComponent} level, one does not need to specify it separately for a {@code Dialog}
     * or {@code DesignDialog}. However, if {@code AemComponent} is missing, {@code title} must be set to a non-blank value
     * in the dialog annotation
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    private static void populateTitleProperty(Source source, Target target) {
        String currentTitleValue = null;
        if (source.adaptTo(Dialog.class) != null) {
            currentTitleValue = source.adaptTo(Dialog.class).title();
        }
        if (StringUtils.isEmpty(currentTitleValue) && source.adaptTo(DesignDialog.class) != null) {
            currentTitleValue = source.adaptTo(DesignDialog.class).title();
        }
        if (StringUtils.isEmpty(currentTitleValue) && source.adaptTo(AemComponent.class) == null) {
            ValidationException ex = new ValidationException(TITLE_MISSING_EXCEPTION_MESSAGE + source.getName());
            PluginRuntime.context().getExceptionHandler().handle(ex);
        } else if (StringUtils.isEmpty(currentTitleValue)) {
            target.attribute(DialogConstants.PN_JCR_TITLE, source.adaptTo(AemComponent.class).title());
        }
    }

    /**
     * Retrieves the {@link DialogLayout} for the component class provided. The value is computed based
     * on layout annotations provided for the component class. If no specific annotations found, the default layout,
     * which is {@link DialogLayout#FIXED_COLUMNS}, returned
     * @param source {@code Source} object used for data retrieval
     * @param scope  Value indicating whether to look for a {@code Dialog}, or {@code DesignDialog} annotation as a possible
     *               source of layout
     * @return {@code DialogLayout} value
     */
    @SuppressWarnings("deprecation") // Processing of Dialog#tabs is retained for compatibility and will be removed
    // in a version after 2.0.2
    private static DialogLayout getLayout(Source source, String scope) {
        DialogLayout result = DialogLayout.FIXED_COLUMNS;

        List<Class<?>> hierarchy = ClassUtil.getInheritanceTree(source.adaptTo(Class.class), true);
        ListIterator<Class<?>> hierarchyIterator = hierarchy.listIterator(hierarchy.size());

        while (hierarchyIterator.hasPrevious()) {
            Class<?> current = hierarchyIterator.previous();
            if (scope.equals(Scopes.CQ_DIALOG)
                && current.isAnnotationPresent(Dialog.class)
                && current.getDeclaredAnnotation(Dialog.class).tabs().length > 0) {
                return DialogLayout.TABS;
            }
            Tabs tabsAnnotation = current.getDeclaredAnnotation(Tabs.class);
            if (tabsAnnotation != null && tabsAnnotation.value().length > 0) {
                return DialogLayout.TABS;
            }
            Accordion accordionAnnotation = current.getDeclaredAnnotation(Accordion.class);
            if (accordionAnnotation != null && accordionAnnotation.value().length > 0) {
                return DialogLayout.ACCORDION;
            }
            result = getLayoutFromNestedClasses(source.adaptTo(Class.class));
            if (!result.equals(DialogLayout.FIXED_COLUMNS)) {
                return result;
            }
        }

        return result;
    }

    /**
     * Called by {@link CqDialogHandler#getLayout(Source, String)} to search for an appropriate {@code DialogLayout} value
     * in case it is defined as an annotation to nested class. If such annotation is found, the appropriate value is
     * immediately returned; otherwise the fallback value {@link DialogLayout#FIXED_COLUMNS} is returned
     * @param componentClass The {@code Class} being processed
     * @return {@code DialogLayout} value
     */
    @SuppressWarnings("deprecation") // Processing of container.Tab is retained for compatibility and will be removed
    // in a version after 2.0.2
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
