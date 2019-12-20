package com.exadel.aem.toolkit.core.maven;

import org.junit.Test;

import com.exadel.aem.toolkit.test.component.ComplexComponent1;
import com.exadel.aem.toolkit.test.component.ComplexComponent2;
import com.exadel.aem.toolkit.test.component.ComponentWithRichTextAndExternalClasses;
import com.exadel.aem.toolkit.test.component.ComponentWithTabsAndInnerClass;

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
}
