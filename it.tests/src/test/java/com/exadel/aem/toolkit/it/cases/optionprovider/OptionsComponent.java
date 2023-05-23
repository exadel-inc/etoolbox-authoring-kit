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
package com.exadel.aem.toolkit.it.cases.optionprovider;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnParam;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionSource;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.it.cases.Constants;

@AemComponent(
    path = Constants.JCR_COMPONENTS_ROOT + "/options",
    title = "Options Component",
    componentGroup = Constants.GROUP_COMPONENTS,
    disableTargeting = true
)
@Dialog
public class OptionsComponent {

    private static final String TAGS_ROOT = "/content/etoolbox-authoring-kit-test/cq:tags/";

    @DialogField(label = "Greeting")
    @Select(
        optionProvider = @OptionProvider(
            value = @OptionSource("/content/etoolbox-authoring-kit-test/lists/greetings"),
            prepend = "None:none",
            append = {"你好:zh", "Hi:ww"},
            exclude = {"es", "it"},
            selectedValue = "en")
    )
    private String greeting;

    @DialogField(label = "Social Channel")
    @RadioGroup(
        buttonProvider = @OptionProvider(
            value = @OptionSource(
                value = "/content/etoolbox-authoring-kit-test/lists/social-channels",
                textMember = "title",
                valueMember = "id"),
            exclude = "test",
            prepend = "None:none",
            selectedValue = "rss"
        )
    )
    private String socialChannel;

    @DialogField(label = "Pages")
    @Select(
        optionProvider = @OptionProvider(
            @OptionSource(
                value = "/content/etoolbox-authoring-kit-test/allowedChildren/static-page",
                textMember = "@name",
                valueMember = "jcr:created",
                textTransform = StringTransformation.CAPITALIZE)
        )
    )
    private String page;

    @DialogField(label = "Fruit 1")
    @RadioGroup(
        buttonProvider = @OptionProvider(
            value = @OptionSource(value = TAGS_ROOT + "fruit"),
            selectedValue = "fruit:banana"
        )
    )
    private String fruit;

    @DialogField(label = "Colors 1")
    @Select(
        optionProvider = @OptionProvider(
            value = {
                @OptionSource(
                    enumeration = Colors.class,
                    textTransform = StringTransformation.CAPITALIZE,
                    valueMember = "toString"
                ),
                @OptionSource(
                    enumeration = MoreColors.class,
                    textTransform = StringTransformation.CAPITALIZE,
                    valueMember = "toString"
                ),
            },
            sorted = true,
            prepend = "None:none",
            selectedValue = "Orange")
    )
    private String color1;

    @DialogField(label = "Colors 2")
    @RadioGroup(
        buttonProvider = @OptionProvider(
            @OptionSource(
                enumeration = ColorConstants.class,
                textMember = "LABEL_*",
                valueMember = "VALUE_*"
            )
        )
    )
    private String color2;

    @DialogField(label = "Users")
    @Select(
        optionProvider = @OptionProvider(
            value = @OptionSource(value = "https://jsonplaceholder.typicode.com/users", textMember = "name", valueMember = "email")
        )
    )
    private String user;

    @DialogField(label = "Multisource")
    @RadioGroup(
        buttonProvider = @OptionProvider(@OptionSource("/content/etoolbox-authoring-kit-test/lists/multisource"))
    )
    @DependsOnRef
    private String multisourceSelection;

    @DialogField
    @Select
    @DependsOn(
        action = DependsOnActions.UPDATE_OPTIONS,
        query = "@multisourceSelection",
        params = @DependsOnParam(name = "valueMember", value = "@name")
    )
    private String multisource;
}
