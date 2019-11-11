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
package com.exadel.aem.toolkit.core.util.validation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.rte.Characters;
import com.exadel.aem.toolkit.api.annotations.meta.Validator;

/**
 *  {@link Validator} implementation for testing that provided character range is valid
 */
public class CharactersObjectValidator extends AllNotBlankValidator {
    private static final String MSG_VALID_PARAMS_EXPECTED = "a character range (start < end) or entity definition must be set";
    private static final String METHOD_TO_STRING = "toString";
    private static final String METHOD_ANNOTATION_TYPE = "annotationType";
    private static final String METHOD_RANGE_START = "rangeStart";
    private static final String METHOD_RANGE_END = "rangeEnd";
    private static final String METHOD_NAME = "name";
    private static final String METHOD_ENTITY = "entity";

    /**
     * Tests that the provided character range is valid
     * @param obj Annotation instance
     * @return True or false
     */
    @Override
    public boolean test(Object obj) {
        if (super.test(obj)) {
            return true;
        }
        Characters characters = (Characters)obj;
        return characters.rangeStart() > 0 && characters.rangeEnd() > characters.rangeStart();
    }

    /**
     * Returns whether this object is of {@code Characters} type
     * @param obj Tested value
     * @return True or false
     */
    @Override
    public boolean isApplicableTo(Object obj) {
        return ClassUtils.isAssignable(obj.getClass(), Characters.class);
    }

    /**
     * Filters out {@code Characters} instances with redundant data
     * @param obj {@code Characters} annotation instance
     * @return Filtered value
     */
    @Override
    public Object getFilteredValue(Object obj) {
        if (super.test(obj)) {
            return getCharactersProxyInstance(obj, new String[]{METHOD_NAME, METHOD_ENTITY});
        }
        return getCharactersProxyInstance(obj, new String[]{METHOD_RANGE_START, METHOD_RANGE_END});
    }

    @Override
    public String getWarningMessage() {
        return MSG_VALID_PARAMS_EXPECTED;
    }

    /**
     * Utility method for creating {@code Characters}-compatible instance at runtime
     */
    private static Characters getCharactersProxyInstance(Object source, String[] setFields) {
        return (Characters) Proxy.newProxyInstance(Characters.class.getClassLoader(),
                new Class[]{Characters.class},
                getProxyInvocationHandler(source, setFields)
        );
    }

    /**
     * Utility method to get invocation handler for {@link CharactersObjectValidator#getCharactersProxyInstance(Object, String[])} routine
     */
    private static InvocationHandler getProxyInvocationHandler(Object source, String[] setFields) {
        return (proxy, method, args) -> {
            if (method.getName().equals(METHOD_ANNOTATION_TYPE)) {
                return Characters.class; // for XmlUtil introspection
            }
            if (method.getName().equals(METHOD_TO_STRING)) {
                return Characters.class.getCanonicalName(); // for debugging
            }
            Method baseMethod = ArrayUtils.contains(setFields, method.getName())
                    ? Characters.class.getDeclaredMethod(method.getName())
                    : null;
            if (baseMethod == null) {
                return method.getReturnType().equals(String.class) ? "" : 0L;
            }
            return baseMethod.invoke(source);
        };
    }
}
