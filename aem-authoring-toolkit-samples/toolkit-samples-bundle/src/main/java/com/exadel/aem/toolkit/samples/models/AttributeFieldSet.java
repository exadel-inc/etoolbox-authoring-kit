package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Switch;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AttributeFieldSet {

    @TextField
    @DialogField( label = "Useless text")
    @Default(values = "What is my purpose?")
    @ValueMapValue
    private String uselessText;


    @DependsOnRef(name = "switch")
    @Switch
    @DialogField(label = "Toggle for text field")
    @Default(booleanValues = false)
    @ValueMapValue
    private boolean toggle;
}
