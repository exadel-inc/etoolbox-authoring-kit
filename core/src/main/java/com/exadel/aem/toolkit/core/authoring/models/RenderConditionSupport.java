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

import java.io.IOException;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.granite.ui.components.rendercondition.RenderConditionHelper;

/**
 * Provides Granite render condition support for using in HTL scripts
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class RenderConditionSupport {

    private static final Logger LOG = LoggerFactory.getLogger(RenderConditionSupport.class);

    @SlingObject
    private SlingHttpServletRequest request;

    @SlingObject
    private SlingHttpServletResponse response;

    /**
     * Checks the render condition associated with the current resource
     * @return True or false
     */
    public boolean check() {
        RenderConditionHelper helper = new RenderConditionHelper(request, response);
        try {
            return helper.getRenderCondition(request.getResource()).check();
        } catch (ServletException | IOException e) {
            LOG.error("Render condition check failed", e);
        }
        return false;
    }
}
