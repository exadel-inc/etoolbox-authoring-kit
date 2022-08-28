package com.exadel.aem.toolkit.api.annotations.widgets.autocomplete3;

import org.apache.commons.lang3.StringUtils;

public enum MatchMode {
    STARTS_WITH {
        @Override
        public String toString() {
            return "startswith";
        }
    },
    CONTAINS {
        @Override
        public String toString() {
            return StringUtils.EMPTY;
        }
    }
}
