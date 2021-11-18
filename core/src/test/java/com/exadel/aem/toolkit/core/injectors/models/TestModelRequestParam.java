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

import com.exadel.aem.toolkit.core.injectors.annotations.RequestParam;

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TestModelRequestParam {

    @RequestParam
    private String paramName;

    @RequestParam(name = "also")
    private String paramNameFromAnnotationName;

    @RequestParam(name = "also")
    private Object paramObjectType;

    @RequestParam(name = "also")
    private RequestParameter paramRequestParamType;

    @RequestParam(name = "also")
    private RequestParameter[] paramRequestParamArray;

    @RequestParam
    private RequestParameterMap paramRequestParameterMapType;

    @RequestParam
    private List<RequestParameter> paramList;

    @RequestParam
    private List<String> paramListString;

    @RequestParam
    private Set<RequestParameter> paramSet;

    @RequestParam(name = "also")
    private String[] paramRequestParamArrayString;

    private final String paramRequestFromMethodParameter;

    @Inject
    public TestModelRequestParam(@RequestParam @Named("also") String paramName) {
        this.paramRequestFromMethodParameter = paramName;
    }

    public String getParamRequestFromMethodParameter() {
        return paramRequestFromMethodParameter;
    }

    public List<String> getParamListString() {
        return paramListString;
    }

    public Set<RequestParameter> getParamSet() {
        return paramSet;
    }

    public String[] getParamRequestParamArrayString() {
        return paramRequestParamArrayString;
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamNameFromAnnotationName() {
        return paramNameFromAnnotationName;
    }

    public RequestParameter getParamRequestParamType() {
        return paramRequestParamType;
    }

    public Object getParamObjectType() {
        return paramObjectType;
    }

    public RequestParameterMap getParamRequestParameterMapType() {
        return paramRequestParameterMapType;
    }

    public RequestParameter[] getParamRequestParamArray() {
        return paramRequestParamArray;
    }

    public List<RequestParameter> getParamList() {
        return paramList;
    }
}
