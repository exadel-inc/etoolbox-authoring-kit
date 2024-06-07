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
package com.exadel.aem.toolkit.plugin.sources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Streams;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.WriteMode;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Implements {@link Source} to expose the metadata that is specific for the Java class representing an AEM component
 */
class ComponentSourceImpl extends ClassSourceImpl implements ComponentSource {

    private static final String ROOT_DIRECTORY = "jcr_root";

    private final String componentPath;

    private List<Source> extraViews;

    /**
     * Initializes a class instance storing a reference to the {@code Class} that serves as the metadata source
     * @param value The metadata source
     */
    ComponentSourceImpl(Class<?> value) {
        super(value);
        componentPath = preparePath();
    }

    /* -----------------------
       ComponentSource members
       ----------------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return componentPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WriteMode getWriteMode() {
        return tryAdaptTo(AemComponent.class)
            .map(AemComponent::writeMode)
            .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Source> getViews() {
        Class<?>[] referencedViews = tryAdaptTo(AemComponent.class)
            .map(AemComponent::views)
            .orElse(ArrayUtils.EMPTY_CLASS_ARRAY);
        return Streams
            .concat(Stream.of((Source) this), Arrays.stream(referencedViews).map(Sources::fromClass))
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(Source view) {
        if (!(view instanceof ClassSourceImpl) || view instanceof ComponentSourceImpl) {
            return;
        }
        if (extraViews == null) {
            extraViews = new ArrayList<>();
        }
        extraViews.add(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(String path) {
        if (StringUtils.startsWith(path, ROOT_DIRECTORY)) {
            return StringUtils.equals(this.componentPath, path);
        }
        if (StringUtils.startsWith(path, CoreConstants.SEPARATOR_SLASH)) {
            return StringUtils.equals(this.componentPath, ROOT_DIRECTORY + path);
        }
        return StringUtils.endsWith(this.componentPath, path);
    }

    /**
     * Completes, if necessary, the path specified for the current AEM component so that it represents an absolute
     * repository path
     * @return A nullable string value
     */
    private String preparePath() {
        String pathByComponent = tryAdaptTo(AemComponent.class)
            .map(AemComponent::path)
            .orElse(null);
        @SuppressWarnings("deprecation") // "name" processing is for compatibility; will be removed in a version after 2.0.2
        String pathByDialog = tryAdaptTo(Dialog.class)
            .map(Dialog::name)
            .orElse(null);
        String effectivePath = Stream.of(pathByComponent, pathByDialog)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(pathByDialog);

        if (StringUtils.isBlank(effectivePath)) {
            return null;
        } else if (StringUtils.startsWith(effectivePath, ROOT_DIRECTORY)) {
            return effectivePath;
        } else if (effectivePath.startsWith(CoreConstants.SEPARATOR_SLASH + ROOT_DIRECTORY)) {
            return effectivePath.substring(1);
        } else if (effectivePath.startsWith(CoreConstants.SEPARATOR_SLASH)) {
            return ROOT_DIRECTORY + effectivePath;
        }

        String result = StringUtils.stripEnd(
            PluginRuntime.context().getSettings().getPathBase(adaptTo(Class.class)),
            CoreConstants.SEPARATOR_SLASH)
            + CoreConstants.SEPARATOR_SLASH
            + effectivePath;
        return StringUtils.strip(result, CoreConstants.SEPARATOR_SLASH);
    }

    /* --------------
       Source members
       -------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T adaptTo(Class<T> type) {
        T result = super.adaptTo(type);
        if (result != null || extraViews == null) {
            return result;
        }
        for (Source extraView : extraViews) {
            result = extraView.adaptTo(type);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
