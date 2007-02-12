package org.openspaces.core.space;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.FactoryBean;
import org.openspaces.core.GigaSpaceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.j_spaces.core.IJSpace;

/**
 * <p>Base class for most space factory beans responsible for creating/finding
 * {@link com.j_spaces.core.IJSpace} implementation.
 *
 * <p>Derived classes should implement the {@link #doCreateSpace()} to obtain the
 * {@link com.j_spaces.core.IJSpace}.
 *
 * @author kimchy
 */
public abstract class AbstractSpaceFactoryBean implements InitializingBean, FactoryBean {

    protected Log logger = LogFactory.getLog(getClass());

    private IJSpace space;

    /**
     * Initializes the space by calling the {@link #doCreateSpace()}.
     */
    public void afterPropertiesSet() throws GigaSpaceException {
        this.space = doCreateSpace();
    }

    /**
     * Spring factory bean returning the {@link com.j_spaces.core.IJSpace} created
     * during the bean initializtion ({@link #afterPropertiesSet()}).
     *
     * @return The {@link com.j_spaces.core.IJSpace} implementation
     * @throws Exception
     */
    public Object getObject() throws Exception {
        return this.space;
    }

    /**
     * Returns the object type of the factory bean. Defaults to IJSpace class or the
     * actual {@link com.j_spaces.core.IJSpace} implementation class.
     */
    public Class getObjectType() {
        return (space == null ? IJSpace.class : space.getClass());
    }

    /**
     * Returns <code>true</code> since this factory is a singleton.
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * Responsible for creating/finding the actual {@link com.j_spaces.core.IJSpace}
     * implementation.
     *
     * @return The IJSpace implementation used for the factory bean
     * @throws GigaSpaceException
     */
    protected abstract IJSpace doCreateSpace() throws GigaSpaceException;
}
