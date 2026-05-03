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
package com.exadel.aem.toolkit.core.utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;

public class ServiceUtilTest {

    private static final String DEFAULT_VALUE = "fallback";
    private static final String STUB_VALUE = "stub-value";

    private static final String TEST_EXCEPTION_MESSAGE = "Something went wrong";

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    @Test
    public void shouldInvokeConsumerAndReturnFunctionResult() {
        AtomicReference<StubService> capturedService = new AtomicReference<>();
        StubService service = new StubServiceImpl();
        context.registerService(StubService.class, service);

        ServiceUtil.withService(StubService.class, context.bundleContext(), capturedService::set);
        assertEquals(service, capturedService.get());

        String functionResult = ServiceUtil.withService(
            StubService.class,
            context.bundleContext(),
            StubService::getValue,
            DEFAULT_VALUE);
        assertEquals(STUB_VALUE, functionResult);
    }

    @Test
    public void shouldSkipExecutionWhenServiceUnavailable() {
        AtomicBoolean consumerInvoked = new AtomicBoolean(false);

        // No service registered, so getServiceReference returns null
        ServiceUtil.withService(StubService.class, context.bundleContext(), s -> consumerInvoked.set(true));
        assertFalse(consumerInvoked.get());

        String result1 = ServiceUtil.withService(
            StubService.class,
            context.bundleContext(),
            StubService::getValue,
            DEFAULT_VALUE);
        assertEquals(DEFAULT_VALUE, result1);

        // Non-null reference but getService returns null
        BundleContext mockContext = newMockContextWithNullService();
        ServiceUtil.withService(StubService.class, mockContext, s -> consumerInvoked.set(true));
        assertFalse(consumerInvoked.get());

        String result2 = ServiceUtil.withService(
            StubService.class,
            mockContext,
            StubService::getValue,
            DEFAULT_VALUE);
        assertEquals(DEFAULT_VALUE, result2);
    }

    @Test
    public void shouldSwallowExceptionsAndReleaseService() {
        // Consumer throws IllegalArgumentException: exception swallowed, service still released in the finally block
        MockContext consumerCtx = newMockContextReturningService();
        ServiceUtil.withService(
            StubService.class,
            consumerCtx.bundleContext,
            s -> { throw new IllegalArgumentException(TEST_EXCEPTION_MESSAGE); });
        Mockito.verify(consumerCtx.bundleContext).ungetService(consumerCtx.serviceRef);

        // Consumer throws generic RuntimeException: exception also swallowed
        MockContext consumerRteCtx = newMockContextReturningService();
        ServiceUtil.withService(
            StubService.class,
            consumerRteCtx.bundleContext,
            s -> { throw new RuntimeException(TEST_EXCEPTION_MESSAGE); });
        Mockito.verify(consumerRteCtx.bundleContext).ungetService(consumerRteCtx.serviceRef);

        // Function throws IllegalStateException: returns default, service still released in the finally block
        MockContext functionCtx = newMockContextReturningService();
        String result = ServiceUtil.withService(
            StubService.class,
            functionCtx.bundleContext,
            s -> { throw new IllegalStateException(TEST_EXCEPTION_MESSAGE); },
            DEFAULT_VALUE);
        assertEquals(DEFAULT_VALUE, result);
        Mockito.verify(functionCtx.bundleContext).ungetService(functionCtx.serviceRef);

        // Function throws generic RuntimeException: also returns default
        MockContext functionRteCtx = newMockContextReturningService();
        String result2 = ServiceUtil.withService(
            StubService.class,
            functionRteCtx.bundleContext,
            s -> { throw new RuntimeException(TEST_EXCEPTION_MESSAGE); },
            DEFAULT_VALUE);
        assertEquals(DEFAULT_VALUE, result2);
        Mockito.verify(functionRteCtx.bundleContext).ungetService(functionRteCtx.serviceRef);

        // UngetService itself throws: exception is swallowed, prior consumer side effects are preserved
        MockContext ungetCtx = newMockContextThrowingOnUnget();
        AtomicBoolean consumerInvoked = new AtomicBoolean(false);
        ServiceUtil.withService(StubService.class, ungetCtx.bundleContext, s -> consumerInvoked.set(true));
        assertTrue(consumerInvoked.get());
    }

    @Test
    public void shouldHandleMissingBundleContext() {
        AtomicBoolean consumerInvoked = new AtomicBoolean(false);

        // FrameworkUtil.getBundle() returns null outside an OSGi container: logs error, consumer not invoked
        ServiceUtil.withService(StubService.class, s -> consumerInvoked.set(true));
        assertFalse(consumerInvoked.get());

        String result = ServiceUtil.withService(StubService.class, StubService::getValue, DEFAULT_VALUE);
        assertEquals(DEFAULT_VALUE, result);
    }

    private static BundleContext newMockContextWithNullService() {
        BundleContext mockContext = Mockito.mock(BundleContext.class);
        @SuppressWarnings("unchecked")
        ServiceReference<StubService> mockRef = Mockito.mock(ServiceReference.class);
        Mockito.when(mockContext.getServiceReference(StubService.class)).thenReturn(mockRef);
        Mockito.when(mockContext.getService(mockRef)).thenReturn(null);
        return mockContext;
    }

    private static MockContext newMockContextReturningService() {
        BundleContext mockContext = Mockito.mock(BundleContext.class);
        @SuppressWarnings("unchecked")
        ServiceReference<StubService> mockRef = Mockito.mock(ServiceReference.class);
        Mockito.when(mockContext.getServiceReference(StubService.class)).thenReturn(mockRef);
        Mockito.when(mockContext.getService(mockRef)).thenReturn(new StubServiceImpl());
        return new MockContext(mockContext, mockRef);
    }

    private static MockContext newMockContextThrowingOnUnget() {
        BundleContext mockContext = Mockito.mock(BundleContext.class);
        @SuppressWarnings("unchecked")
        ServiceReference<StubService> mockRef = Mockito.mock(ServiceReference.class);
        Mockito.when(mockContext.getServiceReference(StubService.class)).thenReturn(mockRef);
        Mockito.when(mockContext.getService(mockRef)).thenReturn(new StubServiceImpl());
        Mockito.when(mockContext.ungetService(mockRef)).thenThrow(new IllegalStateException("Unget failed"));
        return new MockContext(mockContext, mockRef);
    }

    private interface StubService {
        String getValue();
    }

    private static class StubServiceImpl implements StubService {
        @Override
        public String getValue() {
            return STUB_VALUE;
        }
    }

    private static class MockContext {
        final BundleContext bundleContext;
        final ServiceReference<StubService> serviceRef;

        MockContext(BundleContext bundleContext, ServiceReference<StubService> serviceRef) {
            this.bundleContext = bundleContext;
            this.serviceRef = serviceRef;
        }
    }
}
