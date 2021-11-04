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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.Expression;

@Model(adaptables = {SlingHttpServletRequest.class},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TestModelExpression {

    @Expression
    private String foo;

    @Expression(name = "bar")
    private String valueFromBar;

    @Expression(name = "emptyFoo || bar")
    private String fooWithOr;

    @Expression(name = "emptyFoo || nullBar || 'Default Value'")
    private String fooWithOrAndDefaultValue;

    @Expression(name = "foo != bar ? foo : 'not true'")
    private String fooTernary;

    @Expression(name = "fooIntValue != 10 ? fooIntValue : 42")
    private int fooTernaryIntValue;

    @Expression(name = "'My: ' + foo")
    private String fooWithPrefix;

    public String getFoo() {
        return foo;
    }

    public String getValueFromBar() {
        return valueFromBar;
    }

    public String getFooWithOr() {
        return fooWithOr;
    }

    public String getFooWithOrAndDefaultValue() {
        return fooWithOrAndDefaultValue;
    }

    public String getFooTernary() {
        return fooTernary;
    }

    public int getFooTernaryIntValue() {
        return fooTernaryIntValue;
    }

    public String getFooWithPrefix() {
        return fooWithPrefix;
    }

}
