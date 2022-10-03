package com.exadel.aem.toolkit.core.injectors.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.EnumValue;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressWarnings("unused")
@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TestModelValueMapEnumValueInjector {

    @EnumValue
    private TestingNeededEnum plainOldEnumField;
    @EnumValue(name = "custom")
    private TestingNeededEnum enumValueWithCustomName;
    @EnumValue
    @Named("custom")
    private TestingNeededEnum enumValueWithCustomName2;
    @EnumValue(fieldName = "value")
    private TestingNeededEnum enumValueByFieldName;

    @EnumValue(name = "custom2", fieldName = "value")
    private TestingNeededEnum enumValueWithCustomNameAndValue;
    @Default(values = "VAL1")
    @EnumValue
    private TestingNeededEnum withDefaultValueString;
    @Default(values = {"VAL2","VAL1"})
    @EnumValue
    private TestingNeededEnum[] withDefaultValuesStringArray;

    @EnumValue(fieldName = "integer")
    private TestingNeededEnumWithIntegerField enumValueWithIntegerField;
    @EnumValue(fieldName = "i")
    private TestingNeededEnumWithIntField enumValueWithIntField;
    @EnumValue
    @Default(values = "VAL3")
    private TestingNeededEnumWithIntegerField enumValueWithIntegerFieldAndDefaultValue;
    @EnumValue
    @Default(values = {"VAL2","VAL3"})
    private TestingNeededEnumWithIntegerField[] withDefaultValuesIntArray;

    private final ITestModelValueMapEnumValue.TestingNeededEnumFromInterface enumFromConstructor;

    private ITestModelValueMapEnumValue.TestingNeededEnumFromInterface enumFromMethod;

    @Inject
    public TestModelValueMapEnumValueInjector(@EnumValue ITestModelValueMapEnumValue.TestingNeededEnumFromInterface enumFromConstructor) {
        this.enumFromConstructor = enumFromConstructor;
    }

    @Inject
    public void setEnumFromMethod(@EnumValue ITestModelValueMapEnumValue.TestingNeededEnumFromInterface enumFromMethod) {
        this.enumFromMethod = enumFromMethod;
    }

    public ITestModelValueMapEnumValue.TestingNeededEnumFromInterface getEnumFromMethod() {
        return enumFromMethod;
    }

    public TestingNeededEnum getPlainOldEnumField() {
        return plainOldEnumField;
    }
    public TestingNeededEnum getEnumValueWithCustomName() {
        return enumValueWithCustomName;
    }
    public TestingNeededEnum getEnumValueWithCustomName2() {
        return enumValueWithCustomName2;
    }
    public TestingNeededEnum getEnumValueByFieldName() {
        return enumValueByFieldName;
    }
    public TestingNeededEnum getEnumValueWithCustomNameAndValue() {
        return enumValueWithCustomNameAndValue;
    }
    public TestingNeededEnum getWithDefaultValueString() {
        return withDefaultValueString;
    }
    public TestingNeededEnum[] getWithDefaultValuesStringArray() {
        return withDefaultValuesStringArray;
    }
    public TestingNeededEnumWithIntegerField getEnumValueWithIntegerField() {
        return enumValueWithIntegerField;
    }
    public TestingNeededEnumWithIntField getEnumValueWithIntField() {
        return enumValueWithIntField;
    }
    public TestingNeededEnumWithIntegerField getEnumValueWithIntegerFieldAndDefaultValue() {
        return enumValueWithIntegerFieldAndDefaultValue;
    }
    public TestingNeededEnumWithIntegerField[] getWithDefaultValuesIntArray() {
        return withDefaultValuesIntArray;
    }

    public Object getEnumFromConstructor() {
        return enumFromConstructor;
    }

    public enum TestingNeededEnum {
        VAL1("value1"), VAL2("value2"), VAL3("value3");
        private final String value;
        TestingNeededEnum(String value) {
            this.value = value;
        }
    }

    public enum TestingNeededEnumWithIntField {
        VAL1(1), VAL2(2), VAL3(3);
        private final int i;
        TestingNeededEnumWithIntField(int i) {
            this.i = i;
        }
    }

    public enum TestingNeededEnumWithIntegerField {
        VAL1(1), VAL2(2), VAL3(3);
        private final Integer integer;
        TestingNeededEnumWithIntegerField(Integer integer) {
            this.integer = integer;
        }
    }

}
