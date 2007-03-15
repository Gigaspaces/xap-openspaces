package org.openspaces.itest.core.space.filter.adapter;

import com.j_spaces.core.filters.entry.ISpaceFilterEntry;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceLateContext;
import org.openspaces.core.space.filter.AfterWrite;
import org.openspaces.core.space.filter.BeforeRead;
import org.openspaces.core.space.filter.BeforeTake;
import org.openspaces.core.space.filter.BeforeWrite;
import org.openspaces.core.space.filter.OnFilterClose;
import org.openspaces.core.space.filter.OnFilterInit;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class SimpleFilter {

    @GigaSpaceLateContext
    GigaSpace gigaSpace;

    private List<Object[]> lastExecutions = new ArrayList<Object[]>();

    private boolean onInitCalled;
    private boolean onCloseCalled;

    public boolean isOnInitCalled() {
        return onInitCalled;
    }

    public boolean isOnCloseCalled() {
        return onCloseCalled;
    }

    public List<Object[]> getLastExecutions() {
        return this.lastExecutions;
    }

    @OnFilterInit
    void onInit() {
        onInitCalled = true;
    }

    @OnFilterClose
    void onClose() {
        onCloseCalled = true;
    }

    @BeforeWrite
    public void beforeWrite(Message entry) {
        lastExecutions.add(new Object[] {entry});
    }

    @AfterWrite
    public void afterWrite(Echo echo) {
        lastExecutions.add(new Object[] {echo});
    }

    @BeforeRead
    public void beforeRead(ISpaceFilterEntry entry) {
        lastExecutions.add(new Object[] {entry});
    }

    @BeforeTake
    public void beforeTake(Message entry, int operationCode) {
        lastExecutions.add(new Object[] {entry, operationCode});
    }

    public void clearExecutions() {
        this.lastExecutions.clear();
    }
}