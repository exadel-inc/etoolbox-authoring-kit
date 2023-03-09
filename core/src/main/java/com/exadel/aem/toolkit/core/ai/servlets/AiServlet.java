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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.ai.AssistantConstants;
import com.exadel.aem.toolkit.core.ai.models.facility.Facility;
import com.exadel.aem.toolkit.core.ai.models.facility.FacilityCollector;
import com.exadel.aem.toolkit.core.ai.models.solution.Solution;
import com.exadel.aem.toolkit.core.ai.services.AiService;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;
import com.exadel.aem.toolkit.core.utils.ThrowingBiConsumer;

@Component(
    service = Servlet.class,
    property = {
        ServletResolverConstants.SLING_SERVLET_PATHS + CoreConstants.OPERATOR_EQUALS + "/apps/etoolbox-authoring-kit/assistant/facilities",
        ServletResolverConstants.SLING_SERVLET_PATHS + CoreConstants.OPERATOR_EQUALS + "/apps/etoolbox-authoring-kit/assistant/exec",
        ServletResolverConstants.SLING_SERVLET_METHODS + CoreConstants.OPERATOR_EQUALS + HttpConstants.METHOD_GET,
        ServletResolverConstants.SLING_SERVLET_METHODS + CoreConstants.OPERATOR_EQUALS + HttpConstants.METHOD_POST
    })
public class AiServlet extends SlingAllMethodsServlet {
    private static final Logger LOG = LoggerFactory.getLogger(AiServlet.class);

    private static final String QUERY_PARAMETER_COMMAND = "cmd";
    private static final String QUERY_PARAMETER_FILTER = "filter";

    private static final String OPERATION_FACILITIES = "facilities";
    private static final String OPERATION_EXECUTE = "exec";

    private static final String FACILITY_ROLE_UTIL = "util.";

    private static final FacilityCollector FACILITY_COLLECTOR = new FacilityCollector();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @SuppressWarnings("java:S3077")
    private transient volatile List<AiService> services;

    private transient Map<String, ThrowingBiConsumer<SlingHttpServletRequest, SlingHttpServletResponse, IOException>> operations;

    @Activate
    private void activate() {
        operations = new HashMap<>();
        operations.put(OPERATION_FACILITIES, this::doGetFacilities);
        operations.put(OPERATION_EXECUTE, this::doExecute);
    }

    @Override
    protected void service(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        if (!StringUtils.equalsAnyIgnoreCase(request.getMethod(), AssistantConstants.HTTP_METHOD_GET, AssistantConstants.HTTP_METHOD_POST)) {
            this.doGeneric(request, response);
        }
        response.setContentType(CoreConstants.CONTENT_TYPE_JSON);
        String operation = StringUtils.substringAfterLast(
            request.getRequestPathInfo().getResourcePath(),
            CoreConstants.SEPARATOR_SLASH);

        if (!operations.containsKey(operation)) {
            String exceptionMessage = "Invalid operation: " + operation;
            LOG.warn(exceptionMessage);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String responseContent = Solution.from(exceptionMessage).asJson();
            response.getWriter().println(responseContent);
            return;
        }
        if (services == null || services.stream().noneMatch(AiService::isEnabled)) {
            String exceptionMessage = "No enabled Assistant services";
            LOG.error(exceptionMessage);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String responseContent = Solution.from(exceptionMessage).asJson();
            response.getWriter().println(responseContent);
            return;
        }
        operations.get(operation).accept(request, response);
    }

    /* --------------------
       Facilities retrieval
       -------------------- */

    private void doGetFacilities(
        SlingHttpServletRequest request,
        SlingHttpServletResponse response)
        throws IOException {

        String filterParameter = Optional.ofNullable(request.getRequestParameter(QUERY_PARAMETER_FILTER))
            .map(RequestParameter::getString)
            .orElse(null);
        Predicate<Facility> filter = StringUtils.isNotEmpty(filterParameter)
            ? facility -> StringUtils.startsWith(facility.getId(), filterParameter)
            : facility -> !StringUtils.contains(facility.getId(), FACILITY_ROLE_UTIL);
        List<Facility> facilities = collectFacilities(filter);
        List<VendorDto> vendors = collectVendors();
        response.getWriter().write(ObjectConversionUtil.toJson(new FacilitiesDto(facilities, vendors)));
    }

    /* --------------------
       Facilities execution
       -------------------- */

    private void doExecute(
        SlingHttpServletRequest request,
        SlingHttpServletResponse response)
        throws IOException {

        String command = Optional.ofNullable(request.getRequestParameter(QUERY_PARAMETER_COMMAND))
            .map(RequestParameter::getString)
            .orElse(null);

        Facility matchingFacility = collectFacilities(facility -> facility.isAllowed(request))
            .stream()
            .flatMap(facility -> facility.getVariants().isEmpty() ? Stream.of(facility) : facility.getVariants().stream())
            .filter(facility -> facility.getId().equals(command))
            .findFirst()
            .orElse(null);

        if (matchingFacility == null) {
            String exceptionMessage = "Invalid command to execute: " + command;
            LOG.warn(exceptionMessage);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String responseContent = Solution.from(exceptionMessage).asJson();
            response.getWriter().println(responseContent);
            return;
        }

        Solution result = matchingFacility.execute(request);
        response.setStatus(result.getStatusCode());
        response.setContentType(CoreConstants.CONTENT_TYPE_JSON);
        response.getWriter().println(result.asJson());
    }

    /* ---------------
       Utility methods
       --------------- */

    private List<VendorDto> collectVendors() {
        return services
            .stream()
            .filter(AiService::isEnabled)
            .map(service -> new VendorDto(service.getVendorName(), service.getLogo()))
            .collect(Collectors.toList());
    }
    private List<Facility> collectFacilities(Predicate<Facility> filter) {
        return services
            .stream()
            .filter(AiService::isEnabled)
            .flatMap(service -> service.getFacilities().stream())
            .filter(filter)
            .sorted(AiServlet::compareFacilities)
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
