package com.exadel.aem.toolkit.api.handlers;

import org.w3c.dom.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public interface Target {

    Target child();

    Target child(String s);

    default Target mapProperties(Object o) {
        return mapProperties(o, Collections.emptyList());
    }

    Target mapProperties(Object o, List<String> skipped);

    Target attribute(String name, String value);

    Target attribute(String name, Boolean value);

    Target attribute(String name, Long value);

    Target attribute(String name, List<String> values);

    Target attributes(Map<String, String> map);

    boolean hasAttribute(String name);

    Object deleteAttribute(String name);

    List<Target> listChildren();

    String getName();

    Target name(String name, String defaultName);

    Target parent();

    String getAttribute(String name);

    Target getChild(String relPath);

    Target clear();

    boolean isDefault();

    Map<String, String> valueMap();

    Document buildXml(Document document);

    String buildJson();
}
