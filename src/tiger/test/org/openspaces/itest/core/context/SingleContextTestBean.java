package org.openspaces.itest.core.context;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;

/**
 * A simple test bean that verifies direct {@link org.openspaces.core.GigaSpace} injection
 * using {@link org.openspaces.core.context.GigaSpaceContext}.
 *
 * @author kimchy
 */
public class SingleContextTestBean {

    @GigaSpaceContext
    GigaSpace gs1;

    GigaSpace gs2;

    @GigaSpaceContext
    public void setGs2(GigaSpace gs2) {
        this.gs2 = gs2;
    }
}
