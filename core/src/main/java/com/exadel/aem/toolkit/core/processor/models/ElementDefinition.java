package com.exadel.aem.toolkit.core.processor.models;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javax.lang.model.element.Element;

public class ElementDefinition {

    public static final Function<Element, ElementDefinition> ELEMENT_TO_ELEMENT_DEFINITION =
        (element -> new ElementDefinitionBuilder(element).build());
    public static final Function<Element, ElementDefinition> ELEMENT_TO_ELEMENT_DEFINITION_WITH_CHILDREN =
        (element -> new ElementDefinitionBuilder(element).enablePopulatingChildren().build());


    private String name;
    private String kind;
    private List<AnnotationDefinition> annotations;
    private List<ElementDefinition> children;

    public ElementDefinition() {
        this.annotations = Collections.emptyList();
        this.children = Collections.emptyList();
    }

    void setName(String name) {
        this.name = name;
    }

    void setKind(String kind) {
        this.kind = kind;
    }

    void setAnnotations(List<AnnotationDefinition> annotations) {
        this.annotations = annotations;
    }

    void setChildren(List<ElementDefinition> children) {
        this.children = children;
    }
}
