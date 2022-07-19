package com.exadel.aem.toolkit.api.annotations.widgets.autocompletecoral3;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;

public @interface AutocompleteOption {

    String text();

    @PropertyRendering(allowBlank = true)
    String value();
}
