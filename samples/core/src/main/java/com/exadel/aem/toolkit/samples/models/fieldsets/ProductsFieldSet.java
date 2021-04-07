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

package com.exadel.aem.toolkit.samples.models.fieldsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.samples.utils.ListUtils;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ProductsFieldSet {

    private static final String LABEL_MILK = "Milk";

    private static final String LABEL_CHEESE = "Cheese";

    @DialogField(label = LABEL_MILK)
    @Checkbox
    @DependsOnRef(name = "checkbox")
    @ValueMapValue
    private boolean milkChosen;

    @DialogField(label = LABEL_CHEESE)
    @Checkbox
    @DependsOnRef(name = "checkbox")
    @ValueMapValue
    private boolean cheeseChosen;

    public boolean isMilkChosen() {
        return milkChosen;
    }

    public boolean isCheeseChosen() {
        return cheeseChosen;
    }

    private String getMilk() {
        return milkChosen ? "milk" : StringUtils.EMPTY;
    }

    private String getCheese() {
        return cheeseChosen ? "cheese" : StringUtils.EMPTY;
    }

    public String getProducts() {
        return ListUtils.joinNonBlank(ListUtils.COMMA_SPACE_DELIMITER, getMilk(), getCheese());
    }
}
