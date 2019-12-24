package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AttributeFieldSet {

    @DependsOnRef(name = "checkbox")
    @Checkbox
    @DialogField( label = "First checkbox")
    @ValueMapValue
    private boolean checkbox1;


    @DependsOnRef(name = "checkbox")
    @Checkbox
    @DialogField(label = "Second checkbox")
    @ValueMapValue
    private boolean checkbox2;
}
