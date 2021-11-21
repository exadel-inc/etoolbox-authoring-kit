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
    private String param;

    @RequestParam(name = "also")
    private String namedParam;

    @RequestParam(name = "also")
    private Object paramObjectType;

    @RequestParam(name = "param")
    private RequestParameter requestParameter;

    @RequestParam(name = "also")
    private RequestParameter[] requestParameterArray;

    @RequestParam
    private List<RequestParameter> requestParameterList;

    @RequestParam
    private RequestParameterMap requestParameterMap;

    private final String paramRequestFromMethodParameter;

    // Invalid cases

    @RequestParam
    private String paramStringWrongName;

    @RequestParam
    private Set<RequestParameter> paramSet;

    @RequestParam(name = "param")
    private List<Integer> paramListWrongType;

    // Constructor

    @Inject
    public TestModelRequestParam(@RequestParam @Named("also") String param) {
        this.paramRequestFromMethodParameter = param;
    }

    // Accessors - Valid cases

    public String getParam() {
        return param;
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

    public List<RequestParameter> getRequestParameterList() {
        return requestParameterList;
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
