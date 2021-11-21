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
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestSuffix;

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@SuppressWarnings("unused")
public class TestModelSuffix {

    // Valid cases

    @RequestSuffix
    private String suffix;

    @RequestSuffix
    private Object suffixObject;

    @RequestSuffix
    private Resource suffixResource;

    private final String suffixFromParameter;

    // Invalid cases

    @RequestSuffix
    private int suffixInt;

    @RequestSuffix
    private List<String> suffixList;

    // Constructor

    @Inject
    public TestModelSuffix(@RequestSuffix @Named("suffixParam") String suffix) {
        this.suffixFromParameter = suffix;
    }

    // Accessors - Valid cases

    public String getSuffix() {
        return suffix;
    }

    public Object getSuffixObject() {
        return suffixObject;
    }

    public Resource getSuffixResource() {
        return suffixResource;
    }

    public String getSuffixFromParameter() {
        return suffixFromParameter;
    }

    // Accessors - Invalid cases

    public int getSuffixInt() {
        return suffixInt;
    }

    public List<String> getSuffixList() {
        return suffixList;
    }
}
