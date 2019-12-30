package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.samples.constants.PathConstants;
import com.exadel.aem.toolkit.samples.models.fieldsets.ProductsFieldSet;
import com.exadel.aem.toolkit.samples.models.fieldsets.WeaponFieldSet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Dialog(
        name = "content/shopping-list-component",
        title = "Shopping list component",
        description = "Choose what your warrior needs",
        resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
        componentGroup = "Toolkit Samples",
        tabs= {
                @Tab(title = ShoppingListComponent.TAB_MAIN),
        }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ShoppingListComponent {

    public static final String TAB_MAIN = "Shopping list";

    @Attribute(clas = "weapon-fieldSet")
    @FieldSet(title = "Choose weapon to buy")
    @DialogField()
    @Self
    private WeaponFieldSet weaponFieldSet;

    @Attribute(clas = "products-fieldSet")
    @FieldSet(title = "Choose products to buy")
    @DialogField(description = "Check all checkboxes from this group to show text field")
    @Self
    private ProductsFieldSet productsFieldSet;

    @DependsOn(query = "@@checkbox(coral-panel |> .weapon-fieldSet).every(item => item) ? 'Too many weapons for wonderful hero >:C' : @this", action = DependsOnActions.SET)
    @DependsOn(query = "@@checkbox(coral-panel |> .products-fieldSet).every(item => item)")
    @DependsOn(query = "@@checkbox.every(item => item)", action = DependsOnActions.DISABLED)
    @TextField(emptyText = "Check all checkboxes to disable this text field")
    @DialogField(label = "Why you're such a wonderful warrior?")
    @ValueMapValue
    private String answer;

    public String getAnswer() {
        return (answer == null || "".equals(answer))
                ? "he was born like this."
                : answer;
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        sb.append(productsFieldSet.getMilk() ? "milk, " : "");
        sb.append(productsFieldSet.getCheese() ? "cheese, " : "");
        sb.append(weaponFieldSet.getSword() ? "sword, " : "");
        sb.append(weaponFieldSet.getBow() ? "bow, " : "");
        if ("".equals(sb.toString())) {
            sb.append("it's seems your warrior is happy and without shopping.");
        } else if (weaponFieldSet.getBow() && weaponFieldSet.getSword()) {
            sb.append("and your warrior is not wonderful!");
        } else {
            sb.append("and he thinks he is wonderful, because ").append(getAnswer());
        }
        return sb.toString();
    }
}
