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
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyName;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginNamingUtility;
import com.exadel.aem.toolkit.plugin.util.PluginXmlUtility;
import com.exadel.aem.toolkit.plugin.util.XmlAttributeSettingHelper;
import com.exadel.aem.toolkit.plugin.util.XmlDocumentFactory;

public class TargetImpl implements Target {

    private static final String ATTRIBUTE_LIST_TEMPLATE = "[%s]";
    private static final String ATTRIBUTE_LIST_SPLIT_PATTERN = "\\s*,\\s*";
    private static final String ATTRIBUTE_LIST_SURROUND = "[]";
    private static final Pattern ATTRIBUTE_LIST_PATTERN = Pattern.compile("^\\[.+]$");

    private static final String PARENT_PATH = "..";

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * Default routine to manage merging two values of an XML attribute by suppressing existing value with a non-empty new one
     */
    private static final BinaryOperator<String> DEFAULT_ATTRIBUTE_MERGER = (first, second) -> StringUtils.isNotBlank(second) ? second : first;

    private final Target parent;
    private final Map<String, String> attributes;
    private final List<Target> children;

    private String name;
    private XmlScope scope;

    public TargetImpl(String name, Target parent) {
        this.name = name;
        this.parent = parent;
        this.attributes = new HashMap<>();
        this.children = new LinkedList<>();
        this.scope = parent == null ? null : parent.getScope();
        this.attributes.put(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_UNSTRUCTURED);
    }

    @Override
    public Target create(String name) {
        return create(() -> PluginNamingUtility.getUniqueName(name, "item", this));
    }

    @Override
    public Target create(Supplier<String> s) {
        Target child = new TargetImpl(s.get(), this);
        this.children.add(child);
        return child;
    }

    @Override
    public Target getOrCreate(String s) {
        Target child = this.get(s);
        if (child == null) {
            child = new TargetImpl(s, this);
            this.children.add(child);
        }
        return child;
    }

    @Override
    public Target get(String s) {
        Queue<String> pathChunks = Pattern.compile("/").splitAsStream(s).collect(Collectors.toCollection(LinkedList::new));
        Target current = this;
        while (!pathChunks.isEmpty()) {
            String currentChunk = pathChunks.poll();
            if (PARENT_PATH.equals(currentChunk)) {
                current = current.parent();
            } else {
                current = current.getChildren().stream().filter(child -> currentChunk.equals(child.getName())).findFirst().orElse(null);
            }
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    @Override
    public Target mapProperties(Annotation annotation, List<String> skipped) {
        mapAnnotationProperties(annotation, this, skipped);
        return this;
    }

    @Override
    public Target mapProperties(Element element) {
        if (element == null) {
            return this;
        }
        this.name = element.getTagName();
        IntStream.range(0, element.getAttributes().getLength())
            .mapToObj(pos -> element.getAttributes().item(pos))
            .forEach(nodeAttr -> attributes.put(nodeAttr.getNodeName(), nodeAttr.getNodeValue()));

        IntStream.range(0, element.getChildNodes().getLength())
            .mapToObj(pos -> element.getChildNodes().item(pos))
            .forEach(childNode -> get(childNode.getNodeName()).mapProperties((Element) childNode));
        return this;
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
        if (value != null) this.attributes.put(name, "{Date}" + new SimpleDateFormat(DATE_FORMAT).format(value));
        return this;
    }

    @Override
    public Target attributes(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (Long.class.equals(entry.getValue().getClass())) {
                this.attribute(entry.getKey(), (Long) entry.getValue());
            }
            if (Boolean.class.equals(entry.getValue().getClass())) {
                this.attribute(entry.getKey(), (Boolean) entry.getValue());
            }
            if (Double.class.equals(entry.getValue().getClass())) {
                this.attribute(entry.getKey(), (Double) entry.getValue());
            }
            if (Date.class.equals(entry.getValue().getClass())) {
                this.attribute(entry.getKey(), (Date) entry.getValue());
            }
            this.attribute(entry.getKey(), entry.getValue().toString());
        }
        return this;
    }

    @Override
    public Target prefix(String prefix) {
        this.attributes.put(DialogConstants.PN_PREFIX, prefix);
        return this;
    }

    @Override
    public Target postfix(String postfix) {
        this.attributes.put(DialogConstants.PN_POSTFIX, postfix);
        return this;
    }

    @Override
    public Target scope(XmlScope scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public XmlScope getScope() {
        return scope;
    }

    @Override
    public String getPrefix() {
        if (this.parent == null) {
            return StringUtils.EMPTY;
        }
        String prefix = this.attributes.getOrDefault(DialogConstants.PN_PREFIX, StringUtils.EMPTY) ;
        return this.parent.getPrefix() + prefix;
    }

    @Override
    public String getPostfix() {
        if (this.parent == null) {
            return StringUtils.EMPTY;
        }
        String postfix = this.attributes.getOrDefault(DialogConstants.PN_POSTFIX, StringUtils.EMPTY);
        return postfix + this.parent.getPostfix();
    }

    @Override
    public void delete() {
        this.parent.getChildren().remove(this);
    }

    @Override
    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    @Override
    public List<Target> getChildren() {
        return this.children;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Target parent() {
        return parent;
    }

    @Override
    public boolean hasChild(String path) {
        return get(path) != null;
    }

    @Override
    public Document buildXml() throws ParserConfigurationException {
        Document document = XmlDocumentFactory.newDocument();
        return PluginXmlUtility.buildXml(this, document);
    }

    private void populateProperty(Method method, Target target, Annotation annotation) {
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
        String prefix = annotation.annotationType().getAnnotation(PropertyMapping.class).prefix();
        String namePrefix = prefix.contains(DialogConstants.PATH_SEPARATOR)
            ? StringUtils.substringAfterLast(prefix, DialogConstants.PATH_SEPARATOR)
            : prefix;
        if (!ignorePrefix && StringUtils.isNotBlank(prefix)) {
            methodName = namePrefix + methodName;
        }
        BinaryOperator<String> merger = this::mergeStringAttributes;
        XmlAttributeSettingHelper.forMethod(annotation, method)
            .withName(methodName)
            .withMerger(merger)
            .setAttribute(target);
    }

    private String mergeStringAttributes(String first, String second) {
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

    private void mapAnnotationProperties(Annotation annotation, Target target, List<String> skipped) {
        PropertyMapping propMapping = annotation.annotationType().getDeclaredAnnotation(PropertyMapping.class);
        if (propMapping == null) {
            return;
        }
        String prefix = annotation.annotationType().getAnnotation(PropertyMapping.class).prefix();
        String nodePrefix = prefix.contains(DialogConstants.PATH_SEPARATOR)
            ? StringUtils.substringBeforeLast(prefix, DialogConstants.PATH_SEPARATOR)
            : StringUtils.EMPTY;

        Target effectiveElement;
        if (StringUtils.isEmpty(nodePrefix)) {
            effectiveElement = target;
        } else {
            effectiveElement = Pattern.compile(DialogConstants.PATH_SEPARATOR)
                .splitAsStream(nodePrefix)
                .reduce(target, Target::getOrCreate, (prev, next) -> next);
        }
        Arrays.stream(annotation.annotationType().getDeclaredMethods())
            .filter(m -> ArrayUtils.isEmpty(propMapping.mappings()) || ArrayUtils.contains(propMapping.mappings(), m.getName()))
            .filter(m -> !m.isAnnotationPresent(IgnorePropertyMapping.class))
            .filter(m -> !skipped.contains(m.getName()))
            .forEach(m -> populateProperty(m, effectiveElement, annotation));

    }
}
