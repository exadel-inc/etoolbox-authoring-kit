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
package com.exadel.aem.toolkit.core.configurator;

/**
 * Contains constant values used across the {@code EToolbox Configurator} module
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class ConfiguratorConstants {

    public static final String ATTR_CONFIGURATOR = "eak.configurator";
    public static final String ATTR_NAME_HINT = "webconsole.configurationFactory.nameHint";

    public static final String NN_DATA = "data";
    public static final String PN_REPLICATION_ACTION = "cq:lastReplicationAction";

    public static final String ROOT_PATH = "/conf/etoolbox/authoring-kit/configurator";
    public static final String RESOURCE_TYPE_CONFIG = "/bin/etoolbox/authoring-kit/config";

    public static final String SUFFIX_BACKUP = "$backup$";

    public static final String VALUE_EMPTY = "__empty__";

    /**
     * Default (instantiation-restricting) constructor
     */
    private ConfiguratorConstants() {
    }
}
