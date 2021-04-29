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
package com.exadel.aem.toolkit.api.annotations.meta;

/**
 * Enumerates the standard scopes that the ToolKit entities can belong to. Technically, each standard scope
 * represents a node in a component's node tree in JCR
 */
public class Scopes {

    public static final String DEFAULT = "";
    public static final String COMPONENT = ".content.xml";
    public static final String CQ_DIALOG = "_cq_dialog.xml";
    public static final String CQ_DESIGN_DIALOG = "_cq_design_dialog.xml";
    public static final String CQ_EDIT_CONFIG = "_cq_editConfig.xml";
    public static final String CQ_CHILD_EDIT_CONFIG = "_cq_childEditConfig.xml";
    public static final String CQ_HTML_TAG = "_cq_htmlTag.xml";

    private Scopes() {
    }
}
