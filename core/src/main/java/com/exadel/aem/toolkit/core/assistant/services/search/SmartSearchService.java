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
package com.exadel.aem.toolkit.core.assistant.services.search;

import java.util.Arrays;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import com.exadel.aem.toolkit.core.assistant.models.facilities.Facility;
import com.exadel.aem.toolkit.core.assistant.services.AssistantService;

@Component(service = AssistantService.class, immediate = true, property = "service.ranking:Integer=102")
@Designate(ocd = SmartSearchServiceConfig.class)
public class SmartSearchService implements AssistantService {

    private static final String VENDOR_NAME = "Smart Search";

    private SmartSearchServiceConfig config;
    private List<Facility> facilities;

    @Activate
    @Modified
    private void init(SmartSearchServiceConfig config) {
        this.config = config;
        if (facilities == null) {
            facilities = Arrays.asList(new SmartSearchTextFacility(this), new SmartSearchImagesFacility(this));
        }
    }

    @Override
    public String getVendorName() {
        return VENDOR_NAME;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<Facility> getFacilities() {
        return facilities;
    }

    SmartSearchServiceConfig getConfig() {
        return config;
    }
}
