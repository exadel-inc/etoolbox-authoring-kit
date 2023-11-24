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
package com.exadel.aem.toolkit.regression;

import java.io.IOException;
import java.io.Writer;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LogWriter extends Writer {

    private final static Logger LOG = LoggerFactory.getLogger(RegressionTest.class);

    private static final String PREFIX_ERROR = "[ERROR]";
    private static final String PREFIX_INFO = "[INFO]";

    private final boolean isErr;

    public LogWriter(boolean isErr) {
        this.isErr = isErr;
    }

    @Override
    public void write(@Nonnull char[] buffer, int offset, int length) {
        String content = new String(buffer, offset, length).trim();
        boolean shouldLogError = isErr;
        if (StringUtils.startsWith(content, PREFIX_ERROR)) {
            content = StringUtils.replace(content, PREFIX_ERROR, StringUtils.EMPTY).trim();
            shouldLogError = true;
        } else if (StringUtils.startsWith(content, PREFIX_INFO)) {
            content = StringUtils.replace(content, PREFIX_INFO, StringUtils.EMPTY).trim();
            shouldLogError = false;
        }
        if (StringUtils.isEmpty(content)) {
            return;
        }
        if (shouldLogError) {
            LOG.error(content);
        } else {
            LOG.info(content);
        }
    }

    @Override
    public void flush() throws IOException {
        // No operation
    }

    @Override
    public void close() throws IOException {
        // No operation
    }
}
