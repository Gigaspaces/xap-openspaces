package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;
import org.openspaces.core.GigaSpaceException;

/**
 * A direct space factory bean, initalized with an existing {@link IJSpace} and provides it as the
 * space.
 * 
 * <p>
 * Though mostly not relevant for xml based configuration, this might be relevant when using
 * programmatic configuration.
 * 
 * @author kimchy
 * @see UrlSpaceFactoryBean
 */
public class DirectSpaceFactoryBean extends AbstractSpaceFactoryBean {

    private IJSpace space;

    /**
     * Constucts a new direct space factory using the provided space.
     * 
     * @param space
     *            The space to use
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
