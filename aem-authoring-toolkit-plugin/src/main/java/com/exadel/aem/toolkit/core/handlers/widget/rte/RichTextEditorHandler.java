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
package com.exadel.aem.toolkit.core.handlers.widget.rte;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.w3c.dom.Element;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.widgets.rte.AllowElement;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Characters;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.HtmlLinkRules;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.HtmlPasteRules;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.IconMapping;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.ParagraphFormat;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Style;
import com.exadel.aem.toolkit.core.exceptions.ValidationException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;

/**
 * {@link Handler} implementation for creating markup responsible for Granite UI {@code RichTextEditor} widget functionality
 * within the {@code cq:dialog} and {@code cq:editConfig} XML nodes
 */
public class RichTextEditorHandler implements Handler, BiConsumer<Element, Field> {
    private static final String KEYWORD_AUTO = "auto";

    private static final String FEATURE_ALL = "*";
    static final String PLUGIN_FEATURE_SEPARATOR = "#";

    private static final Pattern FEATURE_TOKEN_PATTERN = Pattern.compile("^(?:[\\w-]+#[\\w-:]+|-)$");
    private static final Pattern FEATURE_TOKEN_ARRAY_PATTERN = Pattern.compile("^\\[\\s*(?:(?:[\\w-]+#[\\w-:]+|-)(?:\\s*,\\s*)?)+\\s*]$");
    private static final Pattern HTML_PASTE_RULES_ALLOW_PATTERN = Pattern.compile("^allow([A-Z].+?)s?$");

    private static final String MALFORMED_TOKEN_EXCEPTION_MESSAGE = "Malformed feature token in @RichTextEditor";

    private RichTextEditor rteAnnotation;
    private boolean renderDialogFullScreenNode;

