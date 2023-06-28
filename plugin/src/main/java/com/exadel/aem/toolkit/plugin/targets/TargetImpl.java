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
package com.exadel.aem.toolkit.plugin.targets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.adapters.AdaptationBase;
import com.exadel.aem.toolkit.plugin.metadata.Metadata;
import com.exadel.aem.toolkit.plugin.metadata.Property;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;
import com.exadel.aem.toolkit.plugin.utils.StringUtil;

/**
 * Implements {@link Target} to manage a tree-like data structure that is further rendered in a Granite UI component
 * or component configurations
 */
@SuppressWarnings("HiddenField") // Allows to pass {@code name} arguments in attribute constructors
class TargetImpl extends AdaptationBase<Target> implements Target, LegacyHandlerAcceptor {

    static final BinaryOperator<String> DEFAULT_ATTRIBUTE_MERGER = (first, second) -> StringUtils.isNotBlank(second) ? second : first;

    /* -----------------------------
       Local fields and constructors
       ----------------------------- */

    private final Map<String, String> attributes;
    private final List<Target> children;

    private String name;
    private Target parent;
    private String prefix;
    private String postfix;
    private String scope;

    /**
     * Initializes a class instance with the instance name and parent reference specified
     * @param name   A non-blank string representing the name of the new instance
     * @param parent Nullable {@code Target} object that will serve as the parent reference
     */
    TargetImpl(String name, Target parent) {
        super(Target.class);
        this.name = name;
        this.parent = parent;
        this.attributes = new HashMap<>();
        this.children = new LinkedList<>();
        this.scope = parent != null ? parent.getScope() : Scopes.COMPONENT;
        this.attributes.put(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_UNSTRUCTURED);
    }

    /* -----------------
       Naming operations
       ----------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNamePrefix() {
        if (getParent() == null) {
            return StringUtils.defaultString(prefix);
        }
        return getParent().getNamePrefix() + StringUtils.defaultString(prefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target namePrefix(String value) {
        prefix = NamingUtil.getValidFieldPrefix(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNamePostfix() {
        if (getParent() == null) {
            return StringUtils.defaultString(postfix);
        }
        return StringUtils.defaultString(postfix) + getParent().getNamePostfix();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target namePostfix(String value) {
        postfix = NamingUtil.getValidFieldPostfix(value);
        return this;
    }


    /* ---------------
       Basic accessors
       --------------- */

