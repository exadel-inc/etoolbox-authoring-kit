package com.exadel.aem.toolkit.core.injectors.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import java.util.List;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestAttribute;

@SuppressWarnings("unused")
@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TestModelRequestAttribute {

    @RequestAttribute
    private String param1;
    @RequestAttribute(name = "param1")
    private String namedParam;

    private Object paramObjectType;

    @RequestAttribute(name = "stringsList")
    private List<String> requestAttributeStringList;
    @RequestAttribute(name = "booleanList")
    private List<Boolean> requestAttributeBooleanList;
    @RequestAttribute(name = "integerList")
    private List<Integer> requestAttributeIntegerList;
    @RequestAttribute(name = "longList")
    private List<Long> requestAttributeLongList;

    @RequestAttribute(name = "stringArray")
    private String[] requestAttributeStringArray;
    @RequestAttribute(name = "longArray")
    private Long[] requestAttributeLongArray;
    @RequestAttribute(name = "booleanArray")
    private Boolean[] requestAttributeBooleanArray;
    @RequestAttribute(name = "integerArray")
    private Integer[] requestAttributeIntegerArray;


    public String getParam1() {
        return param1;
    }
    public String getNamedParam() {
        return namedParam;
    }
    public Object getParamObjectType() {
        return paramObjectType;
    }
    public List<String> getRequestAttributeStringList() {
        return requestAttributeStringList;
    }

    public List<Long> getRequestParameterLongList() {
        return requestAttributeLongList;
    }

    public List<Boolean> getRequestAttributeBooleanList() {
        return requestAttributeBooleanList;
    }
    public List<Integer> getRequestAttributeIntegerList() {
        return requestAttributeIntegerList;
    }
    public List<Long> getRequestAttributeLongList() {
        return requestAttributeLongList;
    }
    public String[] getRequestAttributeStringArray() {
        return requestAttributeStringArray;
    }
    public Long[] getRequestAttributeLongArray() {
        return requestAttributeLongArray;
    }
    public Boolean[] getRequestAttributeBooleanArray() {
        return requestAttributeBooleanArray;
    }
    public Integer[] getRequestAttributeIntegerArray() {
        return requestAttributeIntegerArray;
    }
}
