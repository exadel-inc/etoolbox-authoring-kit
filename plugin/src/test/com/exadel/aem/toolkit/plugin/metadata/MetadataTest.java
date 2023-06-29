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
package com.exadel.aem.toolkit.plugin.metadata;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.WriteMode;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.PasteMode;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.annotations.cases.NestedAnnotations;
import com.exadel.aem.toolkit.plugin.exceptions.PluginException;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ScriptedComponent;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.plugin.maven.ThrowsPluginException;

public class MetadataTest {

    private static final String PN_OPTIONS = "moreOptions";
    private static final String PN_NUMBERS = "numbers";

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testAnnotationCreation() {
        AemComponent annotation = Metadata.from(AemComponent.class);
        Assert.assertNotNull(annotation);
        Assert.assertEquals(StringUtils.EMPTY, annotation.path());
        Assert.assertEquals(0, annotation.views().length);

        Metadata metadata = (Metadata) annotation;
        String titleValue = "Title";
        metadata.putValue(CoreConstants.PN_TITLE, titleValue);
        Object putResult = metadata.putValue(CoreConstants.PN_TITLE, titleValue);
        metadata.putValue("views", new Class<?>[] {Object.class, Serializable.class});
        Assert.assertEquals(titleValue, annotation.title());
        Assert.assertEquals(putResult, annotation.title());
        Assert.assertEquals(2, annotation.views().length);
    }

    @Test
    public void testNestedAnnotationCreation1() {
        RichTextEditor annotation = Metadata.from(
            RichTextEditor.class,
            Collections.singletonMap("/features[0]", "feature#1"));
        Assert.assertNotNull(annotation);
        Assert.assertEquals(0, annotation.icons().length);
        Assert.assertEquals(StringUtils.EMPTY, annotation.htmlLinkRules().defaultProtocol());
        Assert.assertEquals(0, annotation.htmlLinkRules().protocols().length);

        Metadata metadata = (Metadata) annotation;
        metadata.putValue("/icons[0]/icon", "first");
        metadata.putValue("/icons[0]/command", "1");
        metadata.putValue("/icons[1]/icon", "second");
        metadata.putValue("/icons[1]/command", "2");
        metadata.putValue("htmlLinkRules/defaultProtocol", "http:");
        metadata.putValue("htmlLinkRules/protocols[0]", "ftp:");
        Assert.assertEquals(1, annotation.features().length);
        Assert.assertEquals(2, annotation.icons().length);
        Assert.assertEquals("second", annotation.icons()[1].icon());
        Assert.assertEquals(1, annotation.htmlLinkRules().protocols().length);
        Assert.assertEquals("ftp:", annotation.htmlLinkRules().protocols()[0]);
        Assert.assertEquals("http:", annotation.htmlLinkRules().defaultProtocol());
    }

    @Test
    public void testNestedAnnotationCreation2() {
        NestedAnnotations.Host host = Metadata.from(NestedAnnotations.Host.class);
        Assert.assertNotNull(host);

        Metadata metadata = (Metadata) host;
        Assert.assertNotNull(host.value());
        metadata.putValue("value.numbers[0]", 42);
        Assert.assertEquals(1, host.value().numbers().length);
        Assert.assertEquals(42, host.value().numbers()[0]);

        NestedAnnotations.Level0 level0 = Metadata.from(NestedAnnotations.Level0.class);
        Assert.assertNotNull(level0);
        Assert.assertEquals(0, level0.value().length);

        metadata = (Metadata) level0;
        metadata.putValue("/value[0]/value[0]/numbers[0]", 42);
        metadata.putValue("/value[0]/value[0]/numbers[1]", 43);
        metadata.putValue("/value[1]/value[0]/numbers[0]", 44);
        Assert.assertEquals(2, level0.value().length);
        Assert.assertEquals(2, level0.value()[0].value()[0].numbers().length);
        Assert.assertEquals(44, level0.value()[1].value()[0].numbers()[0]);
    }

    @Test
    public void testAnnotationModification() {
        Metadata metadata = Metadata.from(ScriptedComponent.class.getAnnotation(AemComponent.class));
        Assert.assertNotNull(metadata);
        Assert.assertEquals("Scripted Component", ((AemComponent) metadata).title());
        Assert.assertEquals(0, ((AemComponent) metadata).views().length);
        Assert.assertFalse(((AemComponent) metadata).disableTargeting());

        metadata.putValue("title", "Modified Component Dialog");
        metadata.putValue("disableTargeting", true);
        Assert.assertEquals("Modified Component Dialog", ((AemComponent) metadata).title());
        Assert.assertTrue(((AemComponent) metadata).disableTargeting());
    }

