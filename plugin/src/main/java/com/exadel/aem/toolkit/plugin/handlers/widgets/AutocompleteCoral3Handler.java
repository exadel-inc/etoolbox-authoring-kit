package com.exadel.aem.toolkit.plugin.handlers.widgets;

import com.exadel.aem.toolkit.api.annotations.widgets.autocompletecoral3.Autocomplete;
import com.exadel.aem.toolkit.api.annotations.widgets.autocompletecoral3.AutocompleteOption;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

import org.apache.commons.lang3.ArrayUtils;

@Handles(Autocomplete.class)
public class AutocompleteCoral3Handler extends OptionProviderHandler implements Handler {

    @Override
    public void accept(Source source, Target target) {
        Autocomplete autocomplete = source.adaptTo(Autocomplete.class);
        if (hasProvidedOptions(autocomplete.optionProvider())) {
            appendOptionProvider(autocomplete.optionProvider(), target);
        }
        if (ArrayUtils.isNotEmpty(autocomplete.options())) {
            Target items = target.getOrCreateTarget(DialogConstants.NN_ITEMS);
            for (AutocompleteOption autocompleteOption : autocomplete.options()) {
                appendOption(autocompleteOption, autocompleteOption.value(), items);
            }
        }
    }
}
