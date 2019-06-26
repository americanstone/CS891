package edu.vandy.simulator.managers.beings.completionService;

import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import edu.vandy.simulator.Controller;
import edu.vandy.simulator.managers.beings.Being;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.utils.Assignment;
import edu.vandy.simulator.utils.ExceptionUtils;

import static edu.vandy.simulator.utils.ExceptionUtils.rethrowSupplier;
import static java.util.stream.Collectors.toList;

/**
 * This BeingManager implementation uses the Java
 * ExecutorCompletionService to create a cached pool of Java threads
 * that run being simulations.
 */
public class ExecutorCompletionServiceMgr
        extends BeingManager<BeingCallable> {
    /**
     * Used for Android debugging.
     */
    private final static String TAG =
            ExecutorCompletionServiceMgr.class.getName();

    /**
     * The ExecutorService contains a cached pool of threads.
     */
    // TODO -- you fill in here.
    private ExecutorService mExecutorService;
    /**
     * The CompletionService that's associated with the
     * ExecutorService above.
     */
    // TODO -- you fill in here.
    CompletionService<BeingCallable> mCompletionService;
    /**
     * Default constructor.
     */
    public ExecutorCompletionServiceMgr() {
    }

    /**
     * Resets the fields to their initial values and tells all beings
     * to reset themselves.
     */
    @Override
    public void reset() {
        super.reset();
    }

    /**
     * Abstract method that BeingManagers implement to return a new
     * BeingCallable instance.
     *
     * @return A new typed Being instance.
     */
    @Override
    public BeingCallable newBeing() {
        // Return a new BeingCallable instance.
        // TODO -- you fill in here, replacing null with the
        // appropriate code.
        return new BeingCallable(this);
    }

    /**
     * This entry point method is called by the Simulator framework to
     * start the being gazing simulation.
     **/
    @Override
    public void runSimulation() {
        // Call a method that uses the ExecutorService to create/start
        // a pool of threads that represent the beings in this
        // simulation.
        // TODO -- you fill in here.
        beginBeingThreadPool();

        // Call a method that waits for all futures to complete.
        // TODO -- you fill in here.
        awaitCompletionOfFutures();
        // Call this classes ExecutorServiceMgr shutdownNow() method
        // to cleanly shutdown the executor service.
        // TODO -- you fill in here.
        shutdownNow();
    }

    /**
     * This factory method creates a cached thread pool executor
     * service.
     *
     * @return A cached thread pool executor
     */
    public ExecutorService createExecutorService() {
        // TODO -- you fill in here (replace null with an executor service instance).
        return Executors.newCachedThreadPool();
    }

    /**
     * This factory method creates an ExecutorCompletionService
     * that's associated with the created ExecutorService.
     *
     * @return An instance of ExecutorCompletionService
     */
    public CompletionService<BeingCallable> createExecutorCompletionService(
            ExecutorService executorService) {
        // TODO -- you fill in here (replace null with an executor
        // completion service instance).
        return new ExecutorCompletionService(executorService);
    }

    /**
     * Use the ExecutorService to create/start a pool of threads that
     * represent the beings in this simulation.
     */
    public void beginBeingThreadPool() {
        // Create an ExecutorService instance that contains a cached
        // pool of threads.  Call the BeingManager.getBeings() method
        // to iterate through the BeingCallables and submit each
        // BeingCallable to the ExecutorCompletionService.

        // TODO -- you fill in here.
        mExecutorService = createExecutorService();
        mCompletionService = createExecutorCompletionService(mExecutorService);
        List<Future<BeingCallable>> xxx = this.getBeings().stream()
                .map(mCompletionService::submit)
                .collect(toList());
    }

    /**
     * Wait for all the futures to complete.
     */
    public void awaitCompletionOfFutures() {
        // All STUDENTS:
        // Use a for loop to iterate through the total number of
        // beings, blocking on the ExecutorCompletionService take()
        // method until there's a completed future available.  Then
        // get the result from the future and use it to tell the
        // Controller log when a simulation completes normally.  If an
        // exception is thrown, however, then tell the Controller log
        // which exception was caught and break out of the loop.

        // GRADUATE STUDENTS:
        // Implement the same logic as above, but use the
        // ExceptionUtils rethrowSupplier() method to avoid the need
        // for a try/catch block to handle checked exceptions. Make
        // sure that your solution does not contain any try/catch blocks.

        if (Assignment.isUndergraduateTodo()) {
            // TODO -- you fill in here.
            for(int i = 0;  i < getBeingCount(); i++){
                try {
                    Future<BeingCallable> result = mCompletionService.take();
                    BeingCallable bc = result.get();
                    Controller.log(TAG, bc);
                } catch (RuntimeException | InterruptedException | ExecutionException e) {
                    Controller.log(TAG,  e);
                    break;
                }
            }

        } else if (Assignment.isGraduateTodo()) {
            // TODO -- you fill in here.
            for(int i = 0;  i < getBeingCount(); i++){
                Future<BeingCallable> result = ExceptionUtils.uncheck(() -> mCompletionService.take());
                BeingCallable bc = ExceptionUtils.uncheck(() -> result.get());
                Controller.log(TAG, bc);
            }
        }
        else {
            throw new IllegalStateException("Invalid assignment type");
        }

        // Print the number of beings that were processed.
        Controller.log(TAG +
                ": awaitCompletionOfFutures: exiting with "
                + getRunningBeingCount());
    }

    /**
     * Called to terminate the executor service. This method should
     * only return after all threads have been terminated and all
     * resources cleaned up.
     */
    @Override
    public void shutdownNow() {
        Controller.log(TAG + ": shutdownNow: entered");

        // Shutdown the executor *now*.
        // TODO -- you fill in here.
        mExecutorService.shutdownNow();
        Controller.log(TAG + ": shutdownNow: exited with "
                + getRunningBeingCount() + "/"
                + getBeingCount() + " running beings.");
    }
}
