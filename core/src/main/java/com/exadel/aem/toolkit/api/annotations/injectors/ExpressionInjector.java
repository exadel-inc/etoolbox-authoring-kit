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
package com.exadel.aem.toolkit.api.annotations.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.adobe.granite.ui.components.ExpressionHelper;
import com.adobe.granite.ui.components.ExpressionResolver;


@Component(service = {Injector.class},
    property = {Constants.SERVICE_RANKING + ":Integer=" + Integer.MIN_VALUE}
)
public class ExpressionInjector implements Injector {

    public static final String NAME = "expression-injector";

    @Reference
    private ExpressionResolver expressionResolver;

    @Reference(target = "(component.name=org.apache.sling.models.impl.injectors.ValueMapInjector)")
    private Injector injectorValueMap;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object getValue(final Object adaptable, final String name, final Type type,
                           final AnnotatedElement element, final DisposalCallbackRegistry callbackRegistry) {

        Expression annotation = element.getAnnotation(Expression.class);

        if (annotation == null) {
            return null;
        }

        SlingHttpServletRequest request = InjectorUtils.getSlingHttpServletRequest(adaptable);
        if (request == null) {
            return null;
        }

        ValueMap map = InjectorUtils.getValueMap(adaptable);

        if (annotation.name().contains("||") && !Pattern.compile("\\?(.*?)\\:").matcher(annotation.name()).find()) {
            return getObjectFromOrCondition(type, annotation, map);
        } else if (Pattern.compile("\\?(.*?)\\:").matcher(annotation.name()).find()) {
            return getObjectFromTernaryOperator(type, annotation, map, request);
        } else if (Pattern.compile("('(.*?)')(.+\\+)|(.+\\+)('(.*?)')").matcher(annotation.name()).find()) {
            return getObjectFromExpressionWithPrefix(annotation, map);
        } else {
            return getObject(adaptable, name, type, callbackRegistry, annotation);
        }
    }

    /**
     * Splits the annotation name and checks if value exists in ValueMap.
     * If value exists replaces it in the annotation or if value is not found, the prefix will be returned.
     * @param annotation used to get annotation name
     * @param map        ValueMap of current resource from SlingHttpServletRequest.
     * @return String result.
     */
    private String getObjectFromExpressionWithPrefix(Expression annotation, ValueMap map) {
        StringBuilder result = new StringBuilder();
        for (String param : annotation.name().split("[+]")) {
            String value = param.trim();
            if (map.containsKey(value)) {
                value = (String) map.get(value);
            } else if (Pattern.compile("('(.*?)')").matcher(value).find()) {
                value = value.substring(1, value.length() - 1);
            } else {
                value = "";
            }
            result.append(value);
        }
        return result.toString();
    }

    /**
     * Computes the value map value and replace it in annotation name and resolves annotation as an expression.
     * @param type       the declared type of the injection point
     * @param annotation used to get annotation name
     * @param map        ValueMap of current resource from SlingHttpServletRequest
     * @param request    SlingHttpServletRequest instance
     * @return the resolved expression
     */
    private Object getObjectFromTernaryOperator(Type type, Expression annotation, ValueMap map, SlingHttpServletRequest request) {
        ExpressionHelper expressionHelper = new ExpressionHelper(expressionResolver, request);
        return Arrays.stream(annotation.name().split("[<=>!?\\|&:]"))
            .map(String::trim)
            .filter(map::containsKey)
            .map(value -> annotation.name().replace(value, String.format("'%s'", map.get(value, (Class<?>) type))))
            .map(expression -> expressionHelper.get(String.format("${%s}", expression), (Class<?>) type))
            .findAny()
            .orElse(null);
    }

    /**
     * Splits annotation name and tries to inject first value. If value is empty or null
     * tries to inject next value and if no luck injects message from single quotes.
     * @param type       the declared type of the injection point
     * @param annotation used to get annotation name
     * @param map        ValueMap of current resource from SlingHttpServletRequest
     * @return result
     */
    private Object getObjectFromOrCondition(Type type, Expression annotation, ValueMap map) {
        for (String param : annotation.name().split("\\|\\|")) {
            String value = param.trim();
            Optional<CharSequence> charSequence = Optional.ofNullable(map.get(value, (Class<?>) type))
                .map(CharSequence.class::cast)
                .filter(sequence -> sequence.length() != 0);
            if (charSequence.isPresent()) {
                return charSequence.get();
            }
            if (Pattern.compile("('(.*?)')").matcher(value).find()) {
                return value.substring(1, value.length() - 1);
            }
        }
        return null;
    }

    /**
     * Gets the value by calling ValueMapInjector. Gets the property from a ValueMap by name.
     * If name is not set the name is derived from the field name.
     * @param adaptable        object which Sling tries to adapt from
     * @param name             name of the field which has been annotated
     * @param type             the declared type of the injection point
     * @param callbackRegistry a registry object to register a callback object which will be invoked.
     * @param annotation       used to get annotation name
     * @return the value to be injected
     */
    private Object getObject(Object adaptable, String name, Type type, DisposalCallbackRegistry callbackRegistry, Expression annotation) {
        Resource adaptableExpression = InjectorUtils.getResource(adaptable);
        String nameExpression = annotation.name().isEmpty() ? name : annotation.name();
        return injectorValueMap.getValue(adaptableExpression, nameExpression, type, new ExpressionAnnotatedElement(name), callbackRegistry);
    }

    private static class ExpressionAnnotatedElement implements AnnotatedElement {

        private String name;

        public ExpressionAnnotatedElement(String name) {
            this.name = name;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            if (annotationClass.equals(ValueMapValue.class)) {
                return (T) new ValueMapValueImpl(name);
            }
            return null;
        }
    }

    private static class ValueMapValueImpl implements ValueMapValue {

        private final String name;

        public ValueMapValueImpl(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean optional() {
            return false;
        }

        @Override
        public InjectionStrategy injectionStrategy() {
            return InjectionStrategy.DEFAULT;
        }

        @Override
        public String via() {
            return "";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ValueMapValue.class;
        }
    }
}
