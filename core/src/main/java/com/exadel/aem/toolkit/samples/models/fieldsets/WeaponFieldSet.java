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
public class WeaponFieldSet {

    private static final String LABEL_BOW = "Bow";

    private static final String LABEL_SWORD = "Sword";

    @DialogField(label = LABEL_BOW)
    @Checkbox
    @DependsOnRef(name = "checkbox")
    @ValueMapValue
    private boolean bowChosen;

    @DialogField(label = LABEL_SWORD)
    @Checkbox
    @DependsOnRef(name = "checkbox")
    @ValueMapValue
    private boolean swordChosen;

    public boolean isBowChosen() {
        return bowChosen;
    }

    public boolean isSwordChosen() {
        return swordChosen;
    }

    private String getBow() {
        return bowChosen ? "bow" : StringUtils.EMPTY;
    }

    private String getSword() {
        return swordChosen ? "sword" : StringUtils.EMPTY;
    }

    public String getWeapon() {
        return ListUtils.joinNonBlank(ListUtils.COMMA_SPACE_DELIMITER, getBow(), getSword());
    }
}
