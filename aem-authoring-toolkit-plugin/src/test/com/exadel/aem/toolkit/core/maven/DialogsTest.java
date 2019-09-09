package com.exadel.aem.toolkit.core.maven;

import org.junit.Test;

import com.exadel.aem.toolkit.TestComponentWithRichTextAndExternalClasses;
import com.exadel.aem.toolkit.TestComponentWithTabsAndInnerClass;
import com.exadel.aem.toolkit.TestFeedVideoImage;
import com.exadel.aem.toolkit.TestSampleComponent;

public class DialogsTest extends ComponentTestBase {
    @Test
    public void testComponentWithRichTextAndExternalClasses() {
        testComponent(TestComponentWithRichTextAndExternalClasses.class);
    }

    @Test
    public void testDialogWithTabsAndInnerClass() {
        testComponent(TestComponentWithTabsAndInnerClass.class);
    }

    @Test
    public void testFeedVideoImageDialog() {
        testComponent(TestFeedVideoImage.class);
    }

    @Test
    public void testSampleComponent() {
        testComponent(TestSampleComponent.class);
    }
}