    public RichTextEditorHandler() {
        this(true);
    }
    public RichTextEditorHandler(boolean renderDialogFullScreenNode) {
        this.renderDialogFullScreenNode = renderDialogFullScreenNode;
    }

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param field Current {@code Field} instance
     */
    @Override
    public void accept(Element element, Field field) {
        accept(element, field.getAnnotation(RichTextEditor.class));
    }

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param rteAnnotation Current {@link RichTextEditor} instance
     */
    public void accept(Element element, RichTextEditor rteAnnotation) {
        this.rteAnnotation = rteAnnotation;
        // create 4 basic builders: for ./uiSettings/cui/inline, ./uiSettings/cui/dialogFullScreen, ./uiSettings/cui/tableEditOptions
        // and ./rtePlugins
        XmlNodeWithListBuilder inlineBuilder = new XmlNodeWithListBuilder(DialogConstants.NN_INLINE, DialogConstants.PN_TOOLBAR);
        XmlNodeWithListBuilder fullScreenBuilder = new XmlNodeWithListBuilder(DialogConstants.NN_FULLSCREEN, DialogConstants.PN_TOOLBAR);

        XmlTreeWithListsBuilder popoversBuilder = new XmlTreeWithListsBuilder(DialogConstants.NN_POPOVERS, DialogConstants.NN_ITEMS, true);
        popoversBuilder.setPostprocessing(popoverNode -> getXmlUtil().setAttribute(popoverNode, DialogConstants.PN_REF, popoverNode.getTagName()));

        inlineBuilder.setChildBuilder(popoversBuilder);
        fullScreenBuilder.setChildBuilder(new XmlTreeWithListsBuilder(popoversBuilder)); // 'cloned' popovers builder

        XmlNodeWithListBuilder tableEditBuilder = new XmlNodeWithListBuilder(DialogConstants.NN_TABLE_EDIT_OPTIONS,DialogConstants.PN_TOOLBAR);
        tableEditBuilder.setFilter((pluginId, feature) -> DialogConstants.NN_TABLE.equals(pluginId) && !RteFeatures.TABLE_TABLE.equals(feature));
        // we do not feed non-'table#...' features to ./uiSettings/cui/tableEditOptions

        XmlTreeWithListsBuilder pluginsBuilder = new XmlTreeWithListsBuilder(DialogConstants.NN_RTE_PLUGINS, DialogConstants.PN_FEATURES);
        pluginsBuilder.setFilter((pluginId, feature) -> !DialogConstants.NN_TABLE.equals(pluginId) || FEATURE_ALL.equals(feature));
        // we do not feed table features to ./rtePlugins, except for 'table#*'

        // concat values of .features() and .fullscreenFeatures() to feed them to all four builders in single run
        Stream.concat(
                Arrays.stream(rteAnnotation.features()).map(feature -> new ImmutablePair<>(inlineBuilder, feature)),
                Arrays.stream(rteAnnotation.fullscreenFeatures()).map(feature -> new ImmutablePair<>(fullScreenBuilder, feature))
        ).forEach(featureItem -> processFeatureItem(featureItem, tableEditBuilder, pluginsBuilder));

        // build uiSettings node with subnodes, append conditionally if not empty
        Element uiSettings = getXmlUtil().createNodeElement(DialogConstants.NN_UI_SETTINGS);
        Element cui = getXmlUtil().createNodeElement(DialogConstants.NN_CUI);
        appendElement(cui, inlineBuilder.build());
        // if .features() are set, but .fullscreenFeatures() are not
        // build either node './inline', './fullscreen' and './dialogFullScreen' (if latter is needed)  from .features()
        // (that allows user to specify only .features() and avoid copy-pasting)
        if(fullScreenBuilder.isEmpty() && !inlineBuilder.isEmpty()){
            inlineBuilder.setName(DialogConstants.NN_FULLSCREEN);
            appendElement(cui, inlineBuilder.build());
            if (renderDialogFullScreenNode) {
                inlineBuilder.setName(DialogConstants.NN_DIALOG_FULL_SCREEN);
                appendElement(cui, inlineBuilder.build());
            }
        }
        // if .fullscreenFeatures() are set, build nodes './fullscreen' and './dialogFullScreen' (latter if needed) from .fullscreenFeatures()
        appendElement(cui, fullScreenBuilder.build());
        fullScreenBuilder.setName(DialogConstants.NN_DIALOG_FULL_SCREEN);
        if (renderDialogFullScreenNode) appendElement(cui, fullScreenBuilder.build());
        appendElement(cui, tableEditBuilder.build());
        appendElement(cui, getIconsNode());
        // if ./cui node has been added any children, append it to ./uiSettings and then append ./uiSettings to root element
        appendElement(uiSettings, cui);
        appendElement(element, uiSettings, RichTextEditorHandler::mergeFeatureAttributes);
        // build rtePlugins node, merge it to existing element structure (to pick up child nodes that may have already been populated)
        // then populate rtePlugins node with the context rteAnnotation fields, then merge again
        Element rtePlugins = appendElement(element, pluginsBuilder.build());
        appendElement(rtePlugins, DialogConstants.NN_PARAFORMAT, getFormatNode());
        appendElement(rtePlugins, DialogConstants.NN_MISCTOOLS, getSpecialCharactersNode());
        appendElement(rtePlugins, DialogConstants.NN_EDIT, this::populatePasteRulesNode);
        appendElement(rtePlugins, DialogConstants.NN_STYLES, this::populateStylesNode);
        appendElement(rtePlugins, DialogConstants.NN_UNDO, e -> getXmlUtil().setAttribute(e, DialogConstants.PN_MAX_UNDO_STEPS, rteAnnotation));
        appendElement(rtePlugins, DialogConstants.NN_KEYS, e -> getXmlUtil().setAttribute(e, DialogConstants.PN_TAB_SIZE, rteAnnotation));
        appendElement(rtePlugins, DialogConstants.NN_LISTS, e -> getXmlUtil().setAttribute(e, DialogConstants.PN_INDENT_SIZE, rteAnnotation));
        appendElement(element, rtePlugins, PluginXmlUtility::mergeStringAttributes);
        // build htmlLinkRules node and append to root element, if needed
        populateHtmlLinkRules(element);
    }

