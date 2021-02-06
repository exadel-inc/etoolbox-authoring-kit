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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyName;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.AdaptationBase;
import com.exadel.aem.toolkit.plugin.util.AttributeSettingHelper;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginNamingUtility;

public class TargetImpl extends AdaptationBase<Target> implements Target {

    private static final String ATTRIBUTE_LIST_TEMPLATE = "[%s]";
    private static final String ATTRIBUTE_LIST_SPLIT_PATTERN = "\\s*,\\s*";
    private static final String ATTRIBUTE_LIST_SURROUND = "[]";
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
        if (StringUtils.contains(path, DialogConstants.PATH_SEPARATOR)) {
            Target existingTarget = getTarget(path);
            if (existingTarget != null && !this.equals(existingTarget)) {
                removeTarget(path);
            } else if (this.equals(existingTarget)) {
                return this;
            }
            return getOrCreateTarget(path);
        }
        if (PARENT_PATH.equals(path)) {
            return getParent() != null ? getParent() : null;
        }
        if (SELF_PATH.equals(path)) {
            return this;
        }
        String effectiveName = PluginNamingUtility.getUniqueName(path, DialogConstants.NN_ITEM, this);
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
        if (path.contains(DialogConstants.PATH_SEPARATOR)) {
            Queue<String> pathChunks = Pattern.compile("/").splitAsStream(path).collect(Collectors.toCollection(LinkedList::new));
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

        if (PARENT_PATH.equals(path) && getParent() != null) {
            return getParent();
        }
        if (PARENT_PATH.equals(path) || SELF_PATH.equals(path)) {
            return this;
        }
        Target result = getChildren().stream().filter(child -> path.equals(child.getName())).findFirst().orElse(null);
        if (result == null && createIfMissing) {
            result = createTarget(path);
        }
        return result;
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
        this.attributes.put(name, "{Boolean}" + value);
        return this;
    }

    @Override
    public Target attribute(String name, long value) {
        this.attributes.put(name, "{Long}" + value);
        return this;
    }

    @Override
    public Target attribute(String name, double value) {
        this.attributes.put(name, "{Double}" + value);
        return this;
    }

    @Override
    public Target attribute(String name, Date value) {
        if (value != null) {
            this.attributes.put(name, "{Date}" + new SimpleDateFormat(DATE_FORMAT).format(value));
        }
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
    public Target attributes(Annotation value, Predicate<Member> filter) {
        populateAnnotationProperties(value, filter);
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

    private void populateAnnotationProperties(Annotation annotation, Predicate<Member> filter) {
        PropertyMapping propMapping = annotation.annotationType().getDeclaredAnnotation(PropertyMapping.class);
        if (propMapping == null) {
            return;
        }
        String prefix = annotation.annotationType().getAnnotation(PropertyMapping.class).prefix();
        String nodePrefix = prefix.contains(DialogConstants.PATH_SEPARATOR)
            ? StringUtils.substringBeforeLast(prefix, DialogConstants.PATH_SEPARATOR)
            : StringUtils.EMPTY;

        Target effectiveTarget = this;
        if (StringUtils.isNotEmpty(nodePrefix)) {
            effectiveTarget = Pattern.compile(DialogConstants.PATH_SEPARATOR)
                .splitAsStream(nodePrefix)
                .reduce(effectiveTarget, Target::getOrCreateTarget, (prev, next) -> next);
        }
        List<Method> propertySources = Arrays.stream(annotation.annotationType().getDeclaredMethods())
            .filter(method -> ArrayUtils.isEmpty(propMapping.mappings()) || ArrayUtils.contains(propMapping.mappings(), method.getName()))
            .filter(m -> !m.isAnnotationPresent(IgnorePropertyMapping.class))
            .filter(filter)
            .collect(Collectors.toList());
        for (Method propertySource: propertySources) {
            populateAnnotationProperty(effectiveTarget, propertySource, annotation);
        }
    }

    private static void populateAnnotationProperty(Target target, Method method, Annotation context) {
        String methodName = method.getName();
        boolean ignorePrefix = false;
        if (method.isAnnotationPresent(PropertyRendering.class)) {
            PropertyRendering propertyRendering = method.getAnnotation(PropertyRendering.class);
            methodName = StringUtils.defaultIfBlank(propertyRendering.name(), methodName);
            ignorePrefix = propertyRendering.ignorePrefix();
        } else if (method.isAnnotationPresent(PropertyName.class)) {
            PropertyName propertyName = method.getAnnotation(PropertyName.class);
            methodName = propertyName.value();
            ignorePrefix = propertyName.ignorePrefix();
        }
        String prefix = context.annotationType().getAnnotation(PropertyMapping.class).prefix();
        String namePrefix = prefix.contains(DialogConstants.PATH_SEPARATOR)
            ? StringUtils.substringAfterLast(prefix, DialogConstants.PATH_SEPARATOR)
            : prefix;
        if (!ignorePrefix && StringUtils.isNotBlank(prefix)) {
            methodName = namePrefix + methodName;
        }
        BinaryOperator<String> merger = TargetImpl::mergeStringAttributes;
        AttributeSettingHelper.forMethod(context, method)
            .withName(methodName)
            .withMerger(merger)
            .setAttribute(target);
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
        return String.format(ATTRIBUTE_LIST_TEMPLATE, String.join(RteFeatures.FEATURE_SEPARATOR, result));
    }
}
