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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

public class ScriptingHelper {

    private static final ContextFactory CONTEXT_FACTORY = new ContextFactory();
    private static final ClassShutter DEFAULT_CLASS_SHUTTER = className ->
        StringUtils.startsWith(className, CoreConstants.ROOT_PACKAGE);

    private static final String PN_DATA = "data";
    private static final String PN_SOURCE = "source";

    private static final String PATH_SCRIPT = "<script>";

    private static final Pattern SCRIPT_TEMPLATE = Pattern.compile("@\\{([^}]*?)}");

    private ScriptingHelper() {
    }

    public static synchronized void interpolate(Metadata value, Source source) {
        if (value == null) {
            return;
        }
        List<Property> templatedProperties = value.stream(true, true)
            .filter(ScriptingHelper::containsTemplate)
            .collect(Collectors.toList());

        if (templatedProperties.isEmpty()) {
            return;
        }

        try (Context context = CONTEXT_FACTORY.enterContext()) {
            context.setLanguageVersion(Context.VERSION_ES6);
            context.setClassShutter(DEFAULT_CLASS_SHUTTER);
            Scriptable scope = context.initStandardObjects();
            ScriptableObject.putProperty(
                scope,
                PN_DATA,
                Context.javaToJS(new MapAdapter(source.adaptTo(DataStack.class).getData()), scope));
            for (Property property : templatedProperties) {
                String result = interpolate(source, property, context, scope);
                value.putValue(property.getPath(), result);
            }
        } catch (IllegalStateException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ScriptingException(e));
        }
    }

    private static String interpolate(Source source, Property property, Context context, Scriptable scope) {
        ScriptableObject.deleteProperty(scope, PN_SOURCE);
        AbstractAdapter adapter = extractAdapter(source);
        if (adapter == null) {
            return StringUtils.EMPTY;
        }
        ScriptableObject.putProperty(scope, PN_SOURCE, Context.javaToJS(adapter, scope));

        StringBuilder result = new StringBuilder(property.getValue().toString());
        Matcher matcher = SCRIPT_TEMPLATE.matcher(result);
        while (matcher.find()) {
            String scriptResult = runScript(context, scope, matcher.group(1));
            result.replace(matcher.start(), matcher.end(), scriptResult);
            matcher.reset();
        }
        return result.toString();
    }

    private static AbstractAdapter extractAdapter(Source source) {
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

    private static boolean containsTemplate(Property property) {
        if (!String.class.equals(property.getType())) {
            return false;
        }
        Object value = property.getValue();
        if (value == null) {
            return false;
        }
        return SCRIPT_TEMPLATE.matcher(value.toString()).find();
    }
}
