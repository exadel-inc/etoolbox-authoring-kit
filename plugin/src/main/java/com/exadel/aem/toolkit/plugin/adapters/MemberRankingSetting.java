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
package com.exadel.aem.toolkit.plugin.adapters;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.handlers.Adapts;
import com.exadel.aem.toolkit.api.handlers.Source;

/**
 * Adapts a {@link DialogField} object to manage the {@code rank} property
 */
@Adapts(Source.class)
public class MemberRankingSetting {

    private int ranking;
    private boolean unset;

    /**
     * Instance constructor per the {@link Adapts} contract
     * @param source {@code Source} object that will be used for extracting resource type
     */
    public MemberRankingSetting(Source source) {
        if (source == null || source.adaptTo(DialogField.class) == null) {
            return;
        }
        int rawRanking = source.adaptTo(DialogField.class).ranking();
        this.unset = rawRanking == Integer.MIN_VALUE;
        this.ranking = unset ? 0 : rawRanking;
    }

    /**
     * Gets whether the rank for the underlying class member was not explicitly set
     * @return True or false
     */
    public boolean isUnset() {
        return unset;
    }

    /**
     * Retrieves the ranking associated with this instance
     * @return Integer value
     */
    public int getRanking() {
        return ranking;
    }

    /**
     * Modifies the ranking associated with this instance
     * @param ranking Integer value
     */
    public void setRanking(int ranking) {
        this.ranking = ranking;
    }
}
