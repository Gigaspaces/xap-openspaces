package org.openspaces.itest.core.space.filter;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.core.filters.ISpaceFilter;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceLateContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kimchy
 */
public class SimpleFilter implements ISpaceFilter {

    private Map<Integer, Integer> stats = new HashMap<Integer, Integer>();

    @GigaSpaceLateContext
    GigaSpace gigaSpace;

    public void init(IJSpace space, String filterId, String url, int priority) throws RuntimeException {

    }

    public void process(SpaceContext context, ISpaceFilterEntry entry, int operationCode) throws RuntimeException {
        Integer counter = stats.get(operationCode);
        if (counter == null) {
            counter = 1;
        } else {
            counter = counter + 1;
        }
        stats.put(operationCode, counter);
    }

    public void process(SpaceContext context, ISpaceFilterEntry[] entries, int operationCode) throws RuntimeException {
        Integer counter = stats.get(operationCode);
        if (counter == null) {
            counter = 1;
        } else {
            counter = counter + entries.length;
        }
        stats.put(operationCode, counter);
    }

    public void close() throws RuntimeException {
    }

    public Map<Integer, Integer> getStats() {
        return stats;
    }
}
