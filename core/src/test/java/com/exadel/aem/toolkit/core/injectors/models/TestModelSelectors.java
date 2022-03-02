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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestSelectors;

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@SuppressWarnings("unused")
public class TestModelSelectors {

    // Valid cases

    @RequestSelectors
    private String selectorsString;

    @RequestSelectors
    private Object selectorsObject;

    @RequestSelectors
    private Collection<String> selectorsCollection;

    @RequestSelectors
    private List<String> selectorsList;

    @RequestSelectors
    private String[] selectorsArray;

    private final String selectorsFromParameter;

    // Invalid cases

    @RequestSelectors
    private int selectorsInt;

    @RequestSelectors
    private int[] selectorsArrayInt;

    @RequestSelectors
    private List<Integer> selectorsListInt;

    @RequestSelectors
    private Set<String> selectorsSet;

    // Constructor

    @Inject
    public TestModelSelectors(@RequestSelectors @Named String selectorsString) {
        this.selectorsFromParameter = selectorsString;
    }

    // Accessors - Valid cases

    public String getSelectorsString() {
        return selectorsString;
    }

    public Object getSelectorsObject() {
        return selectorsObject;
    }

    public Collection<String> getSelectorsCollection() {
        return selectorsCollection;
    }

    public List<String> getSelectorsList() {
        return selectorsList;
    }

    public String[] getSelectorsArray() {
        return selectorsArray;
    }

    public String getSelectorsFromParameter() {
        return selectorsFromParameter;
    }

    // Accessors - Invalid cases

    public int getSelectorsInt() {
        return selectorsInt;
    }

    public List<Integer> getSelectorsListInt() {
        return selectorsListInt;
    }

    public int[] getSelectorsArrayInt() {
        return selectorsArrayInt;
    }

    public Set<String> getSelectorsSet() {
        return selectorsSet;
    }
}
