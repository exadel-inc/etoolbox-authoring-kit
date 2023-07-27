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
package com.exadel.aem.toolkit.plugin.metadata.scripting;

import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.exceptions.ScriptingException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.metadata.Metadata;
import com.exadel.aem.toolkit.plugin.metadata.Property;
import com.exadel.aem.toolkit.plugin.sources.ModifiableMemberSource;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Contains utility methods for interpolating inline scripting templates in ToolKit's API members, such as annotations
 */
public class ScriptingHelper {

    private static final ContextFactory CONTEXT_FACTORY = new ContextFactory();
    private static final ClassShutter DEFAULT_CLASS_SHUTTER = className ->
        StringUtils.startsWith(className, CoreConstants.ROOT_PACKAGE);

    private static final String PN_DATA = "data";
    private static final String PN_SOURCE = "source";

    private static final String PATH_SCRIPT = "<script>";

    private static final String TEMPLATE_START = "$";
    private static final String TEMPLATE_STRIPPED_SYMBOLS = "@${} ";

    private static final String TOKEN_THIS = "this";

    /**
     * Default (instantiation-restricted) constructor
     */
    private ScriptingHelper() {
    }

    /**
     * Replaces inline scripting templates in the provided {@link Metadata} instance with corresponding computed values
     * using the provided {@link Source} as the context
     * @param value  {@code Metadata} instance to process
     * @param source {@code Source} instance to be used as the data context
     */
    public static synchronized void interpolate(Metadata value, Source source) {
        if (value == null) {
            return;
        }
        List<TemplatedProperty> templatedProperties = value.stream(true, true)
            .map(TemplatedProperty::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (templatedProperties.isEmpty()) {
            return;
        }

        AbstractAdapter adapter = getAdapter(source);
        if (adapter == null) {
            return;
        }

        DataStack dataStack = source.adaptTo(DataStack.class);

        try (Context context = CONTEXT_FACTORY.enterContext()) {
            context.setLanguageVersion(Context.VERSION_ES6);
            context.setClassShutter(DEFAULT_CLASS_SHUTTER);
            Scriptable scope = context.initStandardObjects();
            ScriptableObject.putProperty(scope, PN_SOURCE, Context.javaToJS(adapter, scope));
            ScriptableObject.putProperty(scope, PN_DATA, Context.javaToJS(new MapAdapter(dataStack.getData()), scope));
            for (TemplatedProperty property : templatedProperties) {
                String result = interpolate(property, context, scope, dataStack);
                value.putValue(property.getPath(), result);
            }
        } catch (IllegalStateException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ScriptingException(e));
        }
    }

    /**
     * Replaces inline scripting templates in the provided {@link TemplatedProperty} with computed values. The action is
     * done with {@code Rhino engine}'s context and scope. The result is assembled back into the string property value
     * @param templatedProperty The {@code TemplatedProperty} to process
     * @param context           {@link Context} instance used for the script evaluation
     * @param scope             {@link Scriptable} instance used for the script evaluation
     * @param dataStack         {@link DataStack} instance that represents user-set values that are considered while
     *                          interpolating the property value
     * @return A non-null string value
     */
    private static String interpolate(
        TemplatedProperty templatedProperty,
        Context context,
        Scriptable scope,
        DataStack dataStack) {

        String result = templatedProperty.getValue();
        while (!templatedProperty.getEmbeddings().isEmpty()) {
            StringBuilder resultBuilder = new StringBuilder(templatedProperty.getValue());
            LinkedList<Embedding> embeddings = (LinkedList<Embedding>) templatedProperty.getEmbeddings();
            Iterator<Embedding> embeddingIterator = embeddings.descendingIterator();
            while (embeddingIterator.hasNext()) {
                Embedding embedding = embeddingIterator.next();
                List<String> variables = embedding.getVariables();
                for (String variable : variables) {
                    if (TOKEN_THIS.equals(variable)) {
                        continue;
                    }
                    Object value = dataStack.getData().get(variable);
                    ScriptableObject.putProperty(
                        scope,
                        variable,
                        value != null ? Context.javaToJS(value, scope) : Undefined.instance);
                }
                String scriptResult = runScript(context, scope, embedding.getScript());
                resultBuilder.replace(embedding.getStart(), embedding.getEnd(), scriptResult);
            }
            result = resultBuilder.toString();
            templatedProperty.reset(result);
        }
        return result;
    }

