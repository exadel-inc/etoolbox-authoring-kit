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
package com.exadel.aem.toolkit.core.authoring.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.factory.ModelFactory;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Represents the back-end part of the {@code Autocomplete} component for Granite UI dialogs. His Sling model
 * is responsible for injecting and retrieving the properties of component's node.
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@AemComponent(
    path = "components/authoring/autocomplete",
    title = "Autocomplete Coral 3",
    resourceSuperType = "etoolbox-authoring-kit/components/authoring/base"
)
public class Autocomplete extends BaseModel {

    @SlingObject
    private SlingHttpServletRequest request;

    @OSGiService
    private OptionProviderService optionProvider;

    @OSGiService
    private ModelFactory modelFactory;

    @ChildResource
    @Named("items")
    private List<Option> options = new ArrayList<>();

    @ValueMapValue
    private String placeholder;

    @ValueMapValue
    private boolean matchStartsWith;

    @ValueMapValue
    private String icon;

    @ValueMapValue
    private boolean disabled;

    @ValueMapValue
    private boolean invalid;

    @ValueMapValue
    private boolean loading;

    @ValueMapValue
    private boolean multiple;

    /**
     * Retrieves the list of {@link Option} objects
     * @return List of {@link Option} objects
     */
    public List<Option> getOptions() {
        return options;
    }

    /**
     * Retrieves the specified placeholder value of the autocomplete
     * @return String
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * Retrieves the specification of the autocomplete if it is match text by starting with or contains
     * @return true or false
     */
    public boolean isMatchStartsWith() {
        return matchStartsWith;
    }

    /**
     * Retrieves the specified icon value of the autocomplete
     * @return String
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Retrieves the specification of the autocomplete if it is disabled or not
     * @return true or false
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Retrieves the specification of the autocomplete if it is invalid or not
     * @return true or false
     */
    public boolean isInvalid() {
        return invalid;
    }

    /**
     * Retrieves the specification of the autocomplete if it is show loading or not
     * @return true or false
     */
    public boolean isLoading() {
        return loading;
    }

    /**
     * Retrieves the specification of the autocomplete if it is support multiple values or not
     * @return true or false
     */
    public boolean isMultiple() {
        return multiple;
    }

    /**
     * Initializes this model per Sling Model standard
     */
    @PostConstruct
    private void init() {
        List<Option> optionsModels = optionProvider
            .getOptions(request)
            .stream()
            .map(option -> modelFactory.createModel(option, Option.class))
            .collect(Collectors.toList());
        this.options.addAll(optionsModels);
    }
}
