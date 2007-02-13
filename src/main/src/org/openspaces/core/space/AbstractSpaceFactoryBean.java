package org.openspaces.core.space;

import com.gigaspaces.cluster.activeelection.ISpaceModeListener;
import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.GigaSpaceException;
import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.rmi.RemoteException;

/**
 * <p>Base class for most space factory beans responsible for creating/finding
 * {@link com.j_spaces.core.IJSpace} implementation.
 *
 * <p>Derived classes should implement the {@link #doCreateSpace()} to obtain the
 * {@link com.j_spaces.core.IJSpace}.
 *
 * @author kimchy
 */
public abstract class AbstractSpaceFactoryBean implements InitializingBean, DisposableBean, FactoryBean, ApplicationContextAware {

    protected Log logger = LogFactory.getLog(getClass());

    private IJSpace space;

    private ApplicationContext applicationContext;


    private SpaceMode currentSpaceMode;

    private PrimaryBackupListener primaryBackupListener;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Initializes the space by calling the {@link #doCreateSpace()}.
     */
    public void afterPropertiesSet() throws GigaSpaceException {
        this.space = doCreateSpace();
        primaryBackupListener = new PrimaryBackupListener();
        // register the space mode listener with the space
        // TODO Currently it fails when single space, will be fixed tomorrow
        // TODO What happens with clustered proxy (but one that actually created the cluster memeber), will it register on the current cluster member, or should we handle it similar to the clustered flag
        // TODO What happens with clustered proxy (one that DID NOT created any cluster member). We should get only PRIMARY event on it and that is it.
//        try {
//            ISpaceModeListener remoteListener = (ISpaceModeListener) space.getStubHandler().exportObject(primaryBackupListener);
//            currentSpaceMode = ((IInternalRemoteJSpaceAdmin) space.getAdmin()).addSpaceModeListener(remoteListener);
//            if (logger.isDebugEnabled()) {
//                logger.debug("Space [" + space + "] mode is [" + currentSpaceMode + "]");
//            }
//        } catch (RemoteException e) {
//            throw new CannotCreateSpaceException("Failed to regsiter space mode listener with space [" + space + "]", e);
//        }
    }

    public void destroy() throws Exception {
//        try {
//            ISpaceModeListener remoteListener = (ISpaceModeListener) space.getStubHandler().exportObject(primaryBackupListener);
//            ((IInternalRemoteJSpaceAdmin) space.getAdmin()).removeSpaceModeListener(remoteListener);
//        } catch (RemoteException e) {
//            logger.warn("Failed to unregister space mode listener with space [" + space + "]", e);
//        }
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

    private class PrimaryBackupListener implements ISpaceModeListener {

        public void beforeSpaceModeChange(SpaceMode spaceMode) throws RemoteException {
            currentSpaceMode = spaceMode;
            if (logger.isDebugEnabled()) {
                logger.debug("Space [" + space + "] mode is [" + currentSpaceMode + "]");
            }
            if (applicationContext != null) {
                applicationContext.publishEvent(new BeforeSpaceModeChangeEvent(space, spaceMode));
            }
        }

        public void afterSpaceModeChange(SpaceMode spaceMode) throws RemoteException {
            currentSpaceMode = spaceMode;
            if (logger.isDebugEnabled()) {
                logger.debug("Space [" + space + "] mode is [" + currentSpaceMode + "]");
            }
            if (applicationContext != null) {
                applicationContext.publishEvent(new AfterSpaceModeChangeEvent(space, spaceMode));
            }
        }
    }
}
