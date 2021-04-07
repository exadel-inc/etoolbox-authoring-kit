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
package com.exadel.aem.toolkit.api.runtime;

import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.handlers.Source;

/**
 * An abstraction of the ToolKit Maven plugin runtime context. Provides access to {@link XmlUtility}, and
 * {@link ExceptionHandler}
 * @deprecated Since v. 2.0.2 users are encouraged to use the new custom handlers API that is based
 * on {@link Source} and {@link Target} objects handling. Legacy API will be revoked in the versions to come
 */
@Deprecated
@SuppressWarnings("squid:S1133")
public interface RuntimeContext {

    /**
     * Provides the reference to the active {@link ExceptionHandler} instance
     * @return {@code ExceptionHandler} initialized for this context
     */
    ExceptionHandler getExceptionHandler();

    /**
     * Provides the reference to the active {@link XmlUtility} instance
     * @return {@code XmlUtility} initialized for this context
     */
    XmlUtility getXmlUtility();
}
