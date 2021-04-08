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
package com.exadel.aem.toolkit.api.annotations.editconfig.listener;

/**
 * Contains string constants used to populate the {@code cq:EditListenersConfig} node of Granite UI component setup
 */
@SuppressWarnings("unused")
public class ListenerConstants {
    public static final String ACTION_REFRESH_SELF = "REFRESH_SELF";
    public static final String ACTION_REFRESH_PAGE = "REFRESH_PAGE";
    public static final String ACTION_REFRESH_PARENT = "REFRESH_PARENT";

    public static final String EVENT_BEFORE_CHILD_INSERT = "beforechildinsert";
    public static final String EVENT_BEFORE_COPY = "beforecopy";
    public static final String EVENT_BEFORE_EDIT = "beforeedit";
    public static final String EVENT_BEFORE_DELETE = "beforedelete";
    public static final String EVENT_BEFORE_INSERT = "beforeinsert";
    public static final String EVENT_BEFORE_MOVE = "beforemove";

    public static final String EVENT_AFTER_CHILD_INSERT = "afterchildinsert";
    public static final String EVENT_AFTER_COPY = "aftercopy";
    public static final String EVENT_AFTER_DELETE = "afterdelete";
    public static final String EVENT_AFTER_EDIT = "afteredit";
    public static final String EVENT_AFTER_INSERT = "afterinsert";
    public static final String EVENT_AFTER_MOVE = "aftermove";

    private ListenerConstants() {}
}