    /**
     * Appends non-empty child XML node to a parent node. If same-named child node exists, merges attributes of the
     * provided child with those of the existing child with use of the default merging routine
     * @param parent {@code Element} instance representing parent node
     * @param child {@code Element} instance representing child node
     * @return {@code Element} instance representing the appended node
     */
    private Element appendElement(Element parent, Element child) {
        return getXmlUtil().appendNonemptyChild(parent, child);
    }

    /**
     * Appends non-empty child XML node to a parent node. If same-named child node exists, merges attributes of the
     * provided child with those of the existing child with use of the provided merger
     * @param parent {@code Element} instance representing parent node
     * @param child {@code Element} instance representing child node
     * @param merger {@code BinaryOperator<String>} instance
     */
    private void appendElement(Element parent, Element child, BinaryOperator<String> merger) {
        getXmlUtil().appendNonemptyChild(parent, child, merger);
    }

    /**
     * Appends non-empty child XML node to the child node of the provided parent that has the specified name
     * @param parent {@code Element} instance representing parent node
     * @param existingChildName String representing the name of the existing child
     * @param newChild {@code Element} instance representing new child node
     */
    private void appendElement(Element parent, String existingChildName, Element newChild) {
        Element existingChild = getXmlUtil().getChildElement(parent, existingChildName);
        getXmlUtil().appendNonemptyChild(existingChild, newChild);
    }

    /**
     * Appends non-empty child XML node to the child node of the provided parent that has the specified name
     * @param parent {@code Element} instance representing parent node
     * @param existingChildName String representing the name of the existing child
     * @param elementConsumer {@code Consumer<Supplier<Element>>} routine that implements lazy generation of child
     * {@code Element} (one that is triggered only if element with {@code existingChildName} actually present)
     */
    private void appendElement(Element parent, String existingChildName, Consumer<Supplier<Element>> elementConsumer) {
        if (parent == null) {
            return;
        }
        Element child = getXmlUtil().getChildElementNode(parent, existingChildName,
                p -> PluginRuntime.context().getXmlUtility().createNodeElement(existingChildName));
        elementConsumer.accept(() -> {
            if (child.getParentNode() != null) {
                return child; // not to 'reattach' the previously existed child, which would change sequence of children
            }
            return (Element)parent.appendChild(child);
        }); // so that the newly created child is appended to DOM only if .get() is called in the context of elementConsumer
    }