    @Test
    public void testNestedAnnotationModification1() throws NoSuchFieldException {
        RichTextEditor annotation = Metadata.from(
            getTextField().getAnnotation(RichTextEditor.class),
            RichTextEditor.class);
        Assert.assertNotNull(annotation);
        Assert.assertEquals(PasteMode.WORDHTML, annotation.defaultPasteMode());
        Assert.assertFalse(annotation.useFixedInlineToolbar());
        Assert.assertEquals(3, annotation.icons().length);
        Assert.assertEquals(2, annotation.htmlLinkRules().protocols().length);
        Assert.assertEquals(25, annotation.maxUndoSteps());

        Metadata metadata = (Metadata) annotation;
        metadata.putValue("features()", new String[] {"first", "second"});
        metadata.putValue("specialCharacters()[1].entity()", "euro");
        metadata.putValue("htmlLinkRules().defaultProtocol()", "ftp:");
        metadata.putValue("htmlLinkRules().protocols()[1]", "gopher:");
        metadata.unsetValue("maxUndoSteps");
        Assert.assertEquals(2, annotation.features().length);
        Assert.assertEquals("euro", annotation.specialCharacters()[1].entity());
        Assert.assertEquals("ftp:", annotation.htmlLinkRules().defaultProtocol());
        Assert.assertArrayEquals(new String[] {"http:", "gopher:"}, annotation.htmlLinkRules().protocols());
        Assert.assertEquals(50, annotation.maxUndoSteps());
    }

    @Test
    public void testNestedAnnotationModification2() throws NoSuchFieldException {
        Select annotation = Metadata.from(getOptionField().getAnnotation(Select.class), Select.class);
        Assert.assertNotNull(annotation);
        Assert.assertEquals("source1", annotation.optionProvider().value()[0].value());
        Assert.assertEquals("source3_2", annotation.optionProvider().value()[2].attributes()[1]);

        Metadata metadata = (Metadata) annotation;
        metadata.putValue("/options[0]/value", "1000");
        metadata.putValue("optionProvider/value[0]/value", "modified1");
        metadata.putValue("optionProvider/value[2]/attributes[1]", "modified3_2");
        Assert.assertEquals("modified1", annotation.optionProvider().value()[0].value());
        Assert.assertEquals("modified3_2", annotation.optionProvider().value()[2].attributes()[1]);
    }

    @Test
    @ThrowsPluginException
    public void testAnnotationValueReading() throws NoSuchFieldException {
        Metadata metadata = Metadata.from(getTextField().getAnnotation(RichTextEditor.class));
        Assert.assertEquals(PasteMode.WORDHTML, metadata.getValue("defaultPasteMode"));
        Assert.assertFalse((boolean) metadata.getValue("useFixedInlineToolbar"));
        Assert.assertEquals("copy", metadata.getValue("icons()[0].icon()"));
        Assert.assertEquals("http:", metadata.getValue("htmlLinkRules/defaultProtocol"));
        Assert.assertEquals(25L, metadata.getValue("maxUndoSteps"));

        String[] features = new String[] {"first", "second"};
        metadata.putValue("features()", features);
        Assert.assertArrayEquals(features, (String[]) metadata.getValue("features"));

        String entity = "euro";
        metadata.putValue("specialCharacters()[1].entity()", entity);
        Assert.assertEquals(entity, metadata.getValue("specialCharacters[1]/entity"));
    }

    @Test
    public void testGetAnnotation() {
        Target target = ((Metadata) Metadata.from(RichTextEditor.class)).getAnnotation(Target.class);
        Assert.assertNotNull(target);
        Assert.assertArrayEquals(new ElementType[] {ElementType.FIELD, ElementType.METHOD}, target.value());
    }

    @Test
    public void testGetAnyAnnotation() {
        Annotation retention = ((Metadata) Metadata.from(RichTextEditor.class)).getAnyAnnotation(Retention.class, Target.class);
        Assert.assertTrue(retention instanceof Retention);
        Assert.assertEquals(RetentionPolicy.RUNTIME, ((Retention) retention).value());
    }

    @Test
    public void testDefaultValueDetection() throws NoSuchFieldException {
        Metadata rteAnnotation = Metadata.from(getTextField().getDeclaredAnnotation(RichTextEditor.class));
        Assert.assertTrue(rteAnnotation.getProperty("styles").valueIsDefault());
        Assert.assertTrue(rteAnnotation.getProperty("htmlPasteRules").valueIsDefault());
        Assert.assertTrue(rteAnnotation.getProperty("htmlLinkRules().cssInternal()").valueIsDefault());
        Assert.assertFalse(rteAnnotation.getProperty("features").valueIsDefault());
        Assert.assertFalse(rteAnnotation.getProperty("htmlLinkRules").valueIsDefault());
        Assert.assertFalse(rteAnnotation.getProperty(PropertyPath.parse("htmlLinkRules/defaultProtocol")).valueIsDefault());

        rteAnnotation = (Metadata) Metadata.from(RichTextEditor.class);
        Assert.assertTrue(rteAnnotation.stream(true, false).allMatch(Property::valueIsDefault));
        rteAnnotation.putValue("features[0]", "feature");
        Assert.assertFalse(rteAnnotation.stream().allMatch(Property::valueIsDefault));
    }

