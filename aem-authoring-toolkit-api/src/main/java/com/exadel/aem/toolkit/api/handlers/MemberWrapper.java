/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.exadel.aem.toolkit.api.handlers;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper of {@code Member} class to store additional information.
 * Used by {@link DialogHandler}.
 */
public class MemberWrapper {

    private final Member member;
    private final Map<String, Object> params;

    public MemberWrapper(Member member) {
        this.member = member;
        this.params = new HashMap<>();
        this.params.put("prefix", "./");
        this.params.put("postfix", "");
    }

    /**
     * Method that adds information by key-value
     * @param key Information key
     * @param value Information value
     */
    public void addValue(String key, Object value) {
        params.put(key, value);
    }

    /**
     * Method that returns value of defined key
     * @param key Information key
     */
    public Object getValue(String key) {
        return params.get(key);
    }

    /**
     * Method that returns current {@code Member}
     */
    public Member getMember() {
        return member;
    }
}
