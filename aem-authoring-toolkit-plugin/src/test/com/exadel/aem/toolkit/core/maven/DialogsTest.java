package com.exadel.aem.toolkit.core.maven;

import org.junit.Test;

import com.exadel.aem.toolkit.test.component.ComplexComponent1;
import com.exadel.aem.toolkit.test.component.ComplexComponent2;
import com.exadel.aem.toolkit.test.component.ComponentWithRichTextAndExternalClasses;
import com.exadel.aem.toolkit.test.component.ComponentWithTabsAndInnerClass;
import com.exadel.aem.toolkit.test.component.IgnoreWidgetColumnField;
import com.exadel.aem.toolkit.test.component.IgnoreTabsWidgetField;
import com.exadel.aem.toolkit.test.component.IgnoreFieldSuperClass;
import com.exadel.aem.toolkit.test.component.IgnoreFieldSetField;

public class DialogsTest extends ComponentTestBase {
    @Test
    public void testComponentWithRichTextAndExternalClasses() {
        testComponent(ComponentWithRichTextAndExternalClasses.class);
    }

    @Test
    public void testDialogWithTabsAndInnerClass() {
        testComponent(ComponentWithTabsAndInnerClass.class);
    }

    @Test
    public void testComplexComponent1() {
        testComponent(ComplexComponent1.class);
    }

    @Test
    public void testComplexComponent2() {
        testComponent(ComplexComponent2.class);
    }

    @Test
    public void testIgnoreFields1() {
        testComponent(IgnoreWidgetColumnField.class);
    }

    @Test
    public void testIgnoreFields2() {
        testComponent(IgnoreTabsWidgetField.class);
    }

    @Test
    public void testIgnoreFields3() {
        testComponent(IgnoreFieldSuperClass.class);
    }

    @Test
    public void testIgnoreFields4() {
        testComponent(IgnoreFieldSetField.class);
    }
}
