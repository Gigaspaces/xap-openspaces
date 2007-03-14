package org.openspaces.core.space;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.dao.DataAccessException;

import com.gigaspaces.cluster.activeelection.ISpaceModeListener;
import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;

/**
 * Base class for most space factory beans responsible for creating/finding {@link IJSpace}
 * implementation.
 * 
 * <p>
 * Provides support for raising Spring application events: {@link BeforeSpaceModeChangeEvent} and
 * {@link AfterSpaceModeChangeEvent} alerting other beans of the current space mode
 * (primary/backup). Beans that wish to be notified of it should implement Spring
 * {@link org.springframework.context.ApplicationListener}. Note that this space mode events might
 * be raised more than once for the same space mode, and beans that listen to it should take it into
 * account.
 * 
 * <p>
 * The space mode event will be raised regardless of the space "type" that is used. For embedded
 * spaces, an actual space mode event listener will be regsitered with the actual cluster member (if
 * not in cluster mode, the actual space). For remote space lookups (jini/rmi), no listener will be
 * regsitered and Space mode events will still be raised during context refresh with a
 * <code>PRIMARY</code> mode in order to allow beans to be written regardless of how the space is
 * looked up.
 * 
 * <p>
 * Derived classes should implement the {@link #doCreateSpace()} to obtain the {@link IJSpace}.
 * 
 * @author kimchy
 */
public abstract class AbstractSpaceFactoryBean implements InitializingBean, DisposableBean, FactoryBean,
        ApplicationContextAware, ApplicationListener {

    protected Log logger = LogFactory.getLog(getClass());

    private IJSpace space;

    private ApplicationContext applicationContext;

    private SpaceMode currentSpaceMode;

    private PrimaryBackupListener primaryBackupListener;

    /**
     * Injected by Spring thanks to {@link ApplicationContextAware}.
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Initializes the space by calling the {@link #doCreateSpace()}.
     * 
     * <p>
     * Registers with the space an internal space mode listener in order to be able to send Spring
     * level {@link BeforeSpaceModeChangeEvent} and {@link AfterSpaceModeChangeEvent} for primary
     * and backup handling of different beans within the context.
     */
    public void afterPropertiesSet() throws DataAccessException {
        this.space = doCreateSpace();
        // register the space mode listener with the space
        if (isEmbeddedSpace()) {
            primaryBackupListener = new PrimaryBackupListener();
            try {
                IJSpace clusterMemberSpace = SpaceUtils.getClusterMemberSpace(space, true);
                ISpaceModeListener remoteListener = (ISpaceModeListener) clusterMemberSpace.getStubHandler()
                    .exportObject(primaryBackupListener);
                currentSpaceMode = ((IInternalRemoteJSpaceAdmin) clusterMemberSpace.getAdmin()).addSpaceModeListener(remoteListener);
                if (logger.isDebugEnabled()) {
                    logger.debug("Space [" + clusterMemberSpace + "] mode is [" + currentSpaceMode + "]");
                }
            } catch (RemoteException e) {
                throw new CannotCreateSpaceException("Failed to regsiter space mode listener with space [" + space
                        + "]", e);
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
                ISpaceModeListener remoteListener = (ISpaceModeListener) clusterMemberSpace.getStubHandler()
                    .exportObject(primaryBackupListener);
                ((IInternalRemoteJSpaceAdmin) clusterMemberSpace.getAdmin()).removeSpaceModeListener(remoteListener);
            } catch (RemoteException e) {
                logger.warn("Failed to unregister space mode listener with space [" + space + "]", e);
            }
        }
    }

    /**
     * If {@link ContextRefreshedEvent} is raised will send two extra events:
     * {@link BeforeSpaceModeChangeEvent} and {@link AfterSpaceModeChangeEvent} with the current
     * space mode. This is done since other beans that use this events might not catch them while
     * the context is constructed.
     * 
     * <p>
     * Note, this will mean that events with the same Space mode might be raised, one after the
     * other, and Spring beans that listens for them should take it into account.
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
     * Spring factory bean returning the {@link IJSpace} created during the bean initializtion ({@link #afterPropertiesSet()}).
     * 
     * @return The {@link IJSpace} implementation
     * @throws Exception
     */
    public Object getObject() throws Exception {
        return this.space;
    }

    /**
     * Returns the object type of the factory bean. Defaults to IJSpace class or the actual
     * {@link IJSpace} implementation class.
     */
    public Class<? extends IJSpace> getObjectType() {
        return (space == null ? IJSpace.class : space.getClass());
    }

    /**
     * Returns <code>true</code> since this factory is a singleton.
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * Responsible for creating/finding the actual {@link IJSpace} implementation.
     * 
     * @return The IJSpace implementation used for the factory bean
     * @throws DataAccessException
     */
    protected abstract IJSpace doCreateSpace() throws DataAccessException;

    /**
     * Returns <code>true</code> if the space is an embedded one (i.e. does not start with
     * <code>jini</code> or <code>rmi</code> protocols).
     * 
     * <p>
     * Default implementation delegates to {@link IJSpace#isEmbedded()}.
     */
    protected boolean isEmbeddedSpace() {
        return space.isEmbedded();
    }

    private class PrimaryBackupListener implements ISpaceModeListener {

        public void beforeSpaceModeChange(SpaceMode spaceMode) throws RemoteException {
            currentSpaceMode = spaceMode;
            if (logger.isDebugEnabled()) {
                logger.debug("Space [" + space + "] BEFORE mode is [" + currentSpaceMode + "]");
            }
            if (applicationContext != null) {
                applicationContext.publishEvent(new BeforeSpaceModeChangeEvent(space, spaceMode));
            }
        }

        public void afterSpaceModeChange(SpaceMode spaceMode) throws RemoteException {
            currentSpaceMode = spaceMode;
            if (logger.isDebugEnabled()) {
                logger.debug("Space [" + space + "] AFTER mode is [" + currentSpaceMode + "]");
            }
            if (applicationContext != null) {
                applicationContext.publishEvent(new AfterSpaceModeChangeEvent(space, spaceMode));
            }
        }
    }
}
