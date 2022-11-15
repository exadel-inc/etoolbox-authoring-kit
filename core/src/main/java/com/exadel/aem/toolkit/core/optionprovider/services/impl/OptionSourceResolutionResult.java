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
package com.exadel.aem.toolkit.core.optionprovider.services.impl;

import org.apache.sling.api.resource.Resource;

public class OptionSourceResolutionResult {

    private final Resource dataSource;
    private final PathParameters pathParameters;

    public OptionSourceResolutionResult(Resource dataSource, PathParameters pathParameters) {
        this.dataSource = dataSource;
        this.pathParameters = pathParameters;
    }

    public Resource getDataResource() {
        return dataSource;
    }

    public PathParameters getPathParameters() {
        return pathParameters;
    }
}
