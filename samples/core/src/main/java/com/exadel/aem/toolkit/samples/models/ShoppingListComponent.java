/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exadel.aem.toolkit.samples.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import com.exadel.aem.toolkit.samples.constants.PathConstants;
import com.exadel.aem.toolkit.samples.models.fieldsets.ProductsFieldSet;
import com.exadel.aem.toolkit.samples.models.fieldsets.WeaponFieldSet;
import com.exadel.aem.toolkit.samples.utils.ListUtils;

@AemComponent(
    path = "content/shopping-list-component",
    title = "Shopping List Component",
    description = "Choose what your warrior needs",
    resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
    componentGroup = GroupConstants.COMPONENT_GROUP
)
@Dialog(
    extraClientlibs = "etoolbox-authoring-kit.samples.authoring"
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ShoppingListComponent {

    private static final String DESCRIPTION_PRODUCTS_FIELD_SET = "Check all checkboxes from this group to show the text field";

    private static final String LABEL_ANSWER = "Why are you such a wonderful warrior?";

    private static final String DEFAULT_EMPTY_LIST_TEXT = "it seems like your warrior is happy without any purchases.";
    private static final String DEFAULT_NOT_WONDERFUL_TEXT = ", and your warrior is not wonderful!";
    private static final String DEFAULT_WONDERFUL_TEXT = ", and he thinks he is wonderful, because ";
    private static final String DEFAULT_ANSWER = "he was born like this.";

    @DialogField
    @FieldSet(title = "Choose a weapon to buy")
    @Attribute(className = "weapon-fieldSet")
    @Self
    private WeaponFieldSet weaponFieldSet;

    @DialogField(description = DESCRIPTION_PRODUCTS_FIELD_SET)
    @FieldSet(title = "Choose products to buy")
    @Attribute(className = "products-fieldSet")
    @Self
    private ProductsFieldSet productsFieldSet;

    @DialogField(label = LABEL_ANSWER)
    @TextField(emptyText = "Check all checkboxes to disable this text field")
    @DependsOn(
        query = "ToolKitSamples.getShoppingDefaultText(@@checkbox(coral-dialog-content |> .weapon-fieldSet), @this)",
        action = DependsOnActions.SET
    )
    @DependsOn(query = "@@checkbox(coral-dialog-content |> .products-fieldSet).every(item => item)")
    @DependsOn(
        query = "@@checkbox.every(item => item)",
        action = DependsOnActions.DISABLED
    )
    @ValueMapValue
    private String answer;

    public String getAnswer() {
        return StringUtils.defaultIfBlank(answer, DEFAULT_ANSWER);
    }

    public String getText() {
        String shoppingList = ListUtils.joinNonBlank(ListUtils.COMMA_SPACE_DELIMITER,
            productsFieldSet.getProducts(), weaponFieldSet.getWeapon());
        StringBuilder text = new StringBuilder(shoppingList);

        if (StringUtils.isEmpty(shoppingList)) {
            text.append(DEFAULT_EMPTY_LIST_TEXT);
        } else if (weaponFieldSet.isBowChosen() && weaponFieldSet.isSwordChosen()) {
            text.append(DEFAULT_NOT_WONDERFUL_TEXT);
        } else {
            text.append(DEFAULT_WONDERFUL_TEXT).append(getAnswer());
        }
        return text.toString();
    }
}
