package com.exadel.aem.toolkit.core.maven;

import com.exadel.aem.toolkit.core.util.TestHelper;

class ExceptionTestBase extends DefaultTestBase {
    private static final String EXCEPTION_SETTING = "all";

    @Override
    String getExceptionSetting() {
        return EXCEPTION_SETTING;
    }

    @Override
    void testComponent(Class<?> tested) {
        try {
            TestHelper.doTest(tested.getName(), null);
        } catch (ClassNotFoundException ex) {
            LOG.error("Cannot initialize instance of class " + tested.getName(), ex);
        }
    }
}
