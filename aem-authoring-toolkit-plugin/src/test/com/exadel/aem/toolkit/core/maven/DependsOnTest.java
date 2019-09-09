package com.exadel.aem.toolkit.core.maven;

import org.junit.Test;

import com.exadel.aem.toolkit.TestDependsOnRef;
import com.exadel.aem.toolkit.TestDependsOnRequired;
import com.exadel.aem.toolkit.TestDependsOnSetFragmentReference;

public class DependsOnTest extends ComponentTestBase {
    @Test
    public void testDependsOnRequired() {
        testComponent(TestDependsOnRequired.class);
    }

    @Test
    public void testDependsOnSetFragmentReference() {
        testComponent(TestDependsOnSetFragmentReference.class);
    }

    @Test
    public void testDependsOnRef() {
        testComponent(TestDependsOnRef.class);
    }
}
