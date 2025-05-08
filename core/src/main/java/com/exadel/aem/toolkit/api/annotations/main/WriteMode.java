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
package com.exadel.aem.toolkit.api.annotations.main;

/**
 * Enumerates possible operating modes for the routines that interact with package data
 * @see AemComponent#writeMode()
 */
public enum WriteMode {
    /**
     * If the folder for the current component does not exist, it is created. Otherwise, the behavior is the same as for
     * {@link #OPEN}
     */
    CREATE,

    /**
     * In the folder of the current component, the content is merged with the newly stored content. E.g., if the
     * component wants to store a {@code cq:dialog} node, and the folder already has a {@code _cq_dialog.xml} file, the
     * new one is written, and then the old one is merged into it. If the component folder does not exist, an exception
     * if thrown. This is the default behavior
     */
    MERGE,

    /**
     * The folder of the current component is cleaned up before writing file-wise. E.g., if the component needs to store
     * a {@code cq:dialog} node, and the folder already has a {@code _cq_dialog.xml} file, the file is erased and a
     * new one is written. If the component folder does not exist, an exception if thrown. This is the default behavior
     */
    OPEN
}
