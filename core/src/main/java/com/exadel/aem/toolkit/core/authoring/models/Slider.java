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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.widgets.slider.SliderItem;
import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Represents the back-end part of the {@code Slider} component for Granite UI dialogs. his Sling model
 * is responsible for injecting and retrieving the properties of component's node.
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@AemComponent(
    path = "components/authoring/slider",
    title = "Slider",
    resourceSuperType = "etoolbox-authoring-kit/components/authoring/base"
)
public class Slider extends BaseModel {

    @SlingObject
    private Resource resource;

    @SlingObject
    private SlingHttpServletRequest request;

    @ValueMapValue(name = CoreConstants.PN_VALUE)
    private String defaultValue;

    @ValueMapValue
    private int min;

    @ValueMapValue
    @Default(intValues = 100)
    private int max;

    @ValueMapValue
    @Default(intValues = 1)
    private int step;

    @ValueMapValue
    @Default(values = "horizontal")
    private String orientation;

    @ValueMapValue
    private boolean filled;

    @ValueMapValue
    private String startValue;

    @ValueMapValue
    private String endValue;

    @ValueMapValue
    private boolean ranged;

    private Map<String, String> items;

    /**
     * Maps the {@link SliderItem#value(),SliderItem#text()} properties upon this Sling model initialization
     */
    @PostConstruct
    private void init() {
        items = new HashMap<>();
        Resource resourceChild = resource.getChild(CoreConstants.NN_ITEMS);
        if (resourceChild != null) {
            Iterator<Resource> resources = resourceChild.listChildren();
            while (resources.hasNext()) {
                Resource it = resources.next();
                String value = Optional.ofNullable(it)
                    .map(Resource::getValueMap)
                    .map(valueMap -> valueMap.get(CoreConstants.PN_VALUE, String.class))
                    .orElse(null);
                String text = Optional.ofNullable(it)
                    .map(Resource::getValueMap)
                    .map(valueMap -> valueMap.get(CoreConstants.PN_TEXT, String.class))
                    .orElse(null);
                items.put(value, text);
            }
        }
    }

    /**
     * Retrieves the minimum specified value of current property
     * @return int
     */
    public int getMin() {
        return min;
    }

    /**
     * Retrieves the maximum specified value of current property
     * @return int
     */
    public int getMax() {
        return max;
    }

    /**
     * Retrieves the value specified for each step of the slider
     * @return int
     */
    public int getStep() {
        return step;
    }

    /**
     * Retrieves the specified orientation of the slider
     * @return String
     */
    public String getOrientation() {
        return orientation;
    }

    /**
     * Retrieves the specification of the slider if it is filled or not
     * @return true or false
     */
    public boolean isFilled() {
        return filled;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Retrieves the specified start value of the ranged slider
     * @return String
     */
    public String getStartValue() {
        return startValue;
    }

    /**
     * Retrieves the specified end value of the ranged slider
     * @return String
     */
    public String getEndValue() {
        return endValue;
    }

    /**
     * Retrieves the specification of the slider if it is ranged or not
     * @return true or false
     */
    public boolean isRanged() {
        return ranged;
    }

    /**
     * Retrieves the collection of properties of the current list item
     * @return {@code Map} object, non-null
     */
    public Map<String, String> getItems() {
        return items;
    }
}
