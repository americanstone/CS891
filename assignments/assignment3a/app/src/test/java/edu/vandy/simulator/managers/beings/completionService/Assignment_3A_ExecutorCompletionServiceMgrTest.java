package edu.vandy.simulator.managers.beings.completionService;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import admin.AssignmentTests;
import admin.ReflectionHelper;
import edu.vandy.simulator.utils.Assignment;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Assignment_3A_ExecutorCompletionServiceMgrTest extends AssignmentTests {
    @Rule
    public final Timeout timeout = new Timeout(10, SECONDS);
    private final int BEING_COUNT = 5;
    @Mock
    public CompletionService<BeingCallable> mCompletionServiceMock;
    @Mock
    public ExecutorService mExecutorMock;
    @Mock
    Future<BeingCallable> mFutureMock;
    @InjectMocks
    private ExecutorCompletionServiceMgr mManagerMock =
            mock(ExecutorCompletionServiceMgr.class);

    // Used to count lambda errors caught by try/catch block
    private int mErrorCount = 0;

    @Test
    public void testNewBeing() {
        doCallRealMethod().when(mManagerMock).newBeing();

        // Call SUT.
        BeingCallable beingCallable = mManagerMock.newBeing();

        assertNotNull("newBeing should not return null.", beingCallable);
    }

    @Test
    public void testRunSimulation() {
        doCallRealMethod().when(mManagerMock).runSimulation();

        // Call SUT.
        mManagerMock.runSimulation();

        InOrder inOrder = inOrder(mManagerMock);

        verify(mManagerMock).beginBeingThreadPool();
        verify(mManagerMock).awaitCompletionOfFutures();
        verify(mManagerMock).shutdownNow();

        inOrder.verify(mManagerMock).beginBeingThreadPool();
        inOrder.verify(mManagerMock).awaitCompletionOfFutures();
        inOrder.verify(mManagerMock).shutdownNow();
    }

    @Test
    public void testClassFieldsExist() throws Exception {
        ExecutorCompletionServiceMgr instance = new ExecutorCompletionServiceMgr();
        ReflectionHelper.injectValueIntoFirstMatchingField(instance, null, CompletionService.class);
        ReflectionHelper.injectValueIntoFirstMatchingField(instance, null, ExecutorService.class);
    }

    @Test
    public void testCreateExecutorService() {
        when(mManagerMock.createExecutorService()).thenCallRealMethod();
        ExecutorService executorService = mManagerMock.createExecutorService();
        assertNotNull(executorService);
    }

    @Test
    public void testCreateExecutorCompletionService() {
        when(mManagerMock.createExecutorCompletionService(any())).thenCallRealMethod();
        CompletionService<BeingCallable> completionService =
                mManagerMock.createExecutorCompletionService(mExecutorMock);
        assertNotNull(completionService);
    }

    /**
     * Test common to both UNDERGRADUATE And GRADUATE Assignments.
     */
    @Test
    public void testBeginBeingThreadPool() {
        List<BeingCallable> mockBeings = createMockBeingList(BEING_COUNT);
        when(mManagerMock.createExecutorService()).thenReturn(mExecutorMock);
        when(mManagerMock.createExecutorCompletionService(mExecutorMock)).thenReturn(mCompletionServiceMock);
        when(mManagerMock.getBeings()).thenReturn(mockBeings);
        when(mCompletionServiceMock.submit(any(BeingCallable.class))).thenReturn(mFutureMock);
        doCallRealMethod().when(mManagerMock).beginBeingThreadPool();

        // Call SUT.
        mManagerMock.beginBeingThreadPool();

        verify(mManagerMock, times(1)).createExecutorService();
        verify(mManagerMock, times(1)).createExecutorCompletionService(mExecutorMock);
        verify(mManagerMock, times(1)).getBeings();
        verify(mCompletionServiceMock, times(mockBeings.size())).submit(any(BeingCallable.class));

        ReflectionHelper.findFirstMatchingFieldValue(mManagerMock, CompletionService.class);
        ReflectionHelper.findFirstMatchingField(mManagerMock, ExecutorService.class);
    }

    /**
     * Test common to both UNDERGRADUATE And GRADUATE Assignments.
     */
    @Test
    public void testAwaitCompletionOfFuturesUndergraduates() throws Exception {
        BeingCallable beingCallableMock = mock(BeingCallable.class);
        List<BeingCallable> mockBeings = createMockBeingList(BEING_COUNT);
        when(mManagerMock.getBeings()).thenReturn(mockBeings);
        when(mManagerMock.getBeingCount()).thenReturn(mockBeings.size());
        when(mCompletionServiceMock.take()).thenReturn(mFutureMock);
        when(mFutureMock.get()).thenReturn(beingCallableMock);

        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mExecutorMock, ExecutorService.class);
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mCompletionServiceMock, CompletionService.class);

        doCallRealMethod().when(mManagerMock).awaitCompletionOfFutures();

        // Call SUT
        mManagerMock.awaitCompletionOfFutures();

        verify(mManagerMock, atLeastOnce()).getBeingCount();
        verify(mCompletionServiceMock, times(BEING_COUNT)).take();
        verify(mFutureMock, times(BEING_COUNT)).get();
    }

    @Test
    public void testAwaitCompletionOfFuturesWithExceptionUndergraduates() throws Exception {
        if (!Assignment.testType(Assignment.UNDERGRADUATE)) {
            System.out.println("Skipping testAwaitCompletionOfFutures (undergraduate only test)");
            return;
        }

        List<BeingCallable> mockBeings = createMockBeingList(BEING_COUNT);
        when(mManagerMock.getBeings()).thenReturn(mockBeings);
        when(mManagerMock.getBeingCount()).thenReturn(mockBeings.size());
        when(mCompletionServiceMock.take()).thenReturn(mFutureMock);
        when(mFutureMock.get()).thenThrow(new RuntimeException("Mock exception"));

        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mExecutorMock, ExecutorService.class);
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mCompletionServiceMock, CompletionService.class);

        doCallRealMethod().when(mManagerMock).awaitCompletionOfFutures();

        try {
            // Call SUT
            mManagerMock.awaitCompletionOfFutures();
        } catch (Throwable e) {
            fail("Method should not throw an exception");
        }

        verify(mManagerMock, atLeastOnce()).getBeingCount();
        verify(mCompletionServiceMock, times(1)).take();
        verify(mFutureMock, times(1)).get();
    }

    @Test
    public void testAwaitCompletionOfFuturesWithExceptionGraduates() throws Exception {
        if (!Assignment.testType(Assignment.GRADUATE)) {
            System.out.println("Skipping testAwaitCompletionOfFutures (graduate only test)");
            return;
        }

        List<BeingCallable> mockBeings = createMockBeingList(BEING_COUNT);
        when(mManagerMock.getBeings()).thenReturn(mockBeings);
        when(mManagerMock.getBeingCount()).thenReturn(mockBeings.size());
        when(mCompletionServiceMock.take()).thenReturn(mFutureMock);
        when(mFutureMock.get()).thenThrow(new RuntimeException("Mock exception"));

        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mExecutorMock, ExecutorService.class);
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mCompletionServiceMock, CompletionService.class);

        doCallRealMethod().when(mManagerMock).awaitCompletionOfFutures();

        try {
            // Call SUT
            mManagerMock.awaitCompletionOfFutures();
            fail("Method should throw an exception");
        } catch (RuntimeException e) {
        }

        verify(mManagerMock, atLeastOnce()).getBeingCount();
        verify(mCompletionServiceMock, times(1)).take();
        verify(mFutureMock, times(1)).get();
    }

    @Test
    public void testShutdownNow() throws IllegalAccessException {
        ReflectionHelper.injectValueIntoFirstMatchingField(
                mManagerMock, mExecutorMock, ExecutorService.class);

        doCallRealMethod().when(mManagerMock).shutdownNow();

        // Call SUT.
        mManagerMock.shutdownNow();

        verify(mExecutorMock, times(1)).shutdownNow();
    }

    private List<Future<BeingCallable>> createMockFutureList(int count, boolean mockGet) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(unused -> {
                    @SuppressWarnings("unchecked")
                    Future<BeingCallable> futureMock =
                            (Future<BeingCallable>) mock(Future.class);
                    try {
                        if (mockGet) {
                            when(futureMock.get()).thenReturn(mock(BeingCallable.class));
                        }
                    } catch (Exception e) {
                    }
                    return futureMock;
                })
                .collect(Collectors.toList());
    }

    private List<BeingCallable> createMockBeingList(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(unused -> {
                    BeingCallable being = mock(BeingCallable.class);
                    // Mockito says this is never used.
                    // when(being.call()).thenReturn(being);
                    return being;
                })
                .collect(Collectors.toList());
    }
}