    /**
     * Retrieves the {@link AbstractAdapter} instance for the provided {@link Source}. The {@code adapter} instance is
     * used to conveniently access properties of the script-processable object from within the script logic
     * @param source {@code Source} instance to process
     * @return {@code AbstractAdapter} instance, or {@code null} if the provided {@code Source} is not manageable by the
     * scripting engine
     * @see AbstractAdapter
     */
    private static AbstractAdapter getAdapter(Source source) {
        if (source.adaptTo(Member.class) != null) {
            Member reflectedMember = source.adaptTo(Member.class);
            Member reflectedUpstreamMember = source
                .tryAdaptTo(ModifiableMemberSource.class)
                .map(ModifiableMemberSource::getUpstreamMember)
                .orElse(null);
            return new MemberAdapter(reflectedMember, reflectedUpstreamMember, null);
        } else if (source.adaptTo(Class.class) != null) {
            return new ClassAdapter(source.adaptTo(Class.class));
        }
        return null;
    }

    /**
     * Evaluates the provided {@code JavaScript}-coded string using the provided {@code Context} and scope
     * @param context {@link Context} instance used for the script evaluation
     * @param scope   {@link Scriptable} instance used for the script evaluation
     * @param script  The script to evaluate
     * @return A non-null string value representing the result of the evaluation
     */
    private static String runScript(Context context, Scriptable scope, String script) {
        try {
            Object result = context.evaluateString(scope, script, PATH_SCRIPT, 0, null);
            result = Context.jsToJava(result, String.class);
            return result != null && !Undefined.SCRIPTABLE_UNDEFINED.toString().equals(result.toString())
                ? result.toString()
                : StringUtils.EMPTY;
        } catch (RhinoException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ScriptingException(e));
        }
        return StringUtils.EMPTY;
    }

    /* ---------------
       Utility classes
       --------------- */

    /**
     * Represents a property object that can be derived from a common {@link Property} instance and encapsulates a
     * particular property path, value, and the sequence of scripting templates found in the value
     */
    private static class TemplatedProperty {
        private String path;
        private String value;
        private LinkedList<Embedding> embeddings;

        /**
         * Retrieves the property path
         * @return A non-blank string value
         */
        public String getPath() {
            return path;
        }

        /**
         * Retrieves the property value
         * @return A non-blank string value
         */
        public String getValue() {
            return value;
        }

        /**
         * Retrieves the sequence of {@link Embedding} objects that represent inline templates found in the property
         * value
         * @return {@code List} instance
         */
        public List<Embedding> getEmbeddings() {
            return embeddings;
        }

        /**
         * Extracts the sequence of {@link Embedding} objects from the provided property value and stores into the
         * current instance
         * @param content The property value to process; a non-blank string is expected
         */
        public void reset(String content) {
            value = content;
            embeddings = new LinkedList<>();
            SubstringMatcher substringMatcher = new SubstringMatcher(
                value,
                DialogConstants.OPENING_CURLY,
                DialogConstants.CLOSING_CURLY,
                Arrays.asList(TEMPLATE_START, CoreConstants.SEPARATOR_AT));
            SubstringMatcher.Substring substring = substringMatcher.next();
            while (substring != null) {
                Embedding embedding = new Embedding(substring);
                if (embedding.isJavaScript()) {
                    embeddings.add(embedding);
                }
                substring = substringMatcher.next();
            }
        }

        /**
         * Creates a new {@code TemplatedProperty} instance from the provided {@link Property} object
         * @param original The {@code Property} to process
         * @return {@code TemplatedProperty} instance, or {@code null} if the provided {@code Property} does not contain
         * a string value or it does not contain any inline templates
         */
        public static TemplatedProperty from(Property original) {
            if (!String.class.equals(original.getType())) {
                return null;
            }
            Object value = original.getValue();
            String stringValue = value != null ? value.toString() : null;
            boolean hasAtTemplate = stringValue != null
                && stringValue.contains(CoreConstants.SEPARATOR_AT + DialogConstants.OPENING_CURLY);
            boolean hasDollarSignTemplate = stringValue != null
                && stringValue.contains(TEMPLATE_START + DialogConstants.OPENING_CURLY);
            if (!hasAtTemplate && !hasDollarSignTemplate) {
                return null;
            }
            TemplatedProperty result = new TemplatedProperty();
            result.path = original.getPath();
            result.reset(stringValue);
            return !result.getEmbeddings().isEmpty() ? result : null;
        }
    }

    /**
     * Represents a substring within a {@link Property} value that represents an inline scripting template
     * @see SubstringMatcher
     * @see TemplatedProperty
     */
    private static class Embedding {
        private final SubstringMatcher.Substring substring;
        private final LinkedList<SubstringMatcher.Substring> varTokens;

        /**
         * Initializes a new {@code Embedding} instance from the provided {@link SubstringMatcher.Substring} object
         * @param substring The {@code Substring} to process
         */
        Embedding(SubstringMatcher.Substring substring) {
            this.substring = substring;
            this.varTokens = findVariableTokens(substring.getContent());
        }

        /**
         * Retrieves the start index of the inline script within the property value
         * @return Integer value
         */
        public int getStart() {
            return substring.getStart();
        }

        /**
         * Retrieves the end index of the inline script within the property value
         * @return Integer value
         */
        public int getEnd() {
            return substring.getEnd();
        }

        /**
         * Determines if the inline script is a JavaScript expression. If {@code false} is returned, a
         * {@code Granite EL} expression is implied
         * @return True or false
         */
        public boolean isJavaScript() {
            return substring.getContent().startsWith(CoreConstants.SEPARATOR_AT)
                || !varTokens.isEmpty();
        }

        /**
         * Retrieves the inline script content
         * @return A non-blank string value
         */
        public String getScript() {
            String result = substring.getContent();
            if (!varTokens.isEmpty()) {
                StringBuilder resultBuilder = new StringBuilder(result);
                // DescendingIterator is to keep in-string indexes as they are for an unchanged part of the string
                varTokens.descendingIterator().forEachRemaining(token ->
                    resultBuilder.replace(token.getStart(), token.getEnd(), token.getContent().substring(1)));
                result = resultBuilder.toString();
            }
            return StringUtils.strip(
                result.replace(TOKEN_THIS, PN_SOURCE),
                TEMPLATE_STRIPPED_SYMBOLS);
        }

        /**
         * Retrieves the list of variable names found in the inline script
         * @return A non-null {@code List} instance; can be empty
         */
        public List<String> getVariables() {
            return varTokens
                .stream()
                .map(token -> token.getContent().substring(1))
                .collect(Collectors.toList());
        }

        /**
         * Retrieves the list of {@link SubstringMatcher.Substring} objects that represent variable tokens found in the
         * inline script
         * @param expression The inline script to process
         * @return A non-null {@code List} instance
         */
        private static LinkedList<SubstringMatcher.Substring> findVariableTokens(String expression) {
            LinkedList<SubstringMatcher.Substring> result = new LinkedList<>();
            SubstringMatcher substringMatcher = new SubstringMatcher(expression, CoreConstants.SEPARATOR_AT);
            SubstringMatcher.Substring newSubstring = substringMatcher.next();
            while (newSubstring != null) {
                result.add(newSubstring);
                newSubstring = substringMatcher.next();
            }
            return result;
        }
    }
}
