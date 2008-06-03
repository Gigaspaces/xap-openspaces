package org.openspaces.itest.core.space.support;

import com.j_spaces.core.IJSpace;
import com.j_spaces.worker.IWorker;

/**
 * @author kimchy
 */
public class MyWorker implements IWorker {

    private volatile boolean initCalled;
    private volatile boolean runCalled;
    private volatile boolean closeCalled;

    public void init(IJSpace proxy, String workerName, String arg) throws Exception {
        initCalled = true;
    }

    public void close() {
        closeCalled = true;
    }

    public void run() {
        runCalled = true;
    }

    public boolean isInitCalled() {
        return initCalled;
    }

    public boolean isRunCalled() {
        return runCalled;
    }

    public boolean isCloseCalled() {
        return closeCalled;
    }
}
