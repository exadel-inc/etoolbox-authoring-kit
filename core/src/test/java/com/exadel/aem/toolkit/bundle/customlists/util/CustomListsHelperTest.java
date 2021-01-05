package com.exadel.aem.toolkit.bundle.customlists.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.bundle.customlists.models.GenericItem;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;

public class CustomListsHelperTest {

    @Rule
    public AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);
    private ResourceResolver resolver;
    private static final String SIMPLE_LIST_PATH = "/content/aem-custom-lists/simpleList";
    private static final String CUSTOM_LIST_PATH = "/content/aem-custom-lists/customList";

    @Before
    public void setUp() {
        resolver = context.resourceResolver();
        context.load().json("/com/exadel/aem/toolkit/bundle/customlists/util/simpleList.json", SIMPLE_LIST_PATH);
        context.load().json("/com/exadel/aem/toolkit/bundle/customlists/util/customList.json", CUSTOM_LIST_PATH);
        context.addModelsForClasses(ItemModel.class);
    }

    @Test
    public void shouldReturnEmptyListForInvalidInputs() {
        List<ValueMap> actual = CustomListsHelper.getAsCustomList(null, CUSTOM_LIST_PATH);
        assertEquals(0, actual.size());

        List<ValueMap> actual2 = CustomListsHelper.getAsCustomList(resolver, "non-existing-path");
        assertEquals(0, actual2.size());
    }

    @Test
    public void getAsListSimple() {
        List<GenericItem> actual = CustomListsHelper.getAsGenericList(resolver, SIMPLE_LIST_PATH);
        assertEquals("key1", actual.get(0).getTitle());
        assertEquals("value1", actual.get(0).isValue());

        assertEquals("key2", actual.get(1).getTitle());
        assertEquals("value2", actual.get(1).isValue());

        assertEquals("key1", actual.get(2).getTitle());
        assertEquals("value3", actual.get(2).isValue());
    }

    @Test
    public void getAsListCustom() {
        List<ValueMap> actual = CustomListsHelper.getAsCustomList(resolver, CUSTOM_LIST_PATH);
        assertEquals("Hello", actual.get(0).get("textValue"));
        assertEquals(true, actual.get(0).get("booleanValue"));

        assertEquals("World", actual.get(1).get("textValue"));
        assertEquals(false, actual.get(1).get("booleanValue"));
    }

    @Test
    public void getAsListModel() {
        List<ItemModel> actual = CustomListsHelper.getAsCustomList(resolver, CUSTOM_LIST_PATH, ItemModel.class);
        assertEquals(new ItemModel("Hello", true), actual.get(0));
        assertEquals(new ItemModel("World", false), actual.get(1));
    }

    @Test
    public void getAsMapSimple() {
        Map<String, String> actual = CustomListsHelper.getAsGenericMap(resolver, SIMPLE_LIST_PATH);
        assertEquals("value3", actual.get("key1"));
        assertEquals("value2", actual.get("key2"));
    }

    @Test
    public void getAsMapCustom() {
        Map<String, ValueMap> actual = CustomListsHelper.getAsCustomMap(resolver, CUSTOM_LIST_PATH, "textValue");
        assertEquals("Hello", actual.get("Hello").get("textValue"));
        assertEquals(true, actual.get("Hello").get("booleanValue"));

        assertEquals("World", actual.get("World").get("textValue"));
        assertEquals(false, actual.get("World").get("booleanValue"));
    }

    @Test
    public void getAsMapModel() {
        Map<String, ItemModel> actual = CustomListsHelper.getAsCustomMap(resolver, CUSTOM_LIST_PATH, "textValue", ItemModel.class);
        assertEquals(new ItemModel("Hello", true), actual.get("Hello"));
        assertEquals(new ItemModel("World", false), actual.get("World"));
    }

    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class ItemModel {

        @ValueMapValue
        private String textValue;

        @ValueMapValue
        private boolean booleanValue;

        @SuppressWarnings("unused") //used by Sling injectors
        public ItemModel() {
        }

        public ItemModel(String textValue, boolean booleanValue) {
            this.textValue = textValue;
            this.booleanValue = booleanValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemModel itemModel = (ItemModel) o;
            return booleanValue == itemModel.booleanValue && Objects.equals(textValue, itemModel.textValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(textValue, booleanValue);
        }
    }
}
