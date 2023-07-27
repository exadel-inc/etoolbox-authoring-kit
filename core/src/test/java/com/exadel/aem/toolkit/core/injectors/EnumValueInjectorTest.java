package com.exadel.aem.toolkit.core.injectors;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.sling.api.adapter.Adaptable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.TestConstants;
import com.exadel.aem.toolkit.core.injectors.models.enums.Colors;
import com.exadel.aem.toolkit.core.injectors.models.enums.EnumArrays;
import com.exadel.aem.toolkit.core.injectors.models.enums.EnumArraysViaCustomAccessor;
import com.exadel.aem.toolkit.core.injectors.models.enums.EnumCollections;
import com.exadel.aem.toolkit.core.injectors.models.enums.Enums;

public class EnumValueInjectorTest {

    private static final String MODELS_PACKAGE_NAME = CoreConstants.ROOT_PACKAGE + ".core.injectors.models.enums";

    private static final Colors EXPECTED_COLOR = Colors.YELLOW;
    private static final Colors[] EXPECTED_COLOR_ARRAY = new Colors[] {Colors.YELLOW, Colors.ORANGE};
    private static final List<Colors> EXPECTED_COLOR_COLLECTION = Arrays.asList(Colors.YELLOW, Colors.ORANGE);

    @Rule
    public final AemContext context = new AemContext();

    /* -----------
       Preparation
       ----------- */

    @Before
    public void beforeTest() {
        context.addModelsForClasses(MODELS_PACKAGE_NAME);
        EnumValueInjector enumValueInjector = new EnumValueInjector();
        context.registerInjectActivateService(enumValueInjector);
        context.registerInjectActivateService(new DelegateInjector(enumValueInjector));
        context.load().json("/com/exadel/aem/toolkit/core/injectors/enumInjector.json", TestConstants.ROOT_RESOURCE);
        context.request().setResource(context.resourceResolver().getResource("/content/jcr:content/resource"));
    }

    @Test
    public void shouldInjectEnumConstant() {
        shouldInjectEnumConstant(context.request());
        shouldInjectEnumConstant(context.request().getResource());
    }

    private void shouldInjectEnumConstant(Adaptable adaptable) {
        Enums model = adaptable.adaptTo(Enums.class);
        assertNotNull(model);
        assertEquals(EXPECTED_COLOR, model.getValue());
        assertEquals(EXPECTED_COLOR.name(), model.getObjectValue());
        assertEquals(EXPECTED_COLOR, model.getValueSupplier().getValue());
        assertEquals(EXPECTED_COLOR, model.getConstructorValue());
    }

    @Test
    public void shouldInjectEnumArray() {
        shouldInjectEnumArray(context.request());
        shouldInjectEnumArray(context.request().getResource());
    }

    private void shouldInjectEnumArray(Adaptable adaptable) {
        EnumArrays model = adaptable.adaptTo(EnumArrays.class);
        assertNotNull(model);
        assertArrayEquals(EXPECTED_COLOR_ARRAY, model.getValue());
        assertEquals(EXPECTED_COLOR.name(), model.getObjectValue());
        assertArrayEquals(EXPECTED_COLOR_ARRAY, model.getValueSupplier().getValue());
        assertArrayEquals(EXPECTED_COLOR_ARRAY, model.getConstructorValue());
    }

    @Test
    public void shouldInjectEnumCollection() {
        shouldInjectEnumCollection(context.request());
        shouldInjectEnumCollection(context.request().getResource());
    }

    private void shouldInjectEnumCollection(Adaptable adaptable) {
        EnumCollections model = adaptable.adaptTo(EnumCollections.class);
        assertNotNull(model);
        assertNotNull(model.getValue());
        assertEquals(EXPECTED_COLOR.name(), model.getObjectValue());
        assertTrue(CollectionUtils.isEqualCollection(EXPECTED_COLOR_COLLECTION, model.getValue()));
        assertNotNull(model.getValueSupplier().getValue());
        assertTrue(CollectionUtils.isEqualCollection(EXPECTED_COLOR_COLLECTION, model.getValueSupplier().getValue()));
        assertNotNull(model.getConstructorValue());
        assertTrue(CollectionUtils.isEqualCollection(EXPECTED_COLOR_COLLECTION, model.getConstructorValue()));
    }

    @Test
    public void shouldInjectEnumArrayViaCustomAccessor() {
        shouldInjectEnumArrayViaCustomAccessor(context.request());
        shouldInjectEnumArrayViaCustomAccessor(context.request().getResource());
    }

    private void shouldInjectEnumArrayViaCustomAccessor(Adaptable adaptable) {
        EnumArraysViaCustomAccessor model = adaptable.adaptTo(EnumArraysViaCustomAccessor.class);
        assertNotNull(model);
        assertArrayEquals(EXPECTED_COLOR_ARRAY, model.getValue());
        assertArrayEquals(EXPECTED_COLOR_ARRAY, model.getValueSupplier().getValue());
        assertArrayEquals(EXPECTED_COLOR_ARRAY, model.getConstructorValue());
    }
}
