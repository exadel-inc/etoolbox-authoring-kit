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

package com.exadel.aem.toolkit.samples.annotations.handlers;

import com.exadel.aem.toolkit.api.handlers.DialogHandler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.samples.annotations.DialogTitlePostfix;

@Handles(DialogTitlePostfix.class)
public class DialogTitlePostfixHandler implements DialogHandler {

    @Override
    public void accept(Class<?> cls, Target target) {
        DialogTitlePostfix dialogTitlePostfix = cls.getDeclaredAnnotation(DialogTitlePostfix.class);
        String title = target.getAttribute("jcr:title");
        target.attribute("jcr:title", title + dialogTitlePostfix.titlePrefix());
    }
}
