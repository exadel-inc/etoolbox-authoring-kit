package com.exadel.aem.toolkit.core.authoring.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Named;

import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;

import org.apache.sling.models.factory.ModelFactory;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@AemComponent(
    path = "components/authoring/autocomplete",
    title = "Autocomplete Coral 3",
    resourceSuperType = "etoolbox-authoring-kit/components/authoring/base"
)
public class Autocomplete extends BaseModel{

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

    public List<Option> getOptions() {
        return options;
    }
    public String getPlaceholder() {
        return placeholder;
    }
    public boolean isMatchStartsWith() {
        return matchStartsWith;
    }
    public String getIcon() {
        return icon;
    }
    public boolean isDisabled() {
        return disabled;
    }
    public boolean isInvalid() {
        return invalid;
    }
    public boolean isLoading() {
        return loading;
    }
    public boolean isMultiple() {
        return multiple;
    }

    @PostConstruct
    private void init() {
        List<Resource> options = optionProvider.getOptions(request);
        List<Option> optionsModels = options.stream().map(option -> modelFactory.createModel(option, Option.class)).collect(Collectors.toList());
        this.options.addAll(optionsModels);
    }
}
