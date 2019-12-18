package com.exadel.aem.toolkit.core.maven;

import org.junit.Test;

import com.exadel.aem.toolkit.test.dependson.DependsOnRefAnnotation;
import com.exadel.aem.toolkit.test.dependson.DependsOnRequiredAnnotation;
import com.exadel.aem.toolkit.test.dependson.DependsOnSetFragmentReference;

public class DependsOnTest extends ComponentTestBase {
    @Test
    public void testDependsOnRequired() {
        testComponent(DependsOnRequiredAnnotation.class);
    }

    @Test
    public void testDependsOnSetFragmentReference() {
        testComponent(DependsOnSetFragmentReference.class);
    }

    @Test
    public void testDependsOnRef() {
        testComponent(DependsOnRefAnnotation.class);
    }
}
