package edu.vandy.simulator.managers.palantiri.reentrantLockHashMapSimpleSemaphore;

import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class defines a counting semaphore with "fair" semantics that
 * are implemented using a Java ReentrantLock and ConditionObject.
 */
public class SimpleSemaphore {
    /**
     * Define a count of the number of available permits.
     */
    // TODO - you fill in here.  Ensure that this field will ensure
    // its values aren't cached by multiple threads..
     volatile int mPermits;
    /**
     * Define a ReentrantLock to protect critical sections.
     */
    // TODO - you fill in here
      Lock lock;
    /**
     * Define a Condition that's used to wait while the number of
     * permits is 0.
     */
    // TODO - you fill in here
     Condition notEmpty;
    /**
     * Default constructor used for regression tests.
     */
    public SimpleSemaphore() {
//        this.lock = null;
//        this.notEmpty = null;
    }

    /**
     * Constructor initialize the fields.
     */
    public SimpleSemaphore (int permits) {
        // TODO -- you fill in here making sure the ReentrantLock has
        // "fair" semantics.
        this.lock = new ReentrantLock(true);
        this.mPermits = permits;
        this.notEmpty = lock.newCondition();
    }

    /**
     * Acquire one permit from the semaphore in a manner that can be
     * interrupted.
     */
    public void acquire()
        throws InterruptedException {
        // TODO -- you fill in here, make sure the lock is always
        // released, e.g., even if an exception occurs.
        try{
            lock.lockInterruptibly();;
            while(mPermits == 0)
                notEmpty.await();
            mPermits = mPermits-1;
        }
//        catch (InterruptedException e){
//            Thread.currentThread().interrupt();
//            throw new InterruptedException();
//        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Acquire one permit from the semaphore in a manner that cannot
     * be interrupted.  If an interrupt occurs while this method is
     * running make sure the interrupt flag is reset when the method
     * returns.
     */
    public void acquireUninterruptibly() {
        // TODO -- you fill in here, make sure the lock is always
        // released, e.g., even if an exception occurs.
        try{
            lock.lock();
            while(mPermits == 0)
                notEmpty.awaitUninterruptibly();
            mPermits = mPermits-1;
        }
        finally {
            lock.unlock();

        }
    }

    /**
     * Return one permit to the semaphore.
     */
    public void release() {
        // TODO -- you fill in here, make sure the lock is always
        // released, e.g., even if an exception occurs.
        try {
            lock.lockInterruptibly();
            mPermits = mPermits + 1;
            notEmpty.signal();
            //notEmpty.signalAll();
        }catch (InterruptedException e){
        }finally {
            lock.unlock();
        }
    }

    /**
     * Returns the current number of permits.
     */
    protected int availablePermits() {
        // TODO -- you fill in here, replacing 0 with the
        // appropriate field.
        return mPermits;
    }
}
