package com.exadel.aem.toolkit.core.processor.models;

// Represents value of annotation method
// Fields:
//      value -- actual value of annotation method (can be null if there is no value specified by user);
//      defaultValue -- default value of annotation method (can be null if there is no default value);
//      type -- defines type of annotation method (CLASS, DOUBLE, ANNOTATION_TYPE, ENUM etc.);
//      typeName -- defines name of type (annotation name, string, double, enum name etc.);
//      isArray -- defines whether a value is array or not;
// Note: value and defaultValue can't be null at the same time.
public class ValueDefinition {

    private final Object value;
    private final Object defaultValue;
    private final String kind;
    private final String typeName;
    private final boolean isArray;

    public ValueDefinition(Object value, Object defaultValue, String kind, String typeName, boolean isArray) {
        this.value = value;
        this.defaultValue = defaultValue;
        this.kind = kind;
        this.typeName = typeName;
        this.isArray = isArray;
    }
}
