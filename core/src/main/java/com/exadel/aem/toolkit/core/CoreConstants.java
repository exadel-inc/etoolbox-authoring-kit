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
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class CoreConstants {

    public static final String ROOT_PACKAGE = "com.exadel.aem.toolkit";

    public static final String NN_DATASOURCE = "datasource";
    public static final String NN_GRANITE_DATA = "granite:data";
    public static final String NN_ITEM = "item";
    public static final String NN_LIST = "list";

    public static final String PN_APPEND = "append";
    public static final String PN_CHECKED = "checked";
    public static final String PN_DEFAULT_VALUE = "defaultValue";
    public static final String PN_ITEM_RESOURCE_TYPE = "itemResourceType";
    public static final String PN_LIMIT = "limit";
    public static final String PN_LIST_ITEM = "listItem";
    public static final String PN_NAME = "name";
    public static final String PN_OFFSET = "offset";
    public static final String PN_PATH = "path";
    public static final String PN_PREPEND = "prepend";
    public static final String PN_SELECTED = "selected";
    public static final String PN_TEXT = "text";
    public static final String PN_TITLE = "title";
    public static final String PN_UPDATE_COMPONENT_LIST = "updatecomponentlist";
    public static final String PN_VALUE = "value";

    public static final String SEPARATOR_AT = "@";
    public static final String SEPARATOR_DOT = ".";
    public static final String SEPARATOR_COLON = ":";
    public static final String SEPARATOR_COMMA = ",";
    public static final String SEPARATOR_HYPHEN = "-";
    public static final String SEPARATOR_SLASH = "/";
    public static final String SEPARATOR_UNDERSCORE = "_";

    public static final String RELATIVE_PATH_PREFIX = "./";
    public static final String SELF_PATH = SEPARATOR_DOT;
    public static final String PARENT_PATH = "..";

    public static final String ARRAY_OPENING = "[";
    public static final String ARRAY_CLOSING = "]";
    public static final String EQUALITY_SIGN = "=";
    public static final String WILDCARD = "*";

    public static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";

    /**
     * Default (instantiation-restricting) constructor
     */
    private CoreConstants() {
    }
}
