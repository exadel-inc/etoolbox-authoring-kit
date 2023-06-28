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
package com.exadel.aem.toolkit.plugin.annotations.scripting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.lang3.StringUtils;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.annotations.Metadata;
import com.exadel.aem.toolkit.plugin.annotations.Property;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

public class ScriptingHelper {

    private static final Pattern SCRIPT_TEMPLATE = Pattern.compile("@\\{([^}]*?)}");

    private static final ScriptEngine ENGINE;
    static {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ENGINE = factory.getScriptEngine(new RestrictingClassFilter());
    }

    private static final String PN_DATA = "data";

    private ScriptingHelper() {
    }

    public static void interpolate(Metadata value, Source source) {
        if (value == null) {
            return;
        }
        List<Property> templatedProperties = value.stream(true, true)
            .filter(ScriptingHelper::containsTemplate)
            .collect(Collectors.toList());

        if (templatedProperties.isEmpty()) {
            return;
        }

        ScriptContext scriptContext = new SimpleScriptContext();
        Bindings bindings = getBindings(source);
        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        for (Property property : templatedProperties) {
            String result = interpolate(property, scriptContext);
            value.putValue(property.getPath(), result);
        }
    }

    private static String interpolate(Property property, ScriptContext context) {
        StringBuilder result = new StringBuilder(property.getValue().toString());
        Matcher matcher = SCRIPT_TEMPLATE.matcher(result);
        while (matcher.find()) {
            String scriptResult = runScript(matcher.group(1), context);
            result.replace(matcher.start(), matcher.end(), scriptResult);
            matcher.reset();
        }
        return result.toString();
    }

    private static Bindings getBindings(Source source) {
        Bindings bindings = ENGINE.createBindings();
        DataStack dataStack = source.adaptTo(DataStack.class);
        bindings.put(PN_DATA, dataStack.getData());
        return bindings;
    }

    private static String runScript(String script, ScriptContext context) {
        try {
            Object result = ENGINE.eval(script, context);
            return result != null ? result.toString() : StringUtils.EMPTY;
        } catch (ScriptException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
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

    /* ---------
       Utilities
       --------- */

    private static class RestrictingClassFilter implements ClassFilter {
        @Override
        public boolean exposeToScripts(String name) {
            return false;
        }
    }
}
