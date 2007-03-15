package org.openspaces.itest.core.space.filter.adapter;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceLateContext;
import org.openspaces.core.space.filter.BeforeWrite;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class SimpleFilter {

    @GigaSpaceLateContext
    GigaSpace gigaSpace;

    private List<Object[]> lastExecutions = new ArrayList();

    @BeforeWrite
    public void beforeWrite(Message entry) {
        lastExecutions.add(new Object[] {entry});
    }

    public List<Object[]> getLastExecutions() {
        return this.lastExecutions;
    }

    public void clearExecutions() {
        this.lastExecutions.clear();
    }
}