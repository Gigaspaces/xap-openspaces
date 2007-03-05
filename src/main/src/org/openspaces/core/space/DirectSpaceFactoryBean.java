package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;
import org.openspaces.core.GigaSpaceException;

/**
 * <p>A direct space factory bean, initalized with an existing {@link com.j_spaces.core.IJSpace}
 * and provides it as the space.
 *
 * <p>Though mostly not relevant for xml based configuration, this might be relevant when using
 * programmatic configuration.
 *
 * @author kimchy
 */
public class DirectSpaceFactoryBean extends AbstractSpaceFactoryBean {

    private IJSpace space;

    /**
     * <p>Constucts a new direct space factory using the provided space.
     *
     * <p>Since this is the only parameter required {@link #afterPropertiesSet()}
     * is called which makes this object ready to be used.
     *
     * @param space The space to use
     */
    public DirectSpaceFactoryBean(IJSpace space) {
        this.space = space;
    }

    /**
     * Returns the space provided in the constructor.
     *
     * @see AbstractSpaceFactoryBean#doCreateSpace()
     */
    protected IJSpace doCreateSpace() throws GigaSpaceException {
        return space;
    }
}
