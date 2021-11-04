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
package com.exadel.aem.toolkit.api.annotations.injectors.models;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestSelectors;

@Model(adaptables = {SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TestModelSelectors {

    @RequestSelectors
    private List<String> selectorsList;

    @RequestSelectors
    private Collection<String> selectorsCollection;

    @RequestSelectors
    private Set<String> selectorsSet;

    @RequestSelectors
    private List<Integer> selectorsListInt;

    @RequestSelectors
    private List<TestModelSelectors> selectorsListModel;

    @RequestSelectors
    private String[] selectorsArrayString;

    @RequestSelectors
    private int[] selectorsArrayInt;

    @RequestSelectors
    private String selectorsString;

    @RequestSelectors
    private int selectorsInt;

    @RequestSelectors
    private TestModelSelectors selectorsTestModel;

    public Collection<String> getSelectorsCollection() {
        return selectorsCollection;
    }

    public List<Integer> getSelectorsListInt() {
        return selectorsListInt;
    }

    public List<TestModelSelectors> getSelectorsListModel() {
        return selectorsListModel;
    }

    public int[] getSelectorsArrayInt() {
        return selectorsArrayInt;
    }

    public List<String> getSelectorsList() {
        return selectorsList;
    }

    public Set<String> getSelectorsSet() {
        return selectorsSet;
    }

    public String[] getSelectorsArrayString() {
        return selectorsArrayString;
    }

    public String getSelectorsString() {
        return selectorsString;
    }

    public int getSelectorsInt() {
        return selectorsInt;
    }

    public TestModelSelectors getSelectorsTestModel() {
        return selectorsTestModel;
    }
}
