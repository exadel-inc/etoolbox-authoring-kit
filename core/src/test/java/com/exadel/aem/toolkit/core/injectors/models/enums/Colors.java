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
package com.exadel.aem.toolkit.core.injectors.models.enums;

public enum Colors {

    RED("#FF0000"),
    ORANGE("#FFA500"),
    YELLOW("#FFFF00"),
    GREEN("#00FF00"),
    BLUE("#0000FF"),
    INDIGO("#4B0082"),
    VIOLET("#7F00FF");

    public final String hexValue;
    private final int intValue;

    Colors(String hexValue) {
        this.hexValue = hexValue;
        this.intValue = Integer.parseInt(hexValue.substring(1), 16);
    }

    @SuppressWarnings("unused")
    public int getIntValue() {
        return intValue;
    }
}
