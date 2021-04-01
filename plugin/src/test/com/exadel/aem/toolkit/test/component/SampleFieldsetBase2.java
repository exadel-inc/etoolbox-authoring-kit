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
package com.exadel.aem.toolkit.test.component;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;

@SuppressWarnings("unused")
class SampleFieldsetBase2 extends SampleFieldsetAncestor {
    @DialogField(
            name = "First_Field_Primary_Field",
            label = "Label to first primary field",
            description = "Description to first primary field",
            required = true,
            ranking = 1
    )
    @ImageUpload
    private String firstFieldPrimaryField;

    @DialogField(
            name = "Second_Field_Primary_Field",
            label = "Label to second primary field",
            description = "Description to second primary field",
            required = true,
            ranking = 2
    )
    @TextField
    private String secondFieldPrimaryField;

}
