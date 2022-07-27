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
    private String sameOldStringParam;
    @RequestAttribute(name = "sameOldStringParam")
    private String namedParam;


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

    @RequestAttribute(name = "wrappedBooleanArray")
    private Boolean[] requestAttributeBooleanArrayWrapped;
    @RequestAttribute(name = "notWrappedBooleanArray")
    private boolean[] requestAttributeBooleanArrayNotWrapped;

    @RequestAttribute(name = "wrappedIntegerArray")
    private Integer[] requestAttributeIntegerArrayWrapped;
    @RequestAttribute(name = "notWrappedIntegerArray")
    private int[] requestAttributeIntegerArrayNotWrapped;
    @RequestAttribute(name = "wrappedLongArray")
    private Long[] requestAttributeLongArrayWrapped;
    @RequestAttribute(name = "notWrappedLongArray")
    private long[] requestAttributeLongArrayNotWrapped;


    public String getSameOldStringParam() {
        return sameOldStringParam;
    }
    public String getNamedParam() {
        return namedParam;
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
    public Boolean[] getRequestAttributeBooleanArrayWrapped() {
        return requestAttributeBooleanArrayWrapped;
    }
    public boolean[] getRequestAttributeBooleanArrayNotWrapped() {
        return requestAttributeBooleanArrayNotWrapped;
    }
    public Integer[] getRequestAttributeIntegerArrayWrapped() {
        return requestAttributeIntegerArrayWrapped;
    }
    public int[] getRequestAttributeIntegerArrayNotWrapped() {
        return requestAttributeIntegerArrayNotWrapped;
    }
    public Long[] getRequestAttributeLongArrayWrapped() {
        return requestAttributeLongArrayWrapped;
    }
    public long[] getRequestAttributeLongArrayNotWrapped() {
        return requestAttributeLongArrayNotWrapped;
    }
}
