/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.aem.toolkit.it.base;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageSuite extends Runner {

    private static final Logger LOG = LoggerFactory.getLogger(PackageSuite.class);

    private static final String PROPERTY_NO_UNINSTALL = "nouninstall";

    private final Class<?> suiteClass;

    public PackageSuite(Class<?> suiteClass) {
        this.suiteClass = suiteClass;
    }

    @Override
    public Description getDescription() {
        return Description.createSuiteDescription(suiteClass);
    }

    @Override
    public void run(RunNotifier notifier) {
        Suite.SuiteClasses suite = suiteClass.getAnnotation(Suite.SuiteClasses.class);
        if (suite == null || ArrayUtils.isEmpty(suite.value())) {
            LOG.info("Tests not found");
            return;
        }

        notifier.fireTestSuiteStarted(Description.createSuiteDescription(suiteClass));
        runServiceMethod(BeforeClass.class);

        Package contentPackage = new Package();
        List<Object> testObjects = prepareTestClasses(suite, contentPackage);

        try {
            PackageInstallerUtil.install(contentPackage);
        } catch (HttpException e) {
            LOG.error("Error uploading test package. Tests canceled", e);
            return;
        }

        for (Object testObject : testObjects) {
            run(notifier, testObject);
        }

        if (Boolean.TRUE.toString().equals(System.getProperty(PROPERTY_NO_UNINSTALL))) {
            return;
        }

        try {
            PackageInstallerUtil.uninstall(contentPackage);
            contentPackage.close();
        } catch (HttpException e) {
            LOG.error("Error removing test package. You may need to remove it manually", e);
        } catch (IOException e) {
            LOG.warn("Error finalizing test package", e);
        }

        runServiceMethod(AfterClass.class);
        notifier.fireTestSuiteFinished(Description.createSuiteDescription(suiteClass));
    }

    private void runServiceMethod(Class<? extends Annotation> annotationClass) {
        Method serviceMethod = Arrays.stream(suiteClass.getDeclaredMethods())
            .filter(method -> Modifier.isStatic(method.getModifiers()))
            .filter(method -> method.isAnnotationPresent(BeforeClass.class))
            .findFirst()
            .orElse(null);
        if (serviceMethod == null) {
            return;
        }
        try {
            serviceMethod.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.error("Could not run @{} method", annotationClass.getSimpleName(), e);
        }
    }

    private static List<Object> prepareTestClasses(Suite.SuiteClasses suite, Package contentPackage) {
        List<Object> result = new ArrayList<>();
        for (Class<?> testClass : suite.value()) {
            try {
                result.add(testClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                LOG.warn("Could not create an instance of {}", testClass, e);
                continue;
            }
            if (testClass.getAnnotation(Render.class) != null) {
                Arrays.stream(testClass.getAnnotation(Render.class).value()).forEach(contentPackage::includeRender);
            }
        }
        return result;
    }

    private static void run(RunNotifier notifier, Object testObject) {
        Class<?> testClass = testObject.getClass();
        for (Method method : testClass.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())
                || Modifier.isStatic(method.getModifiers())
                || !method.isAnnotationPresent(Test.class)) {
                continue;
            }

            notifier.fireTestStarted(Description.createTestDescription(testClass, method.getName()));
            try {
                method.invoke(testObject);
                notifier.fireTestFinished(Description.createTestDescription(testClass, method.getName()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.warn("Error running {}#{}", testClass, method.getName(), e);
                notifier.fireTestFailure(new Failure(
                    Description.createTestDescription(testClass, method.getName()),
                    e.getCause()
                ));
            }
        }
    }
}
