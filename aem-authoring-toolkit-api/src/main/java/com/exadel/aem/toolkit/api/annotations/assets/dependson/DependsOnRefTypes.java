package com.exadel.aem.toolkit.api.annotations.assets.dependson;

/**
 * Represents acceptable {@code dependsOnRef} values for use with {@link DependsOnRef} annotation
 */
public enum DependsOnRefTypes {
    AUTO {
        @Override
        public String toString() {
            return "";
        }
    },
    ANY,
    BOOLEAN,
    BOOLSTRING,
    NUMBER,
    STRING
}
