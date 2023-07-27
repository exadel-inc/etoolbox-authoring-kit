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
package com.exadel.aem.toolkit.plugin.metadata;

/**
 * Represents a result of a method invocation as performed by a {@link InterfaceHandler}
 * @see InterfaceHandler
 */
class InvocationResult {
    public static final InvocationResult NOT_DONE = new InvocationResult(false, null);

    private final boolean done;
    private final Object result;

    /**
     * Initializes a class instance with a flag specializing if the invocation took place
     * @param done   The flag specifying whether the invocation succeeded
     * @param result The invocation result
     */
    private InvocationResult(boolean done, Object result) {
        this.done = done;
        this.result = result;
    }

    /**
     * Determines whether the invocation was successful
     * @return True or false
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Retrieves the invocation result
     * @return An arbitrary nullable value
     */
    public Object getResult() {
        return result;
    }

    /**
     * Creates a new {@code InvocationResult} instance with the specified result
     * @param result The invocation result
     * @return {@link InvocationResult} instance
     */
    public static InvocationResult done(Object result) {
        return new InvocationResult(true, result);
    }
}
