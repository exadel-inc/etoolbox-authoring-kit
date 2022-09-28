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
package com.exadel.aem.toolkit.plugin.handlers.widgets.cases;

import java.util.List;

import static com.exadel.aem.toolkit.plugin.maven.TestConstants.DEFAULT_COMPONENT_NAME;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

@AemComponent(
        path = DEFAULT_COMPONENT_NAME,
        title = "MultiField Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class MultiFieldWidget {
    @DialogField(
            label="Multifield 1",
            description = "Multifield definition with source class specified"
    )
    @MultiField(value = MultiFieldContainer.class, deleteHint = false, typeHint = "typeHint")
    String multiField1;

    @DialogField(
        label="Multifield 2",
        description = "Multifield definition with implicit source class"
    )
    @MultiField(deleteHint = true)
    MultiFieldContainer multiField2;

    @DialogField(
        label="Multifield 3",
        description = "Multifield definition with collection-typed source"
    )
    @MultiField
    List<ExtendedMultiFieldContainer> multiField3;

    @DialogField(
        label="Multifield 4",
        description = "Multifield definition with one source in multiple-source mode"
    )
    @MultiField(forceComposite = true)
    List<MultiFieldContainer> multiField4;

    // Should render the same as Multifield 3 because when there are several rendered fields within a multifield item,
    // the multifield is always "composite", whether we force this property or not
    @DialogField(
        label="Multifield 5",
        description = "Multifield definition with collection-typed source"
    )
    @MultiField(forceComposite = true)
    List<ExtendedMultiFieldContainer> multiField5;

    private static class MultiFieldContainer {
        @DialogField
        @TextField
        String dialogItem;
    }

    private static class ExtendedMultiFieldContainer extends MultiFieldContainer {
        @DialogField
        @FieldSet(value = MultifieldContainerNestedFieldSet.class)
        String nestedFieldSet;
    }

    private static class MultifieldContainerNestedFieldSet {
        @DialogField
        @TextField
        String nestedItem;
    }
}
