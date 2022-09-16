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
package com.exadel.aem.toolkit.plugin.handlers.widgets;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.codeeditor.CodeEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.codeeditor.CodeEditorOption;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.StringUtil;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the {@code CodeEditor} widget look and behavior
 */
@Handles(CodeEditor.class)
public class CodeEditorHandler implements Handler {

    private static final String CLIENTLIB_CATEGORY_EDITOR = "eak.widgets.editor";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new SimpleModule()
            .addSerializer(CodeEditorOption[].class, new CodeEditorOptionsSerializer()));

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code
     * Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        CodeEditor codeEditor = source.adaptTo(CodeEditor.class);
        target.attributes(codeEditor);
        populateClientLibrary(target);
        if (ArrayUtils.isEmpty(codeEditor.options())) {
            return;
        }
        try {
            String options = OBJECT_MAPPER.writeValueAsString(codeEditor.options());
            target.attribute(DialogConstants.NN_OPTIONS, escapeJson(options));
        } catch (JsonProcessingException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
    }

    /**
     * Conditionally stores the reference to the {@code eak.widgets.editor} client library to the current dialog
     * @param target Resulting {@code Target} object
     */
    private static void populateClientLibrary(Target target) {
        Target dialogRoot = target.findParent(t -> DialogConstants.NN_ROOT.equals(t.getName()));
        if (dialogRoot == null) {
            return;
        }
        String extraClientlibs = dialogRoot.getAttribute(DialogConstants.PN_EXTRA_CLIENTLIBS);
        if (StringUtils.isEmpty(extraClientlibs)) {
            dialogRoot.attribute(DialogConstants.PN_EXTRA_CLIENTLIBS, new String[]{CLIENTLIB_CATEGORY_EDITOR});
        } else {
            Set<String> extraClientlibSet = StringUtil.parseSet(extraClientlibs);
            extraClientlibSet.add(CLIENTLIB_CATEGORY_EDITOR);
            dialogRoot.attribute(DialogConstants.PN_EXTRA_CLIENTLIBS, StringUtil.format(extraClientlibSet, String.class));
        }
    }

    /**
     * Prepares the provided JSON string for storing as an XML attribute value
     * @param value A non-null JSON string
     * @return String value
     */
    private static String escapeJson(String value) {
        return value.replace("{", "\\{").replace("}", "\\}");
    }

    /**
     * Represents {@link JsonSerializer} for storing the configuration set up via {@link CodeEditor} in the content
     * repository
     */
    private static class CodeEditorOptionsSerializer extends JsonSerializer<CodeEditorOption[]> {

        /**
         * Retrieves a JSON render of the provided {@code CodeEditor} annotation
         * @param options            An array of user-specified {@link CodeEditorOption} values
         * @param jsonGenerator      Managed {@code JsonGenerator} object
         * @param serializerProvider Managed {@code SerializerProvider} object
         * @throws IOException if the serialization fails
         */
        @Override
        public void serialize(
            CodeEditorOption[] options,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {

            jsonGenerator.writeStartObject();
            for (CodeEditorOption option : options) {
                if (option.type().equals(Boolean.class) || option.type().equals(boolean.class)) {
                    jsonGenerator.writeBooleanField(option.name(), Boolean.parseBoolean(option.value()));
                } else if (
                    StringUtils.isNumeric(option.value())
                        && (ClassUtils.primitiveToWrapper(option.type()).equals(Integer.class)
                        || ClassUtils.primitiveToWrapper(option.type()).equals(Long.class))
                ) {
                    jsonGenerator.writeNumberField(option.name(), Long.parseLong(option.value()));
                } else {
                    jsonGenerator.writeStringField(option.name(), option.value());
                }
            }
            jsonGenerator.writeEndObject();
        }
    }
}
