package com.exadel.aem.toolkit.core.processor.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.processor.EToolboxProcessor;

public class ElementDefinitionBuilder {

    private static final List<String> UNSUITABLE_CLASS_NAME_PREFIXES = Arrays.asList(
        "java",
        "org.apache",
        "org.osgi"
    );

    private static final Function<Element, List<ElementDefinition>> EMPTY_CHILDREN
        = (elem) -> Collections.emptyList();

    private final Element element;
    private Function<Element, List<ElementDefinition>> populatingFunction;

    public ElementDefinitionBuilder(Element element) {
        this.element = element;
        this.populatingFunction = EMPTY_CHILDREN;
    }

    public ElementDefinitionBuilder enablePopulatingChildren() {
        this.populatingFunction = this::getChildrenFromElement;
        return this;
    }

    public ElementDefinition build() {
        ElementDefinition elementDefinition = new ElementDefinition();

        elementDefinition.setName(this.element.toString());
        elementDefinition.setKind(this.element.getKind().toString());

        elementDefinition.setAnnotations(
            this.element.getAnnotationMirrors()
            .stream()
            .map(AnnotationDefinition::new)
            .collect(Collectors.toCollection(LinkedList::new))
        );

        elementDefinition.setChildren(this.populatingFunction.apply(this.element));

        return elementDefinition;
    }

    // Retrieves all children from Element and its superclasses and superinterfaces.
    private List<ElementDefinition> getChildrenFromElement(Element element) {
        TypeElement typeElement = getTypeElement(element);
        if (typeElement != null) {
            List<Element> classHierarchy = new LinkedList<>();

            TypeElement current = typeElement;
            do {
                classHierarchy.add(current);
                current.getInterfaces()
                    .stream()
                    .map(EToolboxProcessor.getTypeUtils()::asElement)
                    .forEach(classHierarchy::add);
                current = (TypeElement) EToolboxProcessor.getTypeUtils().asElement(current.getSuperclass());
            } while (current != null && !EToolboxProcessor.isObject(current));

            Collections.reverse(classHierarchy);

            return getChildrenFromElements(classHierarchy);
        }
        return Collections.emptyList();
    }

    // Retrieves TypeElement of this element for populating children.
    // Returns null if element is primitive or fully qualified name starts with "java", "org.apache", "org.osgi"
    // because classes from those packages definitely don't have EAK annotations.
    private TypeElement getTypeElement(Element element) {
        if (element instanceof TypeElement) {
            return (TypeElement) element;
        }
        if (TypeKind.DECLARED.equals(element.asType().getKind()) && isProcessable(element.toString())) {
            // Current element represents field with class type, that means we are not able to get children of field.
            // That's why we get type of this field and then get element of this type.
            // This tricky casting helps us to get children of current field type.
            return (TypeElement) EToolboxProcessor.getTypeUtils().asElement(element.asType());
        }
        return null;
    }

    // Defines whether we should populate children of this field type.
    // Filters most common types, that definitely doesn't have EAK annotations.
    private boolean isProcessable(String elementClassName) {
        return UNSUITABLE_CLASS_NAME_PREFIXES.stream()
            .anyMatch(prefix -> StringUtils.startsWith(elementClassName, prefix));
    }

    // Retrieves children from list of elements representing class and its superclasses and interfaces.
    private List<ElementDefinition> getChildrenFromElements(List<Element> elements) {
        return elements.stream()
            .flatMap(element -> element.getEnclosedElements().stream())
            .filter(EToolboxProcessor::isProcessableAnnotationPresents)
            .map(ElementDefinition.ELEMENT_TO_ELEMENT_DEFINITION_WITH_CHILDREN)
            .collect(Collectors.toList());
    }
}
