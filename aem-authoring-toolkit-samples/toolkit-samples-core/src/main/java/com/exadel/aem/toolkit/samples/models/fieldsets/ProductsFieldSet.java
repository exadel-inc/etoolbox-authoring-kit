package com.exadel.aem.toolkit.samples.models.fieldsets;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ProductsFieldSet {

    @DependsOnRef(name = "checkbox")
    @Checkbox
    @DialogField(label = "Milk")
    @ValueMapValue
    private boolean milk;

    @DependsOnRef(name = "checkbox")
    @Checkbox
    @DialogField(label = "Cheese")
    @ValueMapValue
    private boolean cheese;

    public boolean getMilk() {
        return milk;
    }

    public boolean getCheese() {
        return cheese;
    }

    private String getStringMilk() {
        return milk ? "milk, " : "";
    }

    private String getStringCheese() {
        return cheese ? "cheese, " : "";
    }

    public String getProducts() {
        return (getStringMilk() + getStringCheese());
    }
}
