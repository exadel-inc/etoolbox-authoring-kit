package com.exadel.aem.toolkit.api.handlers;

import org.w3c.dom.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public interface Target {

    Target appendChild(Target target);

    Target appendChild(Target target, String defaultName);

    default Target mapProperties(Object o) {
        return mapProperties(o, Collections.emptyList());
    }

    Target mapProperties(Object o, List<String> skipped);

    Target attribute(String name, String value);

    Target attribute(String name, boolean value);

    Target attribute(String name, long value);

    Target attribute(String name, List<String> values);

    Target setParent(Target parent);

    Target setAttributes(Map<String, String> map);

    boolean hasAttribute(String name);

    Object deleteAttribute(String name);

    List<Target> getListChildren();

    String getName();

    void name(String name);

    String getPath();

    Target parent();

    boolean hasChildren();

    String getAttribute(String name);

    Target getChild(String relPath);

    Target getOrAddChild(String name);

    Map<String, String> getValueMap();

    Document buildXml(Document document);

    String buildJson();
}
