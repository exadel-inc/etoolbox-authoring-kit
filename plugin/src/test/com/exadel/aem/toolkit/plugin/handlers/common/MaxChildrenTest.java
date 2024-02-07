package com.exadel.aem.toolkit.plugin.handlers.common;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.plugin.handlers.common.cases.maxchildren.MaxChildrenTestCases;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;

public class MaxChildrenTest {

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testSimpleAnnotation() {
        pluginContext.test(MaxChildrenTestCases.SimpleMaxLimitAnnotation.class, "handlers/common/maxChildren/simple");
    }

    @Test
    public void testAllowedChildrenWithMaxLimitConflict() {
        pluginContext.test(MaxChildrenTestCases.AllowedChildrenWithMaxLimit.class, "handlers/common/maxChildren/withAllowedChildren");
    }

}
