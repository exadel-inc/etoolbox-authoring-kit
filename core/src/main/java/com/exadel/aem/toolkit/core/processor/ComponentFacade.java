package com.exadel.aem.toolkit.core.processor;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypesException;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.DesignDialog;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.core.processor.models.ElementDefinition;


public class ComponentFacade {

    private static final Map<Class<? extends Annotation>, Function<Element, ElementDefinition>> annotations
        = new LinkedHashMap<>();

    static  {
        annotations.put(Dialog.class, ElementDefinition.ELEMENT_TO_ELEMENT_DEFINITION_WITH_CHILDREN);
        annotations.put(DesignDialog.class, ElementDefinition.ELEMENT_TO_ELEMENT_DEFINITION_WITH_CHILDREN);
        annotations.put(EditConfig.class, ElementDefinition.ELEMENT_TO_ELEMENT_DEFINITION);
        annotations.put(ChildEditConfig.class,ElementDefinition.ELEMENT_TO_ELEMENT_DEFINITION);
        annotations.put(HtmlTag.class, ElementDefinition.ELEMENT_TO_ELEMENT_DEFINITION);
    }

    // Retrieves all views from AemComponent and builds from them ElementDefinitions.
    // Return map, where key is file name and value is data
    public static Map<String, ElementDefinition> process(Element element) {
        AemComponent aemComponent = element.getAnnotation(AemComponent.class);
        ElementDefinition aemComponentDefinition = ElementDefinition.ELEMENT_TO_ELEMENT_DEFINITION.apply(element);

        List<Element> elements = getElementsFromAemComponentViews(aemComponent);
        elements.add(element);

        Map<String, ElementDefinition> result = new HashMap<>();
        result.put(aemComponent.path() + "/" + AemComponent.class.getSimpleName(),
            aemComponentDefinition);

        for (Map.Entry<Class<? extends Annotation>, Function<Element, ElementDefinition>> entry : annotations.entrySet()) {
            elements.stream()
                .filter(elem -> elem.getAnnotation(entry.getKey()) != null)
                .findFirst()
                .map(entry.getValue())
                .ifPresent(definition ->
                    result.put(aemComponent.path() + "/" + entry.getKey().getSimpleName(), definition));
        }

        return result;
    }

    private static List<Element> getElementsFromAemComponentViews(AemComponent aemComponent) {
        try {
            return Arrays.stream(aemComponent.views())
                .map(Class::getCanonicalName)
                .map(EToolboxProcessor.getElementUtils()::getTypeElement)
                .collect(Collectors.toList());
        } catch (MirroredTypesException typesException) {
            return typesException.getTypeMirrors()
                .stream()
                .map(EToolboxProcessor.getTypeUtils()::asElement)
                .collect(Collectors.toList());
        }
    }

    private ComponentFacade() {}
}
