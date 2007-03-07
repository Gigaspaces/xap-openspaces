package org.openspaces.itest.core.context;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;

/**
 * A test bean that gets injected with two differnt {@link org.openspaces.core.GigaSpace} instances
 * based on the bean name.
 *
 * @author kimchy
 */
public class DoubleContextTestBean {

    @GigaSpaceContext(name = "gs1")
    GigaSpace gs1;

    GigaSpace gs2;

    @GigaSpaceContext(name = "gs2")
    public void setGs2(GigaSpace gs2) {
        this.gs2 = gs2;
    }
}
