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
package com.exadel.aem.toolkit.core;

/**
 * Contains constant values used across the core module
 */
public class CoreConstants {

    public static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    public static final String NN_ITEM = "item";
    public static final String NN_LIST = "list";

    public static final String OPERATOR_EQUALS = "=";

    public static final String PARAMETER_ID = "@id";
    public static final String PARAMETER_NAME = "@name";

    public static final String PN_APPEND = "append";
    public static final String PN_ITEM_RESOURCE_TYPE = "itemResourceType";
    public static final String PN_LIMIT = "limit";
    public static final String PN_LIST_ITEM = "listItem";
    public static final String PN_OFFSET = "offset";
    public static final String PN_OPTIONS = "options";
    public static final String PN_PATH = "path";
    public static final String PN_PREPEND = "prepend";
    public static final String PN_SELECTED = "selected";
    public static final String PN_SIZE = "size";

    public static final String PN_TEXT = "text";
    public static final String PN_TYPE = "type";
    public static final String PN_UPDATE_COMPONENT_LIST = "updatecomponentlist";
    public static final String PN_VALUE = "value";

    public static final String PATH_PARENT = "..";
    public static final String PATH_PARENT_PREFIX = "../";
    public static final String PATH_RELATIVE_PREFIX = "./";
    public static final String PATH_PARENT = "..";

    public static final String ELLIPSIS = "...";

    public static final String SEPARATOR_AT = "@";
    public static final String SEPARATOR_COLON = ":";
    public static final String SEPARATOR_COMMA = ",";
    public static final String SEPARATOR_DOT = ".";
    public static final String SEPARATOR_HYPHEN = "-";
    public static final String SEPARATOR_SLASH = "/";
    public static final String SEPARATOR_UNDERSCORE = "_";

    /**
     * Default (instantiation-restricting) constructor
     */
    private CoreConstants() {
    }
}
