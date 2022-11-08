package com.exadel.aem.toolkit.core.optionprovider.services.impl.enums;

public enum TestEnum {
    TEST_NUM_VALUE_1("textMember1", "valueMember1", "attributeMember1"),
    TEST_NUM_VALUE_2("textMember2", "valueMember2", "attributeMember2"),
    TEST_NUM_VALUE_3("textMember3", "valueMember3", "attributeMember3");

    private final String textMember;
    private final String valueMember;
    private final String attributeMember;

    TestEnum(String textMember, String valueMember, String attributeMember) {
        this.textMember = textMember;
        this.valueMember = valueMember;
        this.attributeMember = attributeMember;
    }

    public String getTextMember() {
        return textMember;
    }

    public String getValueMember() {
        return valueMember;
    }

    public String getAttributeMember() {
        return attributeMember;
    }

    @Override
    public String toString() {
        return "VALUE_" + super.toString();
    }
}
