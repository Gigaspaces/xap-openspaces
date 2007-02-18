package org.openspaces.core.space;

import com.gigaspaces.cluster.activeelection.ISpaceModeListener;
import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.GigaSpaceException;
import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent;
import org.openspaces.core.util.SpaceUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.rmi.RemoteException;

/**
 * <p>Base class for most space factory beans responsible for creating/finding
 * {@link com.j_spaces.core.IJSpace} implementation.
 *
 * <p>Provides support for raising Spring application events: {@link org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent}
 * and {@link org.openspaces.core.space.mode.AfterSpaceModeChangeEvent} alerting other beans of the current space mode
 * (primary/backup). Beans that wish to be notified of it should implement Spring {@link org.springframework.context.ApplicationListener}.
 * Note that this space mode events might be raised more than once for the same space mode, and beans that listen to it
 * should take it into account.
 *
 * <p>Derived classes should implement the {@link #doCreateSpace()} to obtain the
 * {@link com.j_spaces.core.IJSpace}.
 *
 * @author kimchy
 */
public abstract class AbstractSpaceFactoryBean implements InitializingBean, DisposableBean, FactoryBean, ApplicationContextAware, ApplicationListener {

    protected Log logger = LogFactory.getLog(getClass());

    private IJSpace space;

    private ApplicationContext applicationContext;


    private SpaceMode currentSpaceMode;

    private PrimaryBackupListener primaryBackupListener;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * <p>Initializes the space by calling the {@link #doCreateSpace()}.
     *
     * <p>Registers with the space an internal space mode listener in order to be able to send Spring level
     * {@link org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent} and {@link org.openspaces.core.space.mode.AfterSpaceModeChangeEvent}
     * for primary and backup handling of different beans within the context.
     */
    public void afterPropertiesSet() throws GigaSpaceException {
        this.space = doCreateSpace();
        // register the space mode listener with the space
        if (isEmbeddedSpace()) {
            primaryBackupListener = new PrimaryBackupListener();
            try {
                IJSpace clusterMemberSpace = SpaceUtils.getClusterMemberSpace(space, true);
                ISpaceModeListener remoteListener = (ISpaceModeListener) clusterMemberSpace.getStubHandler().exportObject(primaryBackupListener);
                currentSpaceMode = ((IInternalRemoteJSpaceAdmin) clusterMemberSpace.getAdmin()).addSpaceModeListener(remoteListener);
                if (logger.isDebugEnabled()) {
                    logger.debug("Space [" + clusterMemberSpace + "] mode is [" + currentSpaceMode + "]");
                }
            } catch (RemoteException e) {
                throw new CannotCreateSpaceException("Failed to regsiter space mode listener with space [" + space + "]", e);
            }
        } else {
            currentSpaceMode = SpaceMode.PRIMARY;
        }
    }

    /**
     * Destroys the space and unregisters the intenral space mode listener.
     */
    public void destroy() throws Exception {
        // unregister the sapce mode listener
        if (isEmbeddedSpace()) {
            IJSpace clusterMemberSpace = SpaceUtils.getClusterMemberSpace(space, true);
            try {
                ISpaceModeListener remoteListener = (ISpaceModeListener) clusterMemberSpace.getStubHandler().exportObject(primaryBackupListener);
                ((IInternalRemoteJSpaceAdmin) clusterMemberSpace.getAdmin()).removeSpaceModeListener(remoteListener);
            } catch (RemoteException e) {
                logger.warn("Failed to unregister space mode listener with space [" + space + "]", e);
            }
        }
    }

    /**
     * <p>If {@link org.springframework.context.event.ContextRefreshedEvent} is raised will send two extra
     * events: {@link org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent} and
     * {@link org.openspaces.core.space.mode.AfterSpaceModeChangeEvent} with the current space mode. This is
     * done since other beans that related on this events might not catch them only after the bean has Spring
     * context has been refereshed.
     *
     * <p>Note, this will mean that events with the same Space mode might be raised, one after the other, and
     * Spring beans that listens for them should take it into account.
     *
     * @param applicationEvent
     */
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            if (applicationContext != null) {
                applicationContext.publishEvent(new BeforeSpaceModeChangeEvent(space, currentSpaceMode));
                applicationContext.publishEvent(new AfterSpaceModeChangeEvent(space, currentSpaceMode));
            }
        }
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

    /**
     * Returns <code>true</code> if the space is an embedded one (i.e. does not start with <code>jini</code> or
     * <code>rmi</code> protocols).
     */
    protected abstract boolean isEmbeddedSpace();

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