    private static void processFeatureItem(
            ImmutablePair<XmlNodeWithListBuilder,String> featureItem,
            XmlNodeWithListBuilder tableEditBuilder,
            XmlTreeWithListsBuilder pluginsBuilder
    ) {
        XmlNodeWithListBuilder nodeBuilder = featureItem.left;
        String feature = featureItem.right;
        if (FEATURE_TOKEN_PATTERN.matcher(feature).matches()) {
            // single#feature
            nodeBuilder.store(null, feature);
            pluginsBuilder.store(null, feature);

        } else if (FEATURE_TOKEN_ARRAY_PATTERN.matcher(feature).matches()) {
            // [popover#features...], [table#features...], [some:specific:feature]
            String[] nestedTokens = getNestedTokens(feature);
            String leadingPluginToken = StringUtils.substringBefore(nestedTokens[0], PLUGIN_FEATURE_SEPARATOR);
            if (leadingPluginToken.equals(DialogConstants.NN_TABLE)) {
                nodeBuilder.store(null, RteFeatures.TABLE_TABLE);
                tableEditBuilder.storeMany(leadingPluginToken, nestedTokens);
            } else {
                nodeBuilder.store(null, PLUGIN_FEATURE_SEPARATOR + leadingPluginToken);
                nodeBuilder.getChildBuilder().storeMany(leadingPluginToken, nestedTokens);
            }
            if (leadingPluginToken.equals(DialogConstants.NN_TABLE)
                    || leadingPluginToken.equals(DialogConstants.NN_PARAFORMAT)
                    || leadingPluginToken.equals(DialogConstants.NN_STYLES)) {
                pluginsBuilder.store(leadingPluginToken, FEATURE_ALL);
            } else {
                pluginsBuilder.storeMany(null, nestedTokens);
            }

        } else {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(MALFORMED_TOKEN_EXCEPTION_MESSAGE));
        }
    }

    private Element getIconsNode() {
        return getXmlUtil().createNodeElement(DialogConstants.NN_ICONS,
                iconMapping -> ((IconMapping)iconMapping).command(),
                rteAnnotation.icons());
    }
    private Element getFormatNode(){
        Element result = getXmlUtil().createNodeElement(DialogConstants.NN_FORMATS,
                paragraphFormat -> ((ParagraphFormat)paragraphFormat).tag(),
                rteAnnotation.formats());
        result.setAttribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_WIDGET_COLLECTION);
        return result;
    }
    private Element getSpecialCharactersNode() {
        Function<Annotation, String> childNodeNameProvider = c -> {
            Characters chars = (Characters)c;
            return chars.rangeStart() > 0 ? String.valueOf(chars.rangeStart()) : chars.entity();
        };
        Element charsConfigNode = getXmlUtil().createNodeElement(DialogConstants.NN_SPECIAL_CHARS_CONFIG);
        Element charsNode = getXmlUtil().createNodeElement(DialogConstants.NN_CHARS,
                childNodeNameProvider,
                rteAnnotation.specialCharacters());
        getXmlUtil().appendNonemptyChild(charsConfigNode, charsNode);
        return charsConfigNode;
    }

    private void populateStylesNode(Supplier<Element> elementSupplier){
        Element stylesElement = elementSupplier.get();
        getXmlUtil().setAttribute(stylesElement, DialogConstants.PN_EXTERNAL_STYLESHEETS, rteAnnotation, PluginXmlUtility::mergeStringAttributes);
        if (!featureExists(RteFeatures.Popovers.STYLES::equals)) {
            return;
        }
        Element nestedStylesNode = getXmlUtil().createNodeElement(DialogConstants.NN_STYLES, style -> ((Style)style).cssName(), rteAnnotation.styles());
        nestedStylesNode.setAttribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_WIDGET_COLLECTION);
        getXmlUtil().appendNonemptyChild(stylesElement, nestedStylesNode, PluginXmlUtility::mergeStringAttributes);
        getXmlUtil().setAttribute(stylesElement, DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_WIDGET_COLLECTION);
    }

    private void populatePasteRulesNode(Supplier<Element> elementSupplier){
        HtmlPasteRules rules = this.rteAnnotation.htmlPasteRules();
        Element htmlPasteRulesNode = getXmlUtil().createNodeElement(DialogConstants.NN_HTML_PASTE_RULES);
        List<String> nonDefaultAllowPropsNames = PluginReflectionUtility.getAnnotationNonDefaultProperties(rules).stream()
                .filter(field -> HTML_PASTE_RULES_ALLOW_PATTERN.matcher(field.getName()).matches())
                .map(field -> HTML_PASTE_RULES_ALLOW_PATTERN.matcher(field.getName()).replaceAll("$1").toLowerCase())
                .filter(propName -> {
                    if (StringUtils.equalsAny(propName, DialogConstants.NN_TABLE, DialogConstants.NN_LIST)) {
                        htmlPasteRulesNode.appendChild(getHtmlPasteRulesNode(propName, DialogConstants.NN_TABLE.equals(propName)
                                ? rules.allowTables()
                                : rules.allowLists()));
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        if (!nonDefaultAllowPropsNames.isEmpty()) {
            Element allowBasicsNode = getXmlUtil().createNodeElement(DialogConstants.NN_ALLOW_BASICS);
            // default values are all 'true' so non-defaults are 'false'
            nonDefaultAllowPropsNames.forEach(fieldName -> getXmlUtil().setAttribute(allowBasicsNode, fieldName, false));
            htmlPasteRulesNode.appendChild(allowBasicsNode);
        }
        getXmlUtil().setAttribute(htmlPasteRulesNode, DialogConstants.PN_ALLOW_BLOCK_TAGS, Arrays.asList(rules.allowedBlockTags()));
        getXmlUtil().setAttribute(htmlPasteRulesNode, DialogConstants.PN_FALLBACK_BLOCK_TAG, rules.fallbackBlockTag());

        getXmlUtil().appendNonemptyChild(elementSupplier, htmlPasteRulesNode, RichTextEditorHandler::mergeFeatureAttributes);
        getXmlUtil().setAttribute(elementSupplier, DialogConstants.PN_DEFAULT_PASTE_MODE, rteAnnotation);
    }
    private Element getHtmlPasteRulesNode(String disallowedEntity, AllowElement allowRule) {
        Element disallowed = getXmlUtil().createNodeElement(disallowedEntity);
        getXmlUtil().setAttribute(disallowed, DialogConstants.PN_ALLOW, false);
        getXmlUtil().setAttribute(disallowed, DialogConstants.PN_IGNORE_MODE, allowRule.toString());
        return disallowed;
    }

    private void populateHtmlLinkRules(Element element) {
        HtmlLinkRules rules = this.rteAnnotation.htmlLinkRules();
        if (!PluginReflectionUtility.annotationIsNotDefault(rules)) {
            return;
        }
        Element htmlRulesNode = getXmlUtil().createNodeElement(DialogConstants.NN_HTML_RULES);

        Map<String, String> htmlLinksProps = ImmutableMap.<String,String>builder()
                .put(DialogConstants.PN_CSS_EXTERNAL, rules.cssExternal())
                .put(DialogConstants.PN_CSS_INTERNAL, rules.cssInternal())
                .put(DialogConstants.PN_DEFAULT_PROTOCOL, rules.defaultProtocol())
                .build();
        Element linksNode = getXmlUtil().createNodeElement(DialogConstants.NN_LINKS, htmlLinksProps);
        getXmlUtil().setAttribute(linksNode, DialogConstants.PN_PROTOCOLS, Arrays.asList(rules.protocols()));

        Map<String, String> targetConfigProps = ImmutableMap.<String,String>builder()
                .put(DialogConstants.PN_MODE, KEYWORD_AUTO)
                .put(DialogConstants.PN_TARGET_EXTERNAL, rules.targetExternal().toString())
                .put(DialogConstants.PN_TARGET_INTERNAL, rules.targetInternal().toString())
                .build();
        Element targetConfigNode = getXmlUtil().createNodeElement(DialogConstants.NN_TARGET_CONFIG, targetConfigProps);

        appendElement(linksNode, targetConfigNode);
        appendElement(htmlRulesNode, linksNode);
        appendElement(element, htmlRulesNode, PluginXmlUtility::mergeStringAttributes);
    }

    private static String mergeFeatureAttributes(String array0, String array1) {
        if (!PluginXmlUtility.ATTRIBUTE_LIST_PATTERN.matcher(array0).matches() || !PluginXmlUtility.ATTRIBUTE_LIST_PATTERN.matcher(array1).matches()) {
            return PluginXmlUtility.DEFAULT_ATTRIBUTE_MERGER.apply(array0, array1);
        }
        List<String> result = new LinkedList<>(Arrays.asList(getNestedTokens(array0)));
        Arrays.stream(getNestedTokens(array1))
                .forEach(token -> {if(!result.contains(token)) result.add(token);}); // not using Set<> here because of repeating tokens, such as separators
        return String.format(PluginXmlUtility.ATTRIBUTE_LIST_TEMPLATE, String.join(RteFeatures.FEATURE_SEPARATOR, result));
    }

    private static String[] getNestedTokens(String array) {
        return StringUtils.strip(array, PluginXmlUtility.ATTRIBUTE_LIST_SURROUND).split(PluginXmlUtility.ATTRIBUTE_LIST_SPLIT_PATTERN);
    }

    private boolean featureExists(Predicate<String> matcher) {
        return Stream.concat(Arrays.stream(rteAnnotation.features()), Arrays.stream(rteAnnotation.fullscreenFeatures()))
                .map(s -> Arrays.stream(s.split(PluginXmlUtility.ATTRIBUTE_LIST_SPLIT_PATTERN)))
                .flatMap(stringStream -> stringStream)
                .anyMatch(matcher);
    }

}
