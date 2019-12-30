package com.exadel.aem.toolkit.samples.models.fieldsets;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class WeaponFieldSet {

    @DependsOnRef(name = "checkbox")
    @Checkbox
    @DialogField( label = "Bow")
    @ValueMapValue
    private boolean bow;

    @DependsOnRef(name = "checkbox")
    @Checkbox
    @DialogField(label = "Sword")
    @ValueMapValue
    private boolean sword;

    public boolean getBow() { return bow; }
    public boolean getSword() {return sword; }
}
