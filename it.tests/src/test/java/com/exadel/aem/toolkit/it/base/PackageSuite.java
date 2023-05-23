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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements JUnit {@link Runner} pattern to facilitate creating a content package with test data, deploying it to an
 * AEM server, running multiple test cases, and, optionally, cleaning up the deployed content
 */
public class PackageSuite extends Runner {

    private static final Logger LOG = LoggerFactory.getLogger(PackageSuite.class);

    private static final String PROPERTY_NO_UNINSTALL = "nouninstall";

    private final Class<?> suiteClass;

    /**
     * Creates a new {@link PackageSuite} instance
     * @param suiteClass The class that represents an entry point for test cases
     */
    public PackageSuite(Class<?> suiteClass) {
        this.suiteClass = suiteClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescription() {
        return Description.createSuiteDescription(suiteClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(RunNotifier notifier) {
        Suite.SuiteClasses suite = suiteClass.getAnnotation(Suite.SuiteClasses.class);
        if (suite == null || ArrayUtils.isEmpty(suite.value())) {
            LOG.info("Tests not found");
            return;
        }

        notifier.fireTestSuiteStarted(Description.createSuiteDescription(suiteClass));
        runServiceMethod(suiteClass, BeforeClass.class);

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

        runServiceMethod(suiteClass, AfterClass.class);
        notifier.fireTestSuiteFinished(Description.createSuiteDescription(suiteClass));
    }

    /**
     * Searches for a specially annotated method that manifests a test running stage, such as {@link BeforeClass}, and
     * runs it. This method is intended for running stateless (static) methods of the target class
     * @param target         The class in which the method is expected to be
     * @param annotationType The type of annotation that signifies the method
     */
    private static void runServiceMethod(Class<?> target, Class<? extends Annotation> annotationType) {
        Method serviceMethod = Arrays.stream(target.getDeclaredMethods())
            .filter(method -> Modifier.isStatic(method.getModifiers()))
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .filter(method -> method.isAnnotationPresent(annotationType))
            .findFirst()
            .orElse(null);
        if (serviceMethod == null) {
            return;
        }
        try {
            serviceMethod.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.error("Could not run @{} method", annotationType.getSimpleName(), e);
        }
    }

    /**
     * Searches for a specially annotated method that manifests a test running stage, such as {@link Before}, and runs
     * it. This method is intended for running state-dependent (instance) methods of the target class
     * @param target         The class in which the method is expected to be
     * @param annotationType The type of annotation that signifies the method
     */
    private static void runServiceMethod(Object target, Class<? extends Annotation> annotationType) {
        Class<?> targetClass = target.getClass();
        Method serviceMethod = Arrays.stream(targetClass.getDeclaredMethods())
            .filter(method -> !Modifier.isStatic(method.getModifiers()))
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .filter(method -> method.isAnnotationPresent(annotationType))
            .findFirst()
            .orElse(null);
        if (serviceMethod == null) {
            return;
        }
        try {
            serviceMethod.invoke(target);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.error("Could not run @{} method", annotationType.getSimpleName(), e);
        }
    }

    /**
     * Creates instances of classes that are included in the current suite and renders Granite dialogs/config files for
     * them if necessary
     * @param suite          The {@link Suite.SuiteClasses} object that enumerates classes
     * @param contentPackage {@link Package} instance
     * @return A non-null list of initialized objects; can be empty
     */
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

    /**
     * Runs the particular test case class
     * @param notifier   {@link RunNotifier} object responsible for informing subscribers on the test process
     * @param testObject A class instance that is being tested right now
     */
    private static void run(RunNotifier notifier, Object testObject) {
        Class<?> testClass = testObject.getClass();
        runServiceMethod(testClass, BeforeClass.class);
        List<Method> testMethods = Arrays.stream(testClass.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Test.class))
            .sorted(PackageSuite::compareMethods)
            .collect(Collectors.toList());

        for (Method method : testMethods) {
            notifier.fireTestStarted(Description.createTestDescription(testClass, method.getName()));
            runServiceMethod(testObject, Before.class);
            try {
                method.invoke(testObject);
                notifier.fireTestFinished(Description.createTestDescription(testClass, method.getName()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.warn("Error running {}#{}", testClass, method.getName(), e);
                notifier.fireTestFailure(new Failure(
                    Description.createTestDescription(testClass, method.getName()),
                    e.getCause()
                ));
                if (method.isAnnotationPresent(Order.class)) {
                    // It makes sense to terminate the testing flow  in case of failure for only ordered methods
                    // (assuming that a subsequent method relies on the current one)
                    break;
                }
            } finally {
                runServiceMethod(testObject, After.class);
            }
        }
        runServiceMethod(testClass, AfterClass.class);
    }

    /**
     * Used to sort test methods by the value of {@link Order} annotation
     * @param first  {@code Method} instance
     * @param second {@code Method instance}
     * @return Int value per the contract of {@link Comparator#compare(Object, Object)}
     */
    private static int compareMethods(Method first, Method second) {
        int firstOrder = first.isAnnotationPresent(Order.class) ? first.getAnnotation(Order.class).value() : 0;
        int secondOrder = second.isAnnotationPresent(Order.class) ? second.getAnnotation(Order.class).value() : 0;
        return firstOrder - secondOrder;
    }
}
