package com.exadel.aem.toolkit.core.processor.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.processor.EToolboxProcessor;

public class AnnotationDefinition {

    private final String name;
    private final Map<String, ValueDefinition> values;

    public AnnotationDefinition(AnnotationMirror annotationMirror) {
        this.values = convert(
            annotationMirror.getElementValues(),
            EToolboxProcessor.getElementUtils().getElementValuesWithDefaults(annotationMirror)
        );
        this.name = annotationMirror.getAnnotationType().toString();
    }

    /**
     * Converts {@code Map<? extends ExecutableElement, ? extends AnnotationValue>} to "simple" {@code Map<String, Object>}
     * to easily serialize/deserialize
     */
    private Map<String, ValueDefinition> convert(Map<? extends ExecutableElement, ? extends AnnotationValue> mapWithEnteredValues,
                                        Map<? extends ExecutableElement, ? extends AnnotationValue> mapWithDefaultsValues) {

        Map<String, ValueDefinition> objectMap = new LinkedHashMap<>();
        // Iterates through key set of map with default values,
        // because map with entered values might not contain all keys
        for (ExecutableElement annotationMethod: mapWithDefaultsValues.keySet()) {
            // Gets entered value for current key
            AnnotationValue value = mapWithEnteredValues.get(annotationMethod);
            objectMap.put(
                normalizeMethodName(annotationMethod.toString()),
                buildValue(value, annotationMethod.getDefaultValue(), annotationMethod.getReturnType())
            );
        }
        return objectMap;
    }

    // Removes "()" from the end of annotation method name
    private String normalizeMethodName(String methodName) {
        return StringUtils.substringBeforeLast(methodName, "()");
    }

    // Returns serializable representation of annotation method value
    // with required data for plugin.
    private ValueDefinition buildValue(AnnotationValue enteredValue, AnnotationValue defaultValue, TypeMirror returnType) {
        return ValueDefinitionFactory.getValueDefinition(
            processAnnotationValue(enteredValue),
            processAnnotationValue(defaultValue),
            returnType
        );
    }

    // Casts annotation value to its real type.
    private Object processAnnotationValue(AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        Object innerValue = annotationValue.getValue();
        if (innerValue instanceof AnnotationMirror) {
            return new AnnotationDefinition((AnnotationMirror) annotationValue);
        }
        if (innerValue instanceof List) {
            return ((List<? extends AnnotationValue>) innerValue)
                .stream()
                .map(this::processAnnotationValue)
                .collect(Collectors.toList());
        }
        return innerValue;
    }
}
