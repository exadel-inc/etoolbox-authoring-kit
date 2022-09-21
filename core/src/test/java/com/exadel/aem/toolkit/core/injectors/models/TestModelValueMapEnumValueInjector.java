package com.exadel.aem.toolkit.core.injectors.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.EnumValue;

@SuppressWarnings("unused")
@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TestModelValueMapEnumValueInjector {

    @EnumValue
    private TestingNeededEnum plainOldEnumField;
    @EnumValue(fieldName = "value")
    private TestingNeededEnum enumValueByFieldName;
    @EnumValue(name = "custom")
    private TestingNeededEnum enumValueWithCustomName;
    @EnumValue(name = "custom2", fieldName = "value")
    private TestingNeededEnum enumValueWithCustomNameAndValue;
    @EnumValue(fieldName = "integer")
    private TestingNeededEnumWithIntegerField enumValueWithIntegerField;
    @EnumValue(fieldName = "i")
    private TestingNeededEnumWithIntField enumValueWithIntField;

    public TestingNeededEnum getPlainOldEnumField() {
        return plainOldEnumField;
    }

    public TestingNeededEnum getEnumValueByFieldName() {
        return enumValueByFieldName;
    }

    public TestingNeededEnum getEnumValueWithCustomName() {
        return enumValueWithCustomName;
    }

    public TestingNeededEnum getEnumValueWithCustomNameAndValue() {
        return enumValueWithCustomNameAndValue;
    }

    public TestingNeededEnumWithIntegerField getEnumValueWithIntegerField() {
        return enumValueWithIntegerField;
    }

    public TestingNeededEnumWithIntField getEnumValueWithIntField() {
        return enumValueWithIntField;
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
