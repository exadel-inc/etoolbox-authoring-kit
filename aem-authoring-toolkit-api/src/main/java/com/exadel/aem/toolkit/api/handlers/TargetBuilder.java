package com.exadel.aem.toolkit.api.handlers;

import org.w3c.dom.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public interface TargetBuilder {

    TargetBuilder appendChild(TargetBuilder target);

    TargetBuilder appendChild(TargetBuilder target, String defaultName);

    default TargetBuilder mapProperties(Object o) {
        return mapProperties(o, Collections.emptyList());
    }

    TargetBuilder mapProperties(Object o, List<String> skipped);

    TargetBuilder attribute(String name, String value);

    TargetBuilder attribute(String name, boolean value);

    TargetBuilder attribute(String name, long value);

    TargetBuilder attribute(String name, List<String> values);

    TargetBuilder setParent(TargetBuilder parent);

    TargetBuilder setAttributes(Map<String, String> map);

    boolean hasAttribute(String name);

    Object deleteAttribute(String name);

    List<TargetBuilder> getListChildren();

    String getName();

    void name(String name);

    String getPath();

    TargetBuilder parent();

    boolean hasChildren();

    String getAttribute(String name);

    TargetBuilder getChild(String relPath);

    TargetBuilder getOrAddChild(String name);

    Map<String, String> getValueMap();

    Document buildXml(Document document);

    String buildJson();
}
