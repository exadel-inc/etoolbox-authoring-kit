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
package com.exadel.aem.toolkit.plugin.maven;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;
import com.exadel.aem.toolkit.plugin.exceptions.PluginException;

class MuteableExceptionHandler implements ExceptionHandler {

    private boolean muted = true;

    @Override
    public void handle(String message, Exception cause) {
        if (shouldTerminateOn(cause.getClass())) {
            throw new PluginException(message, cause);
        }
    }

    @Override
    public boolean shouldTerminateOn(Class<? extends Exception> exceptionType) {
        return !muted;
    }

    public void mute() {
        muted = true;
    }

    public void unmute() {
        muted = false;
    }
}
