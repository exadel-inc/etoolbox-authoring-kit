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
package com.exadel.aem.toolkit.core.ai.servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.ai.models.facility.Facility;
import com.exadel.aem.toolkit.core.ai.models.facility.FacilityCollector;
import com.exadel.aem.toolkit.core.ai.models.solution.Solution;
import com.exadel.aem.toolkit.core.ai.services.AiService;
import com.exadel.aem.toolkit.core.utils.ThrowingBiConsumer;

@Component(
    service = Servlet.class,
    property = {
        ServletResolverConstants.SLING_SERVLET_PATHS + CoreConstants.OPERATOR_EQUALS + "/apps/etoolbox-authoring-kit/ai/facilities",
        ServletResolverConstants.SLING_SERVLET_PATHS + CoreConstants.OPERATOR_EQUALS + "/apps/etoolbox-authoring-kit/ai/exec",
        ServletResolverConstants.SLING_SERVLET_METHODS + CoreConstants.OPERATOR_EQUALS + HttpConstants.METHOD_GET,
        ServletResolverConstants.SLING_SERVLET_METHODS + CoreConstants.OPERATOR_EQUALS + HttpConstants.METHOD_POST
    })
public class AiServlet extends SlingSafeMethodsServlet {
    private static final Logger LOG = LoggerFactory.getLogger(AiServlet.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final FacilityCollector FACILITY_COLLECTOR = new FacilityCollector();

    private static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";
    private static final String QUERY_PARAMETER_COMMAND = "cmd";

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    private volatile List<AiService> services;

    private Map<String, ThrowingBiConsumer<SlingHttpServletRequest, SlingHttpServletResponse, IOException>> operations;

    @Activate
    private void activate() {
        operations = new HashMap<>();
        operations.put("facilities", this::doGetFacilities);
        operations.put("exec", this::doExecuteFacility);
    }

    @Override
    protected void doGet(
        @Nonnull SlingHttpServletRequest request,
        @Nonnull SlingHttpServletResponse response)
        throws IOException {

        response.setContentType(CONTENT_TYPE_JSON);
        String operation = StringUtils.substringAfterLast(request.getRequestPathInfo().getResourcePath(), CoreConstants.SEPARATOR_SLASH);
        if (!operations.containsKey(operation)) {
            String exceptionMessage = "Invalid operation: " + operation;
            LOG.warn(exceptionMessage);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(Solution.fromMessage(exceptionMessage).asJson());
            return;
        }
        if (services == null || services.stream().noneMatch(AiService::isEnabled)) {
            String exceptionMessage = "No enabled AI services";
            LOG.error(exceptionMessage);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(Solution.fromMessage(exceptionMessage).asJson());
            return;
        }
        operations.get(operation).accept(request, response);
    }

    private void doGetFacilities(
        SlingHttpServletRequest request,
        SlingHttpServletResponse response)
        throws IOException {

        List<Facility> facilities = getFacilities();
        OBJECT_MAPPER.writeValue(response.getWriter(), facilities);
    }

    private void doExecuteFacility(
        SlingHttpServletRequest request,
        SlingHttpServletResponse response)
        throws IOException {

        String command = Optional.ofNullable(request.getRequestParameter(QUERY_PARAMETER_COMMAND))
            .map(RequestParameter::getString)
            .orElse(null);
        Facility matchingFacility = getFacilities()
            .stream()
            .flatMap(facility -> facility.getVariants().stream())
            .filter(facility -> facility.getId().equals(command))
            .findFirst()
            .orElse(null);

        if (matchingFacility == null) {
            String exceptionMessage = "Invalid command to execute: " + command;
            LOG.warn(exceptionMessage);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(Solution.fromMessage(exceptionMessage).asJson());
            return;
        }

        Solution result = matchingFacility.execute(getArguments(request));
        response.setContentType(CONTENT_TYPE_JSON);
        response.getWriter().println(result.asJson());
    }

    private List<Facility> getFacilities() {
        return services
            .stream()
            .filter(AiService::isEnabled)
            .flatMap(service -> service.getFacilities().stream())
            .sorted(Comparator.comparing(Facility::getTitle))
            .collect(FACILITY_COLLECTOR);
    }

    private static int compareFacilities(Facility first, Facility second) {
        int sortByRanking = first.getRanking() - second.getRanking();
        if (sortByRanking != 0) {
            return sortByRanking;
        }
        return StringUtils.compare(first.getTitle(), second.getTitle());
    }
}
