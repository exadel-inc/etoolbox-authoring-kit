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

package com.exadel.aem.toolkit.core.exceptions.handlers;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;

/**
 * Represents an abstraction of a "permissive" or "selective", or "strict" {@link ExceptionHandler} that can log
 * exception messages
 */
abstract class AbstractExceptionHandler implements ExceptionHandler {
    @Override
    public void handle(Exception e) {
        handle(e.getMessage(), e);
    }
}
