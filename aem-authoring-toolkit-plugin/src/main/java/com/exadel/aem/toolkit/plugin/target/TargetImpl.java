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

package com.exadel.aem.toolkit.plugin.target;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyName;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.AdaptationBase;
import com.exadel.aem.toolkit.plugin.util.AttributeSettingHelper;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginNamingUtility;

public class TargetImpl extends AdaptationBase<Target> implements Target {

    private static final String ATTRIBUTE_LIST_TEMPLATE = "[%s]";
    private static final String ATTRIBUTE_LIST_SPLIT_PATTERN = "\\s*,\\s*";
    private static final String ATTRIBUTE_LIST_SURROUND = "[]";
    private static final String ATTRIBUTE_TYPE_HINT_TEMPLATE = "{%s}";
    private static final Pattern ATTRIBUTE_LIST_PATTERN = Pattern.compile("^\\[.+]$");

    private static final String PARENT_PATH = "..";
    private static final String SELF_PATH = ".";

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private static final BinaryOperator<String> DEFAULT_ATTRIBUTE_MERGER = (first, second) -> StringUtils.isNotBlank(second) ? second : first;

    private final Target parent;
    private final Map<String, String> attributes;
    private final List<Target> children;

    private String name;
    private String prefix;
    private String postfix;
    private XmlScope scope;


    /* ------------
       Constructors
       ------------ */

    TargetImpl(String name, Target parent) {
        super(Target.class);
        this.name = name;
        this.parent = parent;
        this.attributes = new HashMap<>();
        this.children = new LinkedList<>();
        this.scope = parent != null ? parent.getScope() : XmlScope.CQ_DIALOG;
        this.attributes.put(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_UNSTRUCTURED);
    }


    /* ---------------
       Basic accessors
       --------------- */

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Target getParent() {
        return parent;
    }

    @Override
    public List<Target> getChildren() {
        return this.children;
    }


    /* ----------------
       Scope operations
       ---------------- */

    @Override
    public XmlScope getScope() {
        return scope;
    }

    void setScope(XmlScope scope) {
        this.scope = scope;
    }


    /* ---------------
       Path operations
       --------------- */

    @Override
    public Target getTarget(String path) {
        return getTarget(path, false);
    }

