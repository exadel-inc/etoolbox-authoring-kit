package com.exadel.aem.toolkit.core;

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyName;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.handlers.TargetFacade;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.NamingUtil;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;
import com.exadel.aem.toolkit.core.util.XmlAttributeSettingHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TargetFacadeFacadeImpl implements TargetFacade {

    private static final String ATTRIBUTE_LIST_TEMPLATE = "[%s]";
    private static final String ATTRIBUTE_LIST_SPLIT_PATTERN = "\\s*,\\s*";
    private static final String ATTRIBUTE_LIST_SURROUND = "[]";
    private static final Pattern ATTRIBUTE_LIST_PATTERN = Pattern.compile("^\\[.+]$");
    /**
     * Default routine to manage merging two values of an XML attribute by suppressing existing value with a non-empty new one
     */
    private static final BinaryOperator<String> DEFAULT_ATTRIBUTE_MERGER = (first, second) -> StringUtils.isNotBlank(second) ? second : first;


    private String name;
    private final Map<String, String> valueMap;
    private TargetFacade parent;
    private final List<TargetFacade> listChildren;

    public TargetFacadeFacadeImpl(String name) {
        this.name = name;
        this.valueMap = new HashMap<>();
        this.listChildren = new ArrayList<>();
        this.valueMap.put(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_UNSTRUCTURED);
    }

    @Override
    public TargetFacade appendChild(TargetFacade targetFacade) {
        this.listChildren.add(targetFacade);
        targetFacade.setParent(this);
        return targetFacade;
    }

    @Override
    public TargetFacade appendChild(TargetFacade targetFacade, String defaultName) {
        targetFacade.setName(getUniqueName(targetFacade.getName(), defaultName, this));
        return appendChild(targetFacade);
    }

    private String getUniqueName(String name, String defaultName, TargetFacade targetFacade) {
        return NamingUtil.getUniqueName(name, defaultName, targetFacade);
    }

    @Override
    public TargetFacade mapProperties(Object o, List<String> skipped) {
        if (o instanceof Annotation) {
            mapAnnotationProperties((Annotation) o, this, skipped);
        }
        return this;
    }

    private void mapAnnotationProperties(Annotation annotation, TargetFacade targetFacade, List<String> skipped) {
        PropertyMapping propMapping = annotation.annotationType().getDeclaredAnnotation(PropertyMapping.class);
        if (propMapping == null) {
            return;
        }
        String prefix = annotation.annotationType().getAnnotation(PropertyMapping.class).prefix();
        String nodePrefix = prefix.contains(DialogConstants.PATH_SEPARATOR)
                ? StringUtils.substringBeforeLast(prefix, DialogConstants.PATH_SEPARATOR)
                : StringUtils.EMPTY;

        TargetFacade effectiveElement;
        if (StringUtils.isEmpty(nodePrefix)) {
            effectiveElement = targetFacade;
        } else {
            effectiveElement = Pattern.compile(DialogConstants.PATH_SEPARATOR)
                    .splitAsStream(nodePrefix)
                    .reduce(targetFacade, this::getOrAddChildElement, (prev, next) -> next);
        }
        Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(m -> ArrayUtils.isEmpty(propMapping.mappings()) || ArrayUtils.contains(propMapping.mappings(), m.getName()))
                .filter(m -> !m.isAnnotationPresent(IgnorePropertyMapping.class))
                .filter(m -> !skipped.contains(m.getName()))
                .forEach(m -> populateProperty(m, effectiveElement, annotation));

    }

    private TargetFacade getOrAddChildElement(TargetFacade targetFacade, String name) {
        TargetFacade child = targetFacade.getChild(name);
        if (child == null) {
            return targetFacade.appendChild(new TargetFacadeFacadeImpl(name));
        } else {
            return child;
        }
    }

    private void populateProperty(Method method, TargetFacade targetFacade, Annotation annotation) {
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
                .setAttribute(targetFacade);
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
    public TargetFacade setAttribute(String name, String value) {
        this.valueMap.put(name, value);
        return this;
    }

    @Override
    public TargetFacade setAttribute(String name, boolean value) {
        this.valueMap.put(name, "{Boolean}" + value);
        return this;
    }

    @Override
    public TargetFacade setAttribute(String name, long value) {
        this.valueMap.put(name, "{Long}" + value);
        return this;
    }

    @Override
    public TargetFacade setAttribute(String name, List<String> values) {
        this.valueMap.put(name, values.toString());
        return this;
    }

    @Override
    public TargetFacade setParent(TargetFacade parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public TargetFacade setAttributes(Map<String, String> map) {
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
    public List<TargetFacade> getListChildren() {
        return listChildren;
    }

    @Override
    public String getPath() {
        if (parent == null) {
            return name;
        }
        return parent.getName() + "/" + name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public TargetFacade getParent() {
        return parent;
    }

    @Override
    public boolean hasChildren() {
        return !listChildren.isEmpty();
    }

    @Override
    public String getAttribute(String name) {
        return valueMap.get(name);
    }

    @Override
    public TargetFacade getChild(String relPath) {
        Queue<String> pathChunks = Pattern.compile("/").splitAsStream(relPath).collect(Collectors.toCollection(LinkedList::new));
        TargetFacade currentFacade = this;
        while (!pathChunks.isEmpty()) {
            String currentChunk = pathChunks.poll();
            if (currentChunk.isEmpty() || currentChunk.equals(currentFacade.getName())) {
                return currentFacade;
            }
            currentFacade = currentFacade.getListChildren().stream().filter(child -> currentChunk.equals(child.getName())).findFirst().orElse(null);
            if (currentFacade == null) {
                return null;
            }
        }
        return currentFacade;
    }

    @Override
    public TargetFacade getOrAddChild(String name) {
        TargetFacade child = this.getChild(name);
        if (child == null) {
            return this.appendChild(new TargetFacadeFacadeImpl(name));
        }
        return child;
    }

    @Override
    public Map<String, String> getValueMap() {
        return valueMap;
    }

    @Override
    public Document buildXml(Document document) {
        try {
            return PluginXmlUtility.buildXml(this, document);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String buildJson() {
        return null;
    }
}
