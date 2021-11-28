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
package com.exadel.aem.toolkit.plugin.adapters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.Column;
import com.exadel.aem.toolkit.api.annotations.layouts.FixedColumns;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.handlers.Adapts;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.handlers.containers.AccordionPanelFacade;
import com.exadel.aem.toolkit.plugin.handlers.containers.ColumnFacade;
import com.exadel.aem.toolkit.plugin.handlers.containers.Section;
import com.exadel.aem.toolkit.plugin.handlers.containers.TabFacade;

/**
 * Adapts a {@link Source} object that is considered to be a Java class member marked with any of the multi-section
 * container-type widget annotations, such as {@link com.exadel.aem.toolkit.api.annotations.layouts.Accordion} or {@link
 * com.exadel.aem.toolkit.api.annotations.layouts.Tabs}, to retrieve information on the declared container sections
 */
@Adapts(Source.class)
public class ContainerWidgetSetting {

    private final Source source;

    /**
     * Instance constructor per the {@link Adapts} contract
     * @param source {@code Source} object that will be used for extracting data
     */
    public ContainerWidgetSetting(Source source) {
        this.source = source;
    }

    /**
     * Retrieves whether the current source represents a Java class member annotated with any of the multi-section
     * container-type widget annotations
     * @return True or false
     */
    public boolean isPresent() {
        return source != null
            && Stream.of(Accordion.class, FixedColumns.class, Column.class, Tabs.class)
            .anyMatch(annotationType -> source.tryAdaptTo(annotationType).isPresent());
    }

    /**
     * Retrieves container sections declared by the current source
     * @return Collection of {@link Section} objects
     */
    public List<Section> getSections() {
        if (source == null) {
            return Collections.emptyList();
        }
        if (source.tryAdaptTo(Tabs.class).isPresent()) {
            return Arrays
                .stream(source.adaptTo(Tabs.class).value())
                .map(tab -> new TabFacade(tab, false))
                .collect(Collectors.toList());

        } else if (source.tryAdaptTo(Accordion.class).isPresent()) {
            return Arrays
                .stream(source.adaptTo(Accordion.class).value())
                .map(accordionPanel -> new AccordionPanelFacade(accordionPanel, false))
                .collect(Collectors.toList());

        } else if (source.tryAdaptTo(FixedColumns.class).isPresent()) {
            return Arrays
                .stream(source.adaptTo(FixedColumns.class).value())
                .map(ColumnFacade::new)
                .collect(Collectors.toList());

        } else if (source.tryAdaptTo(Column.class).isPresent()) {
            return Collections.singletonList(new ColumnFacade(source.adaptTo(Column.class)));
        }
        return Collections.emptyList();
    }
}
