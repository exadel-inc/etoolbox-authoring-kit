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
package com.exadel.aem.toolkit.plugin.handlers.widgets.common;

import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the common attributes of a Granite dialog component, such as {@code name}, {@code label},
 * {@code description}, etc.
 */
public class DialogFieldAnnotationHandler implements BiConsumer<Source, Target> {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        DialogField dialogField = source.adaptTo(DialogField.class);
        if (dialogField == null) {
            return;
        }
        String name = StringUtils.defaultIfEmpty(
            getNameByDialogFieldProperty(dialogField),
            NamingUtil.stripGetterPrefix(source));
        String slingSuffix = getSlingSuffixByDialogFieldProperty(dialogField);

        String prefix = target.getNamePrefix();
        // A prefix must be ignored in a multifield's descendant field unless this is the "field" container or this is
        // the only multifield's field. However, we respect the "part" of the prefix that might be set already "inside"
        // the multifield by, e.g., a nested fieldset
        if (isCompositeMultifieldField(target)) {
            Target multifieldAncestor = getClosestMultifield(target);
            assert multifieldAncestor != null;
            String ancestorPrefix = multifieldAncestor.getNamePrefix();
            prefix = StringUtils.removeStart(prefix, ancestorPrefix);
        }
        // In case there are multiple sources in multifield container, their "name" values must not be prepended
        // with "./" which is by default
        // see https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/
        // jcr_root/libs/granite/ui/components/coral/foundation/form/multifield/index.html#examples
        if (!isDeepMultifieldDescendant(target)) {
            prefix = mergeWithPrefix(DialogConstants.RELATIVE_PATH_PREFIX, prefix);
        }

        name = mergeWithPrefix(prefix, name)
            + target.getNamePostfix()
            + slingSuffix;
        target.attribute(CoreConstants.PN_NAME, name);
    }

    /**
     * Retrieves the {@code name} value from the provided {@link DialogField} annotation. If the name contains a Sling
     * suffix, on;y the part before a suffix is returned
     * @param dialogField {@code DialogField} instance
     * @return String value
     */
    private static String getNameByDialogFieldProperty(DialogField dialogField) {
        if (StringUtils.isBlank(dialogField.name())) {
            return StringUtils.EMPTY;
        }
        if (CoreConstants.SEPARATOR_SLASH.equals(dialogField.name())
            || DialogConstants.RELATIVE_PATH_PREFIX.equals(dialogField.name())) {
            return DialogConstants.RELATIVE_PATH_PREFIX;
        }
        if (dialogField.name().contains(CoreConstants.SEPARATOR_AT)) {
            return NamingUtil.getValidFieldName(StringUtils.substringBeforeLast(dialogField.name(), CoreConstants.SEPARATOR_AT));
        }
        return NamingUtil.getValidFieldName(dialogField.name());
    }

    /**
     * Retrieves the Sling suffix as stored in the {@code name} property of the provided {@link DialogField} annotation.
     * @param dialogField {@code DialogField} instance
     * @return String value (empty string is returned if no Sling suffix is found)
     */
    private static String getSlingSuffixByDialogFieldProperty(DialogField dialogField) {
        if (!StringUtils.contains(dialogField.name(), CoreConstants.SEPARATOR_AT)) {
            return StringUtils.EMPTY;
        }
        String result = NamingUtil.getValidPlainName(StringUtils.substringAfterLast(dialogField.name(), CoreConstants.SEPARATOR_AT));
        if (StringUtils.isNotEmpty(result)) {
            return CoreConstants.SEPARATOR_AT + result;
        }
        return result;
    }

    /**
     * Called by {@link DialogFieldAnnotationHandler#accept(Source, Target)} to merge parts of a field name avoiding
     * prefix collisions
     * @param left  The left part of the merging, usually a field prefix
     * @param right The right part of the merging, usually a field name
     * @return String value
     */
    private static String mergeWithPrefix(String left, String right) {
        if (StringUtils.isBlank(left)) {
            return right;
        }
        if (DialogConstants.RELATIVE_PATH_PREFIX.equals(left) && DialogConstants.RELATIVE_PATH_PREFIX.equals(right)
            || DialogConstants.RELATIVE_PATH_PREFIX.equals(left) && right.startsWith(DialogConstants.PARENT_PATH_PREFIX)) {
            return right;
        }
        return left + right;
    }

    /* --------------------
       Multifield utilities
       -------------------- */

    /**
     * Called by {@link DialogFieldAnnotationHandler#accept(Source, Target)} to determine if the current target
     * represents a widget inside a composite multifield
     * @param target {@link Target} object being processed
     * @return True or false
     */
    private static boolean isCompositeMultifieldField(Target target) {
        Target multifieldAncestor = getClosestMultifield(target);
        if (multifieldAncestor == null) {
            return false;
        }
        return multifieldAncestor.getAttribute(DialogConstants.PN_COMPOSITE, StringUtils.EMPTY).equals("{Boolean}true");
    }

    /**
     * Called by {@link DialogFieldAnnotationHandler#accept(Source, Target)} to determine if the current target is
     * situated inside a multifield node but is NOT an immediate multifield descendant
     * @param target {@link Target} object being processed
     * @return True or false
     */
    private static boolean isDeepMultifieldDescendant(Target target) {
        Target multifieldAncestor = getClosestMultifield(target);
        if (multifieldAncestor == null) {
            return false;
        }
        return !multifieldAncestor.equals(target.getParent());
    }

    /**
     * Retrieves the closest ancestor of the provided target which represents a multifield node if there is any
     * @param target {@link Target} object being processed
     * @return {@link Target} instance or null
     */
    private static Target getClosestMultifield(Target target) {
        return target.findParent(t -> ResourceTypes.MULTIFIELD.equals(t.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE)));
    }
}
