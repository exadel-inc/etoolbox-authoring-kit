package com.exadel.aem.toolkit.api.handlers;

public interface SourceFacade {

    Object fromValueMap(String s);

    Object addToValueMap(String s1, String s2);

    <T> T adaptTo(Class<T> t);

    Object getSource();
}
