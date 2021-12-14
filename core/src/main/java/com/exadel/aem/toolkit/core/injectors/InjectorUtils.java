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
package com.exadel.aem.toolkit.core.injectors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains common methods for use with the bundled {@code Injector} components
 */
class InjectorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(InjectorUtils.class);

    /**
     * Default (instantiation-restricting) constructor
     */
    private InjectorUtils() {
    }

    /**
     * Retrieves a {@link SlingHttpServletRequest} instance from the provided adaptable object
     * @param adaptable The object which Sling tries to adapt from
     * @return {@code SlingHttpServletRequest} object if adaptable is of an appropriate type, or null
     */
    public static SlingHttpServletRequest getRequest(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return (SlingHttpServletRequest) adaptable;
        }
        return null;
    }

    /**
     * Retrieves a {@link Resource} instance from the provided adaptable object if it is assignable from
     * {@code SlingHttpServletRequest} or {@code Resource}
     * @param adaptable The object which Sling tries to adapt from
     * @return {@code Resource} object if adaptable is of an appropriate type, or null
     */
    public static Resource getResource(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource();
        }
        if (adaptable instanceof Resource) {
            return (Resource) adaptable;
        }
        return null;
    }

    /**
     * Retrieves the {@code ValueMap} instance from the provided adaptable if it is of type {@code SlingHttpServletRequest},
     * or {@code Resource}, or else {@code ValueMap}
     * @param adaptable The object which Sling tries to adapt from
     * @return Data-filled {@code ValueMap} if adaptation was successful. Otherwise, an empty {@code ValueMap} is returned
     */
    public static ValueMap getValueMap(Object adaptable) {
        ValueMap result = null;
        if (adaptable instanceof SlingHttpServletRequest) {
            result = ((SlingHttpServletRequest) adaptable).getResource().getValueMap();
        } else if (adaptable instanceof Resource) {
            result = ((Resource) adaptable).getValueMap();
        } else if (adaptable instanceof ValueMap) {
            result = (ValueMap) adaptable;
        }
        if (result != null) {
            return result;
        }
        return ValueMap.EMPTY;
    }

    /**
     * Retrieves whether the provided {@code Type} of a Java class member is a parametrized collection type and its
     * parameter type matches the list of allowed value types
     * @param value              {@code Type} object
     * @param allowedMemberTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    public static boolean isValidCollection(Type value, Class<?>... allowedMemberTypes) {
        if (!(value instanceof ParameterizedType)
            || !ClassUtils.isAssignable((Class<?>) ((ParameterizedType) value).getRawType(), Collection.class)) {
            return false;
        }
        Class<?> componentType = (Class<?>) ((ParameterizedType) value).getActualTypeArguments()[0];
        return isValidObjectType(componentType, allowedMemberTypes);
    }

    /**
     * Retrieves whether the provided {@code Type} of a Java class member is a parametrized collection type and its
     * parameter type matches the list of allowed value types
     * @param value              {@code Type} object
     * @param allowedMemberTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    public static boolean isValidArray(Type value, Class<?>... allowedMemberTypes) {
        if (!(value instanceof Class<?>) || !((Class<?>) value).isArray()) {
            return false;
        }
        Class<?> componentType = ((Class<?>) value).getComponentType();
        return isValidObjectType(componentType, allowedMemberTypes);
    }

    /**
     * Retrieves whether the provided {@code Type} of a Java class member is eligible for injection
     * @param value        {@code Type} object
     * @param allowedTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    public static boolean isValidObjectType(Type value, Class<?>... allowedTypes) {
        if (!(value instanceof Class<?>)) {
            return false;
        }
        return isValidObjectType((Class<?>) value, allowedTypes);
    }

    /**
     * Retrieves whether the provided {@code Class} representing the type of a Java class member is eligible for injection
     * @param value        {@code Class} object
     * @param allowedTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    private static boolean isValidObjectType(Class<?> value, Class<?>... allowedTypes) {
        if (ArrayUtils.isEmpty(allowedTypes)) {
            return true;
        }
        return Arrays.asList(allowedTypes).contains(value) || value.equals(Object.class);
    }

    /**
     * Retrieves whether the provided {@code Type} of a Java class member is a parametrized collection
     * @param type {@code Type}
     * @return True of false
     */
    public static boolean isTypeCollection(Type type) {
        return type instanceof ParameterizedType
            && ClassUtils.isAssignable((Class<?>) ((ParameterizedType) type).getRawType(), Collection.class);
    }

    /**
     * Retrieves actual type parameter from parameterized type
     * @param parameterizedType {@code Class} that represents a parameterized type
     * @return {@code Class} representing the actual type arguments to this type
     */
    public static Class<?> getActualType(ParameterizedType parameterizedType) {
        return (Class<?>) parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * Creates and initializes a new instance of a class
     * @param instanceClass represent a {@code Class} to be initialized
     * @param <T>           parameterized type
     * @return <T> initialized object instance
     */
    public static <T> T getObjectInstance(Class<? extends T> instanceClass) {
        try {
            return instanceClass.getConstructor().newInstance();
        } catch (InstantiationException
            | IllegalAccessException
            | InvocationTargetException
            | NoSuchMethodException ex) {
            LOG.error("Could not initialize object " + instanceClass.getName(), ex);
        }
        return null;
    }

    /**
     * Retrieves the last parent resource
     * @param currentResource current {@code Resource}
     * @param relativePath    {@code String} relative path
     * @return {@code Resource} object that representing the last node in relative path if success. Otherwise, null is returned
     */
    public static Resource getLastParentResource(Resource currentResource, String relativePath) {
        if (!StringUtils.isNotBlank(relativePath)) {
            return null;
        }
        return currentResource.getChild(StringUtils.substringBeforeLast(prepareRelativePath(relativePath), "/"));
    }

    /**
     * Retrieves the prepared relative path
     * @param path {@code String} current path
     * @return {@code String} that representing prepared path if success. Otherwise, an empty string is returned.
     */
    public static String prepareRelativePath(String path) {
        if (StringUtils.isNotBlank(path)) {
            if (path.startsWith("./")) {
                return path.substring(2);
            }

            if (path.startsWith("/")) {
                return path.substring(1);
            }

            return path;
        }
        return StringUtils.EMPTY;
    }

    /**
     * Retrieves the predicate function that attempts to match the entire region against the pattern.
     * @param relativePath {@code String} relative path
     * @param regex        {@code String} regular expression
     * @return {@code Predicate<Resource>} object that representing predicate function
     */
    public static Predicate<Resource> getPatternPredicate(String relativePath, String regex) {
        String lastNodeName = InjectorUtils.getLastNodeName(relativePath);
        Pattern pattern = Pattern.compile(regex.replace(InjectorConstants.CHILD_INJECTOR_REPLACE_NAME, lastNodeName));
        return resource -> pattern.matcher(resource.getPath()).matches();
    }

    /**
     * Retrieves the last name from a given relative path
     * @param relativePath {@code String} relative path
     * @return {@code String} that representing the last name
     */
    public static String getLastNodeName(String relativePath) {
        String lastNodeName = relativePath;
        if (relativePath.endsWith("/")) {
            lastNodeName = lastNodeName.substring(0, relativePath.length() - 1);
        }
        return lastNodeName.substring(relativePath.lastIndexOf("/") + 1);
    }
}
