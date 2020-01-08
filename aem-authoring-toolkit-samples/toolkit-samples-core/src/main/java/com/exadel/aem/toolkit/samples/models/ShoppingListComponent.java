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
import org.apache.commons.lang3.StringUtils;
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

    static final String TAB_MAIN = "Shopping list";

    private static final String DEFAULT_EMPTY_LIST_TEXT = "it's seems your warrior is happy and without shopping.";
    private static final String DEFAULT_NOT_WONDERFUL_TEXT = "and your warrior is not wonderful!";
    private static final String DEFAULT_WONDERFUL_TEXT = "and he thinks he is wonderful, because ";
    private static final String DEFAULT_ANSWER = "he was born like this.";

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

    @DependsOn(query = "AATSamples.getShoppingDefaultText(@@checkbox(coral-panel |> .weapon-fieldSet), @this)", action = DependsOnActions.SET)
    @DependsOn(query = "@@checkbox(coral-panel |> .products-fieldSet).every(item => item)")
    @DependsOn(query = "@@checkbox.every(item => item)", action = DependsOnActions.DISABLED)
    @TextField(emptyText = "Check all checkboxes to disable this text field")
    @DialogField(label = "Why you're such a wonderful warrior?")
    @ValueMapValue
    private String answer;

    public String getAnswer() { return StringUtils.defaultIfBlank(answer, DEFAULT_ANSWER); }

    public String getText() {
        String shoppingList = productsFieldSet.getProducts() + weaponFieldSet.getWeapon();
        StringBuilder text = new StringBuilder(shoppingList);

        if (StringUtils.isEmpty(shoppingList)) {
            text.append(DEFAULT_EMPTY_LIST_TEXT);
        } else if (weaponFieldSet.getBow() && weaponFieldSet.getSword()) {
            text.append(DEFAULT_NOT_WONDERFUL_TEXT);
        } else {
            text.append(DEFAULT_WONDERFUL_TEXT).append(getAnswer());
        }
        return text.toString();
    }
}
