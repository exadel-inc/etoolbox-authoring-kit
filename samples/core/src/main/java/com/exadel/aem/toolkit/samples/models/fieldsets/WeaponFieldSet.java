package com.exadel.aem.toolkit.samples.models.fieldsets;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.samples.utils.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

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
