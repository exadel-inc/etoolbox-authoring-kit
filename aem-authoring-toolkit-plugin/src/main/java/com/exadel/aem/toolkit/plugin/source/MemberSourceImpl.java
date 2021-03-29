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
package com.exadel.aem.toolkit.plugin.source;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.markers._Default;

public abstract class MemberSourceImpl extends SourceImpl implements MemberSource {

    private final Class<?> reportingClass;

    MemberSourceImpl(Class<?> reportingClass) {
        this.reportingClass = reportingClass;
    }

    @Override
    public Class<?> getReportingClass() {
        return this.reportingClass;
    }

    abstract Class<?> getPlainReturnType();

    @SuppressWarnings("deprecation") // Usage of Multifield#field is retained for compatibility and will be removed after v. 2.0.1
    @Override
    public Class<?> getValueType() {
        // Retrieve the "immediate" return type
        Class<?> result = getPlainReturnType();
        // Then switch to directly specified type, if any
        if (getDeclaredAnnotation(MultiField.class) != null
            && getDeclaredAnnotation(MultiField.class).value() != _Default.class) {
            result = getDeclaredAnnotation(MultiField.class).value();
        } else if (getDeclaredAnnotation(MultiField.class) != null
            && getDeclaredAnnotation(MultiField.class).field() != _Default.class) {
            result = getDeclaredAnnotation(MultiField.class).field();
        } else if (getDeclaredAnnotation(FieldSet.class) != null
            && getDeclaredAnnotation(FieldSet.class).value() != _Default.class) {
            result = getDeclaredAnnotation(FieldSet.class).value();
        }
        return result;
    }

}
