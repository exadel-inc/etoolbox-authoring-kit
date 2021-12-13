package com.exadel.aem.toolkit.core.processor.models;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.exadel.aem.toolkit.core.processor.EToolboxProcessor;

public class ValueDefinitionFactory {

    public static ValueDefinition getValueDefinition(Object value, Object defaultValue, TypeMirror returnType) {
        // Check for array type
        if (returnType.getKind().equals(TypeKind.ARRAY)) {
            TypeMirror componentType = ((ArrayType) returnType).getComponentType();
            if (componentType.getKind().isPrimitive()) {
                // Array of primitive type
                return new ValueDefinition(
                    value,
                    defaultValue,
                    componentType.getKind().toString(),
                    componentType.toString(),
                    true
                );
            } else {
                // Array of class, enum or annotations
                return new ValueDefinition(
                    processArray(value),
                    processArray(defaultValue),
                    getExactKind(componentType),
                    componentType.toString(),
                    true
                );
            }
        } else {
            if (returnType.getKind().isPrimitive()) {
                // Single primitive type
                return new ValueDefinition(
                    value,
                    defaultValue,
                    returnType.getKind().toString(),
                    returnType.toString(),
                    false
                );
            } else {
                // Single class, enum or annotation
                return new ValueDefinition(
                    processSingle(value),
                    processSingle(defaultValue),
                    getExactKind(returnType),
                    returnType.toString(),
                    false
                );
            }
        }
    }

    // Transforms list of objects to serializable list. Because we don't need to serialize
    // all data from list of TypeMirror or VariableElement.
    private static Object processArray(Object value) {
        if (value == null) {
            return null;
        }
        return ((List<?>) value).stream()
            .map(ValueDefinitionFactory::processSingle)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    // Transforms object to serializable object. Because we don't need to serialize
    // all data from TypeMirror or VariableElement.
    private static Object processSingle(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof AnnotationDefinition) {
            return value;
        }
        return value.toString();
    }

    // Gets string representation of return type kind.
    // Transforms TypeMirror to Element and gets kind of element.
    // Because of this casting we can define whether this is ANNOTATION_TYPE, CLASS or ENUM.
    private static String getExactKind(TypeMirror typeMirror) {
        return EToolboxProcessor.getTypeUtils()
            .asElement(typeMirror)
            .getKind()
            .toString();
    }
}
