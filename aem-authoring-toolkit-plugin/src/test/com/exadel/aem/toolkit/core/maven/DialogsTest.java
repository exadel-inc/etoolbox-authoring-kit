package com.exadel.aem.toolkit.core.maven;

import org.junit.Test;

import com.exadel.aem.toolkit.test.component.ComponentWithRichTextAndExternalClasses;
import com.exadel.aem.toolkit.test.component.ComponentWithTabsAndInnerClass;
import com.exadel.aem.toolkit.test.component.FeedVideoImage;
import com.exadel.aem.toolkit.test.component.ComplexComponent;

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
    public void testFeedVideoImageDialog() {
        testComponent(FeedVideoImage.class);
    }

    @Test
    public void testSampleComponent() {
        testComponent(ComplexComponent.class);
    }
}
