package com.exadel.aem.toolkit.api.annotations.assets.dependson;

/**
 * Represents acceptable {@code dependsOnRef} values for use with {@link DependsOnRef} annotation
 */
@SuppressWarnings("unused")
public enum DependsOnRefTypes {
    /**
     * Type will be chosen automatically based on element widget type
     */
    AUTO {
        @Override
        public String toString() {
            return "";
        }
    },
    /**
     * No type cast is performed
     */
    ANY,
    /**
     * Cast to boolean (according to JS cast rules)
     */
    BOOLEAN,
    /**
     * Cast as a string value to boolean (true if string cast equals "true")
     */
    BOOLSTRING,
    /**
     * Cast to number value
     */
    NUMBER,
    /**
     * Cast to string
     */
    STRING,
    /**
     * Parse as JSON value
     */
    JSON
}