    @Override
    public Target getOrCreateTarget(String path) {
        return getTarget(path, true);
    }

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
        if (PARENT_PATH.equals(effectivePath)) {
            return getParent() != null ? getParent() : null;
        }
        if (SELF_PATH.equals(effectivePath)) {
            return this;
        }
        String effectiveName = PluginNamingUtility.getUniqueName(effectivePath, DialogConstants.NN_ITEM, this);
        Target child = new TargetImpl(effectiveName, this);
        this.children.add(child);
        return child;
    }

    @Override
    public void removeTarget(String path) {
        Target removable = getTarget(path);
        if (removable != null && removable.getParent() != null) {
            removable.getParent().getChildren().remove(removable);
        }
    }

    private Target getTarget(String path, boolean createIfMissing) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        boolean isEscaped = path.startsWith(DialogConstants.DOUBLE_QUOTE) && path.endsWith(DialogConstants.DOUBLE_QUOTE);
        String effectivePath = isEscaped ? StringUtils.strip(path, DialogConstants.DOUBLE_QUOTE) : path;
        PathSplitHelper pathSplitHelper = PathSplitHelper.of(effectivePath);

        if (!isEscaped && pathSplitHelper.isSplittable()) {
            Queue<String> pathChunks = pathSplitHelper.getChunks();
            Target current = this;
            while (!pathChunks.isEmpty()) {
                String currentChunk = pathChunks.poll();
                current = ((TargetImpl) current).getTarget(currentChunk, createIfMissing);
                if (current == null) {
                    return null;
                }
            }
            return current;
        }

        if (PARENT_PATH.equals(effectivePath) && getParent() != null) {
            return getParent();
        }
        if (PARENT_PATH.equals(effectivePath) || SELF_PATH.equals(effectivePath)) {
            return this;
        }
        String nameToFind = PluginNamingUtility.getValidNodeName(effectivePath);
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


    /* --------------------
       Filtering operations
       -------------------- */

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

    @Override
    public Target findChild(Predicate<Target> filter) {
        if (filter == null) {
            return null;
        }
        return findChild(this, filter);
    }

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
            if ((match = findChild(child, filter)) != null) {
                return match;
            }
        }
        return null;
    }

    @Override
    public List<Target> findChildren(Predicate<Target> filter) {
        if (filter == null) {
            return Collections.emptyList();
        }
        List<Target> result = new ArrayList<>();
        findChildren(this, filter, result);
        return result;
    }

    private static void findChildren(Target current, Predicate<Target> filter, List<Target> collection) {
        List<Target> matches = current.getChildren().stream().filter(filter).collect(Collectors.toList());
        collection.addAll(matches);
        for (Target child : current.getChildren()) {
            findChildren(child, filter, collection);
        }
    }


    /* -----------------
       Prefix operations
       ----------------- */

    @Override
    public String getNamePrefix() {
        if (getParent() == null) {
            return StringUtils.defaultString(this.prefix);
        }
        return getParent().getNamePrefix() + StringUtils.defaultString(this.prefix);
    }

    @Override
    public Target namePrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }


    /* ------------------
       Postfix operations
       ------------------ */

    @Override
    public String getNamePostfix() {
        if (getParent() == null) {
            return StringUtils.defaultString(this.postfix);
        }
        return StringUtils.defaultString(this.postfix) + getParent().getNamePostfix();
    }

    @Override
    public Target namePostfix(String postfix) {
        this.postfix = postfix;
        return this;
    }


    /* ---------------------
       Attributes operations
       --------------------- */

    @Override
    public Map<String, String> getAttributes() {
        return this.attributes;
    }


    @Override
    public Target attribute(String name, String value) {
        if (value != null) this.attributes.put(name, value);
        return this;
    }

    @Override
    public Target attribute(String name, boolean value) {
        this.attributes.put(name, String.format(ATTRIBUTE_TYPE_HINT_TEMPLATE, Boolean.class.getSimpleName()) + value);
        return this;
    }

    @Override
    public Target attribute(String name, String[] value) {
        if (value != null) {
            String stringValues = String.join(DialogConstants.ITEM_SEPARATOR_COMMA, value);
            this.attributes.put(
                name,
                String.format(ATTRIBUTE_TYPE_HINT_TEMPLATE, Long.class.getSimpleName())
                    + String.format(ATTRIBUTE_LIST_TEMPLATE, stringValues));
        }
        return this;
    }

    @Override
    public Target attribute(String name, boolean[] value) {
        if (value != null) {
            String booleanValues = IntStream.range(0, value.length)
                .mapToObj(index -> value[index])
                .map(String::valueOf)
                .collect(Collectors.joining(DialogConstants.ITEM_SEPARATOR_COMMA));
            this.attributes.put(
                name,
                String.format(ATTRIBUTE_TYPE_HINT_TEMPLATE, Boolean.class.getSimpleName())
                    + String.format(ATTRIBUTE_LIST_TEMPLATE, booleanValues));
        }
        return this;
    }

    @Override
    public Target attribute(String name, long value) {
        this.attributes.put(name, String.format(ATTRIBUTE_TYPE_HINT_TEMPLATE, Long.class.getSimpleName()) + value);
        return this;
    }

    @Override
    public Target attribute(String name, long[] value) {
        if (value != null) {
            String longValues = Arrays.stream(value)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(DialogConstants.ITEM_SEPARATOR_COMMA));
            this.attributes.put(
                name,
                String.format(ATTRIBUTE_TYPE_HINT_TEMPLATE, Long.class.getSimpleName())
                    + String.format(ATTRIBUTE_LIST_TEMPLATE, longValues));
        }
        return this;
    }

    @Override
    public Target attribute(String name, double value) {
        this.attributes.put(name, String.format(ATTRIBUTE_TYPE_HINT_TEMPLATE, Double.class.getSimpleName()) + value);
        return this;
    }

    @Override
    public Target attribute(String name, double[] value) {
        if (value != null) {
            String doubleValues = Arrays.stream(value)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(DialogConstants.ITEM_SEPARATOR_COMMA));
            this.attributes.put(
                name,
                String.format(ATTRIBUTE_TYPE_HINT_TEMPLATE, Double.class.getSimpleName())
                    + String.format(ATTRIBUTE_LIST_TEMPLATE, doubleValues));
        }
        return this;
    }

    @Override
    public Target attribute(String name, Date value) {
        if (value != null) {
            this.attributes.put(
                name,
                String.format(ATTRIBUTE_TYPE_HINT_TEMPLATE, Date.class.getSimpleName())
                    + new SimpleDateFormat(DATE_FORMAT).format(value));
        }
        return this;
    }

    @Override
    public Target attribute(String name, Date[] value) {
        if (value == null) {
            return this;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dateValues = Arrays.stream(value)
            .filter(Objects::nonNull)
            .map(date -> dateFormat.format(value))
            .collect(Collectors.joining(DialogConstants.ITEM_SEPARATOR_COMMA));
        this.attributes.put(
            name,
            String.format(ATTRIBUTE_TYPE_HINT_TEMPLATE, Date.class.getSimpleName())
                + String.format(ATTRIBUTE_LIST_TEMPLATE, dateValues));
        return this;
    }

    @Override
    public Target attributes(Map<String, Object> value) {
        if (value == null) {
            return this;
        }
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            if (Long.class.equals(entry.getValue().getClass())) {
                this.attribute(entry.getKey(), (long) entry.getValue());
            } else if (Boolean.class.equals(entry.getValue().getClass())) {
                this.attribute(entry.getKey(), (boolean) entry.getValue());
            } else if (Double.class.equals(entry.getValue().getClass())) {
                this.attribute(entry.getKey(), (double) entry.getValue());
            } else if (Date.class.equals(entry.getValue().getClass())) {
                this.attribute(entry.getKey(), (Date) entry.getValue());
            } else {
                this.attribute(entry.getKey(), entry.getValue().toString());
            }
        }
        return this;
    }

    public Target attributes(Element value) {
        if (value != null) {
            populateElementProperties(value);
        }
        return this;
    }

    @Override
    public Target attributes(Annotation value, Predicate<Method> filter) {
        if (value == null) {
            return this;
        }
        populateAnnotationProperties(value, filter == null ? method -> true : filter);
        return this;
    }

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

    private void populateAnnotationProperties(Annotation annotation, Predicate<Method> filter) {
        String completePropertyPrefix = Optional.ofNullable(annotation.annotationType().getAnnotation(PropertyMapping.class))
            .map(PropertyMapping::prefix)
            .orElse(StringUtils.EMPTY);
        String nodePrefix = completePropertyPrefix.contains(DialogConstants.PATH_SEPARATOR)
            ? StringUtils.substringBeforeLast(completePropertyPrefix, DialogConstants.PATH_SEPARATOR)
            : StringUtils.EMPTY;

        Target effectiveTarget = this;
        if (StringUtils.isNotEmpty(nodePrefix)) {
            effectiveTarget = effectiveTarget.getOrCreateTarget(nodePrefix);
        }
        List<Method> propertySources = Arrays.stream(annotation.annotationType().getDeclaredMethods())
            .filter(filter)
            .collect(Collectors.toList());
        for (Method propertySource: propertySources) {
            populateAnnotationProperty(annotation, propertySource, effectiveTarget);
        }
    }

    private static void populateAnnotationProperty(Annotation context, Method method, Target target) {
        boolean ignorePrefix = false;

        // Extract property name
        String propertyName = method.getName();
        if (method.isAnnotationPresent(PropertyRendering.class)) {
            PropertyRendering propertyRenderingAnnotation = method.getAnnotation(PropertyRendering.class);
            propertyName = PluginNamingUtility.getValidFieldName(StringUtils.defaultIfBlank(propertyRenderingAnnotation.name(), propertyName));
            ignorePrefix = propertyRenderingAnnotation.ignorePrefix();
        } else if (method.isAnnotationPresent(PropertyName.class)) {
            PropertyName propertyNameAnnotation = method.getAnnotation(PropertyName.class);
            propertyName = PluginNamingUtility.getValidFieldName(propertyNameAnnotation.value());
            ignorePrefix = propertyNameAnnotation.ignorePrefix();
        }

        // Extract property prefix and prepend it to the name
        String prefixByPropertyMapping = Optional.ofNullable(context.annotationType().getAnnotation(PropertyMapping.class))
            .map(PropertyMapping::prefix)
            .orElse(StringUtils.EMPTY);
        String namePrefix = prefixByPropertyMapping.contains(DialogConstants.PATH_SEPARATOR)
            ? StringUtils.substringAfterLast(prefixByPropertyMapping, DialogConstants.PATH_SEPARATOR)
            : prefixByPropertyMapping;
        if (!ignorePrefix && StringUtils.isNotBlank(prefixByPropertyMapping)) {
            if (propertyName.contains(DialogConstants.PATH_SEPARATOR)) {
                propertyName = StringUtils.substringBeforeLast(propertyName, DialogConstants.PATH_SEPARATOR)
                    + DialogConstants.PATH_SEPARATOR
                    + namePrefix
                    + StringUtils.substringAfterLast(propertyName, DialogConstants.PATH_SEPARATOR);
            } else {
                propertyName = namePrefix + propertyName;
            }
        }

        // Adjust target if the property name contains a relative path
        Target effectiveTarget = target;
        if (propertyName.contains(DialogConstants.PATH_SEPARATOR)) {
            effectiveTarget = target.getOrCreateTarget(StringUtils.substringBeforeLast(propertyName, DialogConstants.PATH_SEPARATOR));
            propertyName = StringUtils.substringAfterLast(propertyName, DialogConstants.PATH_SEPARATOR);
        }

        BinaryOperator<String> merger = TargetImpl::mergeStringAttributes;
        AttributeSettingHelper.forMethod(context, method)
            .withName(propertyName)
            .withMerger(merger)
            .setAttribute(effectiveTarget);
    }

    private static String mergeStringAttributes(String first, String second) {
        if (!ATTRIBUTE_LIST_PATTERN.matcher(first).matches() || !ATTRIBUTE_LIST_PATTERN.matcher(second).matches()) {
            return DEFAULT_ATTRIBUTE_MERGER.apply(first, second);
        }
        Set<String> result = new HashSet<>(Arrays.asList(StringUtils
            .strip(first, ATTRIBUTE_LIST_SURROUND)
            .split(ATTRIBUTE_LIST_SPLIT_PATTERN)));
        result.addAll(new HashSet<>(Arrays.asList(StringUtils
            .strip(second, ATTRIBUTE_LIST_SURROUND)
            .split(ATTRIBUTE_LIST_SPLIT_PATTERN))));
        return String.format(ATTRIBUTE_LIST_TEMPLATE, String.join(DialogConstants.ITEM_SEPARATOR_COMMA, result));
    }
}
