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

class ReplicationContext implements Filter {

    private static final ThreadLocal<Data> HOLDER = new ThreadLocal<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Not implemented
    }

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

    @Override
    public void destroy() {
        // Not implemented
    }

    /* ----------------
       Static accessors
       ---------------- */

    public static List<String> getProperties() {
        return HOLDER.get() != null ? HOLDER.get().properties : null;
    }

    public static void setProperties(List<String> properties) {
        if (HOLDER.get() != null) {
            HOLDER.get().properties = properties;
        }
    }

    /* ----------
       Data model
       ---------- */

    private static class Data {
        private List<String> properties;
    }
}