    @Override
    public Target getParent() {
        return parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Target> getChildren() {
        return children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        if (!children.isEmpty() || attributes.size() > 1) {
            return false;
        }
        return attributes.isEmpty()
            || attributes.getOrDefault(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_UNSTRUCTURED)
            .equals(DialogConstants.NT_UNSTRUCTURED);
    }

    /* ----------------
       Scope operations
       ---------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScope() {
        return scope;
    }

    /**
     * Assigns a scope value to the current instance
     * @param value String value
     */
    void setScope(String value) {
        scope = value;
    }


    /* ---------------
       Path operations
       --------------- */

    @Override
    public Target getTarget(String path) {
        return getTargetInternal(path, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target getOrCreateTarget(String path) {
        return getTargetInternal(path, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target createTarget(String path) {
        if (path == null) {
            return null;
        }
        boolean isEscaped = path.startsWith(DialogConstants.DOUBLE_QUOTE) && path.endsWith(DialogConstants.DOUBLE_QUOTE);
        String effectivePath = isEscaped ? StringUtils.strip(path, DialogConstants.DOUBLE_QUOTE) : path;

        if (!isEscaped && PathSplitHelper.of(effectivePath).isSplittable()) {
            Target existingTarget = getTarget(effectivePath);
            if (existingTarget != null && !this.equals(existingTarget)) {
                removeTarget(effectivePath);
            } else if (this.equals(existingTarget)) {
                return this;
            }
            return getOrCreateTarget(effectivePath);
        }
        if (CoreConstants.PARENT_PATH.equals(effectivePath)) {
            return getParent() != null ? getParent() : null;
        }
        if (CoreConstants.SELF_PATH.equals(effectivePath)) {
            return this;
        }
        String effectiveName = NamingUtil.getUniqueName(effectivePath, CoreConstants.NN_ITEM, this);
        Target child = new TargetImpl(effectiveName, this);
        children.add(child);
        return child;
    }

    /**
     * Retrieves or creates as necessary a {@code Target} object related to the current instance by the provided path
     * @param path            String value, non-blank
     * @param createIfMissing True to create a {@code Target} for the unmatched path segment; otherwise, false
     * @return New {@code Target} instance
     */
    private Target getTargetInternal(String path, boolean createIfMissing) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        boolean isEscaped = path.startsWith(DialogConstants.DOUBLE_QUOTE) && path.endsWith(DialogConstants.DOUBLE_QUOTE);
        String effectivePath = StringUtils.strip(path, DialogConstants.DOUBLE_QUOTE);
        PathSplitHelper pathSplitHelper = PathSplitHelper.of(effectivePath);

        if (!isEscaped && pathSplitHelper.isSplittable()) {
            Queue<String> pathChunks = pathSplitHelper.getChunks();
            Target current = this;
            while (!pathChunks.isEmpty()) {
                String currentChunk = pathChunks.poll();
                current = ((TargetImpl) current).getTargetInternal(currentChunk, createIfMissing);
                if (current == null) {
                    break;
                }
            }
            return current;
        }

        if (CoreConstants.PARENT_PATH.equals(effectivePath) && getParent() != null) {
            return getParent();
        }
        if (CoreConstants.PARENT_PATH.equals(effectivePath) || CoreConstants.SELF_PATH.equals(effectivePath)) {
            return this;
        }
        String nameToFind = NamingUtil.getValidNodeName(effectivePath);
        Target result = getChildren()
            .stream()
            .filter(child -> nameToFind.equals(child.getName()))
            .findFirst()
            .orElse(null);
        if (result == null && createIfMissing) {
            result = createTarget(path);
        }
        return result;
    }


    /* ------------------------------
       Relation management operations
       ------------------------------ */

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTarget(Target other, int position) {
        // We cannot attach Target B to Target A if Target A is a descendant of Target A
        if (other == null || other.findChild(child -> child.equals(this)) != null) {
            return;
        }
        if (other.getParent() != null) {
            other.getParent().getChildren().remove(other);
        }
        if (other instanceof TargetImpl) {
            ((TargetImpl) other).parent = this;
        }
        if (position > -1 && position < getChildren().size()) {
            getChildren().add(position, other);
        } else {
            getChildren().add(other);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeTarget(String path) {
        Target removable = getTarget(path);
        if (removable != null && removable.getParent() != null) {
            removable.getParent().getChildren().remove(removable);
        }
    }


    /* --------------------
       Filtering operations
       -------------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    public Target findParent(Predicate<Target> filter) {
        if (filter == null) {
            return null;
        }
        Target current = getParent();
        while (current != null) {
            if (filter.test(current)) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target findChild(Predicate<Target> filter) {
        if (filter == null) {
            return null;
        }
        return findChild(this, filter);
    }

    /**
     * Performs a recursive search for a matching descendant {@code Target} with the provided filter
     * @param current {@code Target object} whose children are currently tested
     * @param filter  {@code Predicate} to test the descendants of the current target
     * @return Relevant descendant {@code Target} object, or null
     */
    private static Target findChild(Target current, Predicate<Target> filter) {
        Target match = current.getChildren()
            .stream()
            .filter(filter)
            .findFirst()
            .orElse(null);
        if (match != null) {
            return match;
        }
        for (Target child : current.getChildren()) {
            match = findChild(child, filter);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Target> findChildren(Predicate<Target> filter) {
        if (filter == null) {
            return Collections.emptyList();
        }
        List<Target> result = new ArrayList<>();
        findChildren(this, filter, result);
        return result;
    }

    /**
     * Performs a recursive search for matching descendant {@code Target}s with the provided filter
     * @param current    {@code Target object} whose children are currently tested
     * @param filter     {@code Predicate} to test the descendants of the current target
     * @param collection {@code List} object accumulating the relevant targets
     */
    private static void findChildren(Target current, Predicate<Target> filter, List<Target> collection) {
        List<Target> matches = current.getChildren().stream().filter(filter).collect(Collectors.toList());
        collection.addAll(matches);
        for (Target child : current.getChildren()) {
            findChildren(child, filter, collection);
        }
    }


    /* ---------------------
       Attributes operations
       --------------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Target attribute(String name, String value) {
        if (value != null) {
            attributes.put(name, value);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target attribute(String name, String[] value) {
        if (value != null) {
            attributes.put(name, StringUtil.format(value, String.class));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target attribute(String name, boolean value) {
        attributes.put(name, StringUtil.format(value, Boolean.class));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target attribute(String name, boolean[] value) {
        if (value != null) {
            List<Boolean> booleanValues = IntStream.range(0, value.length)
                .mapToObj(pos -> value[pos])
                .collect(Collectors.toList());
            attributes.put(name, StringUtil.format(booleanValues, Boolean.class));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target attribute(String name, long value) {
        attributes.put(name, StringUtil.format(value, Long.class));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target attribute(String name, long[] value) {
        if (value != null) {
            attributes.put(
                name,
                StringUtil.format(LongStream.of(value).boxed().collect(Collectors.toList()), Long.class));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target attribute(String name, double value) {
        attributes.put(name, StringUtil.format(value, Double.class));
        return this;
    }

    @Override
    public Target attribute(String name, double[] value) {
        if (value != null) {
            attributes.put(
                name,
                StringUtil.format(DoubleStream.of(value).boxed().collect(Collectors.toList()), Double.class));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target attribute(String name, Date value) {
        if (value != null) {
            attributes.put(name, StringUtil.format(value, Date.class));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target attribute(String name, Date[] value) {
        if (value != null) {
            attributes.put(name, StringUtil.format(value, Date.class));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target attribute(String name, Object value) {
        if (value != null) {
            attributes.put(name, StringUtil.format(value, String.class));
        }
        return this;
    }

    @Override
    public Target attribute(String name, Object[] value) {
        if (value != null) {
            attributes.put(name, StringUtil.format(value, String.class));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target attributes(Annotation value, Predicate<Method> filter) {
        if (value == null) {
            return this;
        }
        populateAnnotationProperties(value, filter == null ? method -> true : filter);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target attributes(Element value) {
        if (value != null) {
            populateElementProperties(value);
        }
        return this;
    }

    /**
     * Called by {@link TargetImpl#attributes(Element)} to store element attributes to the current instance
     * @param value {@code Element} object used as the source of attribute names and values
     */
    private void populateElementProperties(Element value) {
        this.name = value.getTagName();
        IntStream.range(0, value.getAttributes().getLength())
            .mapToObj(pos -> value.getAttributes().item(pos))
            .forEach(nodeAttr -> attributes.put(nodeAttr.getNodeName(), nodeAttr.getNodeValue()));

        IntStream.range(0, value.getChildNodes().getLength())
            .mapToObj(pos -> value.getChildNodes().item(pos))
            .forEach(childNode -> {
                TargetImpl newChild = (TargetImpl) getOrCreateTarget(childNode.getNodeName());
                newChild.attributes((Element) childNode);
            });
    }

    /**
     * Called by {@link TargetImpl#attributes(Annotation, Predicate)} in order to store annotation properties to the
     * current instance
     * @param annotation {@code Annotation} object used as the source of attribute names and values
     * @param filter     {@code Predicate} used to sort out irrelevant properties
     */
    private void populateAnnotationProperties(Annotation annotation, Predicate<Method> filter) {
        String propertyPrefix = getPropertyPrefix(annotation);
        String nodePrefix = propertyPrefix.contains(CoreConstants.SEPARATOR_SLASH)
            ? StringUtils.substringBeforeLast(propertyPrefix, CoreConstants.SEPARATOR_SLASH)
            : StringUtils.EMPTY;

        Target effectiveTarget = StringUtils.isNotEmpty(nodePrefix)
            ?  getOrCreateTarget(nodePrefix)
            : this;
        Metadata.from(annotation)
            .stream()
            .filter(prop -> prop.matches(filter))
            .forEach(method -> populateAnnotationProperty(annotation, method, effectiveTarget));
    }

    /**
     * Called by {@link TargetImpl#populateAnnotationProperties(Annotation, Predicate)} to store the value of a
     * particular annotation property
     * @param annotation {@code Annotation} object used as the source of attribute names and values
     * @param property   {@code Method} reference representing the annotation property
     * @param target     Resulting {@code Target} object
     */
    private static void populateAnnotationProperty(Annotation annotation, Property property, Target target) {
        boolean ignorePrefix = false;

        // Extract property name
        String propertyName = property.getName();
        PropertyRendering propertyRendering = property.getAnnotation(PropertyRendering.class);
        if (propertyRendering != null) {
            propertyName = NamingUtil.getValidFieldName(StringUtils.defaultIfBlank(propertyRendering.name(), propertyName));
            ignorePrefix = propertyRendering.ignorePrefix();
        }

        // Extract the property prefix and prepend it to the name
        String prefixByPropertyMapping = getPropertyPrefix(annotation);
        String namePrefix = prefixByPropertyMapping.contains(CoreConstants.SEPARATOR_SLASH)
            ? StringUtils.substringAfterLast(prefixByPropertyMapping, CoreConstants.SEPARATOR_SLASH)
            : prefixByPropertyMapping;
        if (!ignorePrefix && StringUtils.isNotBlank(prefixByPropertyMapping)) {
            if (propertyName.contains(CoreConstants.SEPARATOR_SLASH)) {
                propertyName = StringUtils.substringBeforeLast(propertyName, CoreConstants.SEPARATOR_SLASH)
                    + CoreConstants.SEPARATOR_SLASH
                    + namePrefix
                    + StringUtils.substringAfterLast(propertyName, CoreConstants.SEPARATOR_SLASH);
            } else {
                propertyName = namePrefix + propertyName;
            }
        }

        // Adjust the target if the property name contains a relative path
        Target effectiveTarget = target;
        if (propertyName.contains(CoreConstants.SEPARATOR_SLASH)) {
            effectiveTarget = target.getOrCreateTarget(StringUtils.substringBeforeLast(propertyName, CoreConstants.SEPARATOR_SLASH));
            propertyName = StringUtils.substringAfterLast(propertyName, CoreConstants.SEPARATOR_SLASH);
        }

        BinaryOperator<String> merger = TargetImpl::mergeStringAttributes;
        AttributeHelper.forAnnotationProperty(annotation, property)
            .withName(propertyName)
            .withMerger(merger)
            .setTo(effectiveTarget);
    }

    /**
     * Retrieves a name prefix for the given {@code Annotation}
     * @param annotation {@code Annotation} object to look for a prefix value
     * @return String value
     */
    @SuppressWarnings("deprecation") // Processing of PropertyMapping is retained for compatibility and will be removed
    // in a version after 2.0.2
    private static String getPropertyPrefix(Annotation annotation) {
        Metadata metadata = Metadata.from(annotation);
        return Optional.ofNullable(metadata.getAnyAnnotation(AnnotationRendering.class, PropertyMapping.class))
            .map(a -> Metadata.from(a).getValue(DialogConstants.PN_PREFIX))
            .map(Object::toString)
            .orElse(StringUtils.EMPTY);
    }

    /**
     * Merges two string attributes that express plain values or inline value lists into the resulting string.
     * This method leaves no duplicate elements
     * @param first  First string value
     * @param second Second string value
     * @return A string containing the merged value
     */
    private static String mergeStringAttributes(String first, String second) {
        if (!StringUtil.isCollection(first) || !StringUtil.isCollection(second)) {
            return DEFAULT_ATTRIBUTE_MERGER.apply(first, second);
        }
        Set<String> result = StringUtil.parseSet(first);
        result.addAll(StringUtil.parseSet(second));
        return StringUtil.format(result, String.class);
    }
}
