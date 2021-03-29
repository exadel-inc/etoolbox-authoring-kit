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
import com.exadel.aem.toolkit.api.handlers.Adaptable;
import com.exadel.aem.toolkit.api.handlers.Source;

@Adaptable(Source.class)
public class MemberRankingSetting {

    private int ranking;
    private boolean unset;

    public MemberRankingSetting(Source source) {
        if (source == null || source.adaptTo(DialogField.class) == null) {
            return;
        }
        int rawRanking = source.adaptTo(DialogField.class).ranking();
        this.unset = rawRanking == Integer.MIN_VALUE;
        this.ranking = unset ? 0 : rawRanking;
    }

    public boolean isUnset() {
        return unset;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }
}
