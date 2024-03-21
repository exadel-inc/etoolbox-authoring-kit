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
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

class MavenProjectHelper {

    private MavenProjectHelper() {
    }

    static MavenProject getProject(Path source) {
        Model model = getModel(source);
        return new MavenProject(model);
    }

    static Model getModel(Path source) {
        try (InputStream input = source.toUri().toURL().openStream()) {
            return new MavenXpp3Reader().read(input);
        } catch (IOException | XmlPullParserException e) {
            throw new AssertionError("Could not read Maven project model " + source.toAbsolutePath());
        }
    }

}
