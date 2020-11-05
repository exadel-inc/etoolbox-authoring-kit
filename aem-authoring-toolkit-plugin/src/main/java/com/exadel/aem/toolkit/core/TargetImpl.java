package com.exadel.aem.toolkit.core;

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyName;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.NamingUtil;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;
import com.exadel.aem.toolkit.core.util.XmlAttributeSettingHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TargetImpl implements Target {

    private static final String ATTRIBUTE_LIST_TEMPLATE = "[%s]";
    private static final String ATTRIBUTE_LIST_SPLIT_PATTERN = "\\s*,\\s*";
    private static final String ATTRIBUTE_LIST_SURROUND = "[]";
    private static final Pattern ATTRIBUTE_LIST_PATTERN = Pattern.compile("^\\[.+]$");
    /**
     * Default routine to manage merging two values of an XML attribute by suppressing existing value with a non-empty new one
     */
    private static final BinaryOperator<String> DEFAULT_ATTRIBUTE_MERGER = (first, second) -> StringUtils.isNotBlank(second) ? second : first;


    private final String name;
    private final Map<String, String> valueMap;
    private final Target parent;
    private final List<Target> listChildren;

    public TargetImpl(String name, Target parent) {
        this.name = name;
        this.parent = parent;
        this.valueMap = new HashMap<>();
        this.listChildren = new ArrayList<>();
        this.valueMap.put(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_UNSTRUCTURED);
    }

    @Override
    public Target child() {
        Target child = new TargetImpl(NamingUtil.getUniqueName(StringUtils.EMPTY, "item", this), this);
        this.listChildren.add(child);
        return child;
    }

    @Override
    public Target child(String s) {
        Target child = this.getChild(s);
        if (child == null) {
            child = new TargetImpl(s, this);
            this.listChildren.add(child);
        }
        return child;
    }

    @Override
    public Target mapProperties(Object o, List<String> skipped) {
        if (o instanceof Annotation) {
            mapAnnotationProperties((Annotation) o, this, skipped);
        }
        return this;
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
                    .reduce(target, this::getOrAddChildElement, (prev, next) -> next);
        }
        Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(m -> ArrayUtils.isEmpty(propMapping.mappings()) || ArrayUtils.contains(propMapping.mappings(), m.getName()))
                .filter(m -> !m.isAnnotationPresent(IgnorePropertyMapping.class))
                .filter(m -> !skipped.contains(m.getName()))
                .forEach(m -> populateProperty(m, effectiveElement, annotation));

    }

    private Target getOrAddChildElement(Target target, String name) {
        Target child = target.getChild(name);
        if (child == null) {
            child = new TargetImpl(name, target);
        }
        return child;
    }

    private void populateProperty(Method method, Target target, Annotation annotation) {
        String name = method.getName();
        boolean ignorePrefix = false;
        if (method.isAnnotationPresent(PropertyRendering.class)) {
            PropertyRendering propertyRendering = method.getAnnotation(PropertyRendering.class);
            name = StringUtils.defaultIfBlank(propertyRendering.name(), name);
            ignorePrefix = propertyRendering.ignorePrefix();
        } else if (method.isAnnotationPresent(PropertyName.class)) {
            PropertyName propertyName = method.getAnnotation(PropertyName.class);
            name = propertyName.value();
            ignorePrefix = propertyName.ignorePrefix();
        }
        String prefix = annotation.annotationType().getAnnotation(PropertyMapping.class).prefix();
        String namePrefix = prefix.contains(DialogConstants.PATH_SEPARATOR)
                ? StringUtils.substringAfterLast(prefix, DialogConstants.PATH_SEPARATOR)
                : prefix;
        if (!ignorePrefix && StringUtils.isNotBlank(prefix)) {
            name = namePrefix + name;
        }
        BinaryOperator<String> merger = this::mergeStringAttributes;
        XmlAttributeSettingHelper.forMethod(annotation, method)
                .withName(name)
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

    @Override
    public Target attribute(String name, String value) {
        this.valueMap.put(name, value);
        return this;
    }

    @Override
    public Target attribute(String name, boolean value) {
        this.valueMap.put(name, "{Boolean}" + value);
        return this;
    }

    @Override
    public Target attribute(String name, long value) {
        this.valueMap.put(name, "{Long}" + value);
        return this;
    }

    @Override
    public Target attribute(String name, List<String> values) {
        this.valueMap.put(name, values.toString());
        return this;
    }

    @Override
    public Target attributes(Map<String, String> map) {
        this.valueMap.putAll(map);
        return this;
    }

    @Override
    public boolean hasAttribute(String name) {
        return valueMap.containsKey(name);
    }

    @Override
    public Object deleteAttribute(String name) {
        return this.valueMap.remove(name);
    }

    @Override
    public List<Target> listChildren() {
        return listChildren;
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
    public String getAttribute(String name) {
        return valueMap.get(name);
    }

    @Override
    public Target getChild(String relPath) {
        Queue<String> pathChunks = Pattern.compile("/").splitAsStream(relPath).collect(Collectors.toCollection(LinkedList::new));
        Target currentFacade = this;
        while (!pathChunks.isEmpty()) {
            String currentChunk = pathChunks.poll();
            if (currentChunk.isEmpty() || currentChunk.equals(currentFacade.getName())) {
                return currentFacade;
            }
            currentFacade = currentFacade.listChildren().stream().filter(child -> currentChunk.equals(child.getName())).findFirst().orElse(null);
            if (currentFacade == null) {
                return null;
            }
        }
        return currentFacade;
    }

    @Override
    public Map<String, String> valueMap() {
        return valueMap;
    }

    @Override
    public Document buildXml(Document document) {
        return PluginXmlUtility.buildXml(this, document);
    }

    @Override
    public String buildJson() {
        return null;
    }
}
