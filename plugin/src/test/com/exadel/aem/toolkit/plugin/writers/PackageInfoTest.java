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
package com.exadel.aem.toolkit.plugin.writers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;
import com.exadel.aem.toolkit.plugin.maven.ThrowsPluginException;

@ThrowsPluginException
public class PackageInfoTest {

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testCreatePackageInfo() {
        // Throws a wrapped FileAlreadyExistsException unless a check for an existing "version.info" file is implemented
        Map<String, String> versionInfo1 = pluginContext.getPackageVersionInfo();
        String timestampString1 = versionInfo1.getOrDefault(TestConstants.PROPERTY_TIMESTAMP, StringUtils.EMPTY);

        Map<String, String> versionInfo2 = pluginContext.getPackageVersionInfo();
        String timestampString2 = versionInfo2.getOrDefault(TestConstants.PROPERTY_TIMESTAMP, StringUtils.EMPTY);

        Assert.assertTrue(StringUtils.isNoneEmpty(timestampString1, timestampString2));
        LocalDateTime timestamp1 = LocalDateTime.parse(timestampString1, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime timestamp2 = LocalDateTime.parse(timestampString2, DateTimeFormatter.ISO_DATE_TIME);
        Assert.assertFalse(timestamp2.isBefore(timestamp1));
    }
}
