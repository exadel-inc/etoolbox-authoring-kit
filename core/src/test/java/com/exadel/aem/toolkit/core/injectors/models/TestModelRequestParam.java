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
package com.exadel.aem.toolkit.core.injectors.models;

import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestParam;

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@SuppressWarnings("unused")
public class TestModelRequestParam {

    // Valid cases

    @RequestParam
    private String param1;

    @RequestParam(name = "param2")
    private String namedParam;

    @RequestParam(name = "param2")
    private Object paramObjectType;

    @RequestParam(name = "param1")
    private RequestParameter requestParameter;

    @RequestParam(name = "param2")
    private RequestParameter[] requestParameterArray;

    @RequestParam(name = "param2")
    private String[] requestParameterStringArray;

    @RequestParam
    private List<RequestParameter> requestParameterList;

    @RequestParam(name = "param2")
    private List<String> requestParameterStringList;

    @RequestParam
    private RequestParameterMap requestParameterMap;

    private final String paramRequestFromMethodParameter;

    // Invalid cases

    @RequestParam
    private String paramStringWrongName;

    @RequestParam
    private Set<RequestParameter> paramSet;

    @RequestParam(name = "param1")
    private List<Integer> paramListWrongType;

    // Constructor

    @Inject
    public TestModelRequestParam(@RequestParam @Named("param2") String param1) {
        this.paramRequestFromMethodParameter = param1;
    }

    // Accessors - Valid cases

    public String getParam1() {
        return param1;
    }

    public String getNamedParam() {
        return namedParam;
    }

    public Object getParamObjectType() {
        return paramObjectType;
    }

    public RequestParameter getRequestParameter() {
        return requestParameter;
    }

    public RequestParameter[] getRequestParameterArray() {
        return requestParameterArray;
    }

    public String[] getRequestParameterStringArray() {
        return requestParameterStringArray;
    }

    public List<RequestParameter> getRequestParameterList() {
        return requestParameterList;
    }

    public List<String> getRequestParameterStringList() {
        return requestParameterStringList;
    }

    public RequestParameterMap getRequestParameterMap() {
        return requestParameterMap;
    }

    public String getParamRequestFromMethodParameter() {
        return paramRequestFromMethodParameter;
    }

    // Accessors - Invalid cases

    public String getParamStringWrongName() {
        return paramStringWrongName;
    }

    public Set<RequestParameter> getParamSet() {
        return paramSet;
    }

    public List<Integer> getParamListWrongType() {
        return paramListWrongType;
    }
}