    @Test
    public void testAnnotationIterator() throws NoSuchFieldException {
        Metadata metadata = Metadata.from(getNumbersField().getAnnotation(NestedAnnotations.Level0.class));
        Assert.assertNotNull(metadata);
        Map<String, Object> properties = new LinkedHashMap<>();
        metadata.forEach(next -> properties.put(next.getPath(), next.getValue()));
        Assert.assertEquals(1, properties.size());

        properties.clear();
        Iterator<Property> iterator = metadata.iterator(false, true);
        while (iterator.hasNext()) {
            Property next = iterator.next();
            properties.put(next.getPath(), next.getValue());
        }
        Assert.assertEquals(3, properties.size());

        properties.clear();
        iterator = metadata.iterator(true, true);
        while (iterator.hasNext()) {
            Property next = iterator.next();
            properties.put(next.getPath(), next.getValue());
        }
        Assert.assertEquals(15, properties.size());
    }

    @Test
    public void testAnnotationSpliterator() throws NoSuchFieldException {
        Metadata metadata = Metadata.from(getNumbersField().getAnnotation(NestedAnnotations.Level0.class));
        Map<String, Object> collectedValues = StreamSupport
            .stream(metadata.spliterator(), false)
            .collect(Collectors.toMap(Property::getPath, Property::getValue));

        Assert.assertEquals(1, collectedValues.size());
        Assert.assertEquals(3, Array.getLength(collectedValues.values().toArray()[0]));

        collectedValues = StreamSupport
            .stream(metadata.spliterator(false, true), false)
            .collect(Collectors.toMap(Property::getPath, Property::getValue));
        Assert.assertEquals(3, collectedValues.size());
        String[] collectedKeys = collectedValues.keySet().toArray(new String[0]);
        Arrays.sort(collectedKeys);
        Assert.assertArrayEquals(new String[] {"value[0]", "value[1]", "value[2]"}, collectedKeys);

        int[] collectedNumbers = StreamSupport
            .stream(metadata.spliterator(true, true), false)
            .map(Property::getValue)
            .mapToInt(v -> (Integer) v)
            .toArray();
        Assert.assertArrayEquals(IntStream.range(0, 15).toArray(), collectedNumbers);
    }

    @Test
    @ThrowsPluginException
    public void testExceptionWhenSettingUnknownProperty() throws NoSuchFieldException {
        Metadata metadata = Metadata.from(getTextField().getAnnotation(RichTextEditor.class));
        Assert.assertThrows(
            PluginException.class,
            () -> metadata.putValue("foo", "bar"));
    }

    @Test
    @ThrowsPluginException
    public void testExceptionWhenSettingWrongType() throws NoSuchFieldException {
        Metadata metadata = Metadata.from(getTextField().getAnnotation(RichTextEditor.class));
        Assert.assertThrows(
            PluginException.class,
            () -> metadata.putValue("externalStyleSheets", 42));
    }

    @Test
    @ThrowsPluginException
    public void testExceptionArrayOutOfBounds() throws NoSuchFieldException {
        Field optionField = ScriptedComponent.class.getDeclaredField(PN_OPTIONS);
        Metadata metadata = Metadata.from(optionField.getAnnotation(Select.class));
        Assert.assertThrows(
            PluginException.class,
            () -> metadata.putValue("/optionProvider/value[0]/attributes[5]", 42));
    }

    @Test
    public void testHashCode() {
        AemComponent annotation1 = Metadata.from(AemComponent.class);
        Assert.assertNotNull(annotation1);

        int hashCode1 = annotation1.hashCode();
        Assert.assertTrue(hashCode1 != 0);
        ((Metadata) annotation1).putValue("title", "New title");
        Assert.assertNotEquals(hashCode1, annotation1.hashCode());
    }

    @Test
    public void testEquals() {
        Metadata annotation1 = Metadata.from(ScriptedComponent.class.getAnnotation(AemComponent.class));
        Assert.assertNotNull(annotation1);
        Metadata annotation2 = Metadata.from(ScriptedComponent.class.getAnnotation(AemComponent.class));
        Assert.assertNotNull(annotation2);

        Assert.assertEquals(annotation1, annotation2);
        annotation1.putValue("title", "New title");
        annotation1.putValue("writeMode", WriteMode.CREATE);
        Assert.assertNotEquals(annotation1, annotation2);
        annotation2.putValue("title", "New title");
        annotation2.putValue("writeMode", WriteMode.CREATE);
        Assert.assertEquals(annotation1, annotation2);
    }

    /* ---------------
       Service methods
       --------------- */

    private static Field getTextField() throws NoSuchFieldException {
        return ScriptedComponent.class.getDeclaredField(CoreConstants.PN_TEXT);
    }

    private static Field getOptionField() throws NoSuchFieldException {
        return ScriptedComponent.class.getDeclaredField(PN_OPTIONS);
    }

    private static Field getNumbersField() throws NoSuchFieldException {
        return ScriptedComponent.class.getDeclaredField(PN_NUMBERS);
    }
}
