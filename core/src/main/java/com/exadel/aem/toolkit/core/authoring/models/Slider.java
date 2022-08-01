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

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.api.annotations.widgets.slider.SliderItem;

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

    @ValueMapValue
    private SliderItem[] items;

    @ValueMapValue
    private Map<String, String> itemsMap;

    @PostConstruct
    private void init() {
        itemsMap = new HashMap<>();
        Resource resourceChild = resource.getChild(CoreConstants.NN_ITEMS);
        if (resourceChild != null) {
            Iterator<Resource> resources = resourceChild.listChildren();
            while (resources.hasNext()) {
                Resource it = resources.next();
                itemsMap.put(it.getValueMap().get(CoreConstants.PN_VALUE, String.class), it.getValueMap().get(CoreConstants.PN_TEXT, String.class));
            }
        }
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getStep() {
        return step;
    }

    public String getOrientation() {
        return orientation;
    }

    public boolean isFilled() {
        return filled;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getStartValue() {
        return startValue;
    }

    public String getEndValue() {
        return endValue;
    }

    public boolean isRanged() {
        return ranged;
    }

    public SliderItem[] getItems() {
        return items;
    }

    public Map<String, String> getItemsMap() {
        return itemsMap;
    }
}
