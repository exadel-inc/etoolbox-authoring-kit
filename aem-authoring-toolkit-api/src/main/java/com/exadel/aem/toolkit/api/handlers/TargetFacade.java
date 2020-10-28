package com.exadel.aem.toolkit.api.handlers;

import org.w3c.dom.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public interface TargetFacade {

    TargetFacade appendChild(TargetFacade targetFacade);

    TargetFacade appendChild(TargetFacade targetFacade, String defaultName);

    default TargetFacade mapProperties(Object o) {
        return mapProperties(o, Collections.emptyList());
    }

    TargetFacade mapProperties(Object o, List<String> skipped);

    TargetFacade setAttribute(String name, String value);

    TargetFacade setAttribute(String name, boolean value);

    TargetFacade setAttribute(String name, long value);

    TargetFacade setAttribute(String name, List<String> values);

    TargetFacade setParent(TargetFacade parent);

    TargetFacade setAttributes(Map<String, String> map);

    boolean hasAttribute(String name);

    Object deleteAttribute(String name);

    List<TargetFacade> getListChildren();

    String getName();

    void setName(String name);

    String getPath();

    TargetFacade getParent();

    boolean hasChildren();

    String getAttribute(String name);

    TargetFacade getChild(String relPath);

    TargetFacade getOrAddChild(String name);

    Map<String, String> getValueMap();

    Document buildXml(Document document);

    String buildJson();
}
