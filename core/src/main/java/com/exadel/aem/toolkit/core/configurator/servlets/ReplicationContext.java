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
package com.exadel.aem.toolkit.core.configurator.servlets;

import java.io.IOException;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Provides a thread-local storage for replication-related data during the processing of a replication request.
 */
class ReplicationContext implements Filter {

    private static final ThreadLocal<Data> HOLDER = new ThreadLocal<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Not implemented
    }

    /**
     * Establishes a replication context for the duration of the request processing, then ensures cleanup in a
     * {@code finally} block to prevent memory leaks
     * @param request  The servlet request
     * @param response The servlet response
     * @param chain    The filter chain to continue processing
     * @throws IOException      If an I/O error occurs during filter chain processing
     * @throws ServletException If a servlet error occurs during filter chain processing
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        try {
            HOLDER.set(new Data());
            chain.doFilter(request, response);
        } finally {
            HOLDER.remove();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // Not implemented
    }

    /* ----------------
       Static accessors
       ---------------- */

    /**
     * Retrieves the properties stored in the current thread's replication context
     * @return The list of properties, or {@code null} if no context is established for the current thread
     */
    public static List<String> getProperties() {
        return HOLDER.get() != null ? HOLDER.get().properties : null;
    }

    /**
     * Stores properties in the current thread's replication context. Has no effect if no context is established for the
     * current thread (i.e., outside of a request being processed by this filter)
     * @param properties The list of properties to store
     */
    public static void setProperties(List<String> properties) {
        if (HOLDER.get() != null) {
            HOLDER.get().properties = properties;
        }
    }

    /* ----------
       Data model
       ---------- */

    /**
     * Holds replication-related data for a single request.
     */
    private static class Data {
        private List<String> properties;
    }
}
