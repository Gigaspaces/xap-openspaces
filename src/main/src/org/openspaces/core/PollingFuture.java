package org.openspaces.core;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface PollingFuture<T> {
	/**
	 * @return the result, or one of the following exceptions
	 * @throws ExecutionException - execution failed and an exception was raised during work
	 * @throws IllegalStateException - execution not done yet. Call this method only when @{link {@link #isDone()} returns true. 
	 * @throws TimeoutException - execution timed out and result is not available.
	 */
	T get() throws ExecutionException, IllegalStateException , TimeoutException;
    
	/**
	 * @return true if execution completed successfully, with an exception or timed-out. If it is still in progress returns null.
	 */
    boolean isDone();
    
    /**
     * @return true if execution timed out.
     */
    boolean isTimedOut();

    /**
     * @return the execption raised during execution wraped as an ExecutionException or null if in progress or no exception was raised. 
     */
    ExecutionException getException();
    
    Date getTimestamp();
}