/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.core.space;

import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.cluster.MemberAliveIndicator;
import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent;
import org.openspaces.core.space.mode.SpaceAfterBackupListener;
import org.openspaces.core.space.mode.SpaceAfterPrimaryListener;
import org.openspaces.core.space.mode.SpaceBeforeBackupListener;
import org.openspaces.core.space.mode.SpaceBeforePrimaryListener;
import org.openspaces.core.util.SpaceUtils;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
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
import com.gigaspaces.cluster.activeelection.SpaceInitializationIndicator;
import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.dump.InternalDump;
import com.gigaspaces.internal.dump.InternalDumpProcessor;
import com.gigaspaces.internal.dump.InternalDumpProcessorFailedException;
import com.gigaspaces.security.directory.CredentialsProvider;
import com.gigaspaces.security.directory.DefaultCredentialsProvider;
import com.gigaspaces.security.directory.UserDetails;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.IJSpaceContainer;
import com.j_spaces.core.SpaceHealthStatus;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceRuntimeInfo;

/**
 * Base class for most space factory beans responsible for creating/finding {@link IJSpace}
 * implementation.
 *
 * <p>Provides support for raising Spring application events: {@link BeforeSpaceModeChangeEvent} and
 * {@link AfterSpaceModeChangeEvent} alerting other beans of the current space mode
 * (primary/backup). Beans that wish to be notified of it should implement Spring
 * {@link org.springframework.context.ApplicationListener}. Note that this space mode events might
 * be raised more than once for the same space mode, and beans that listen to it should take it into
 * account.
 *
 * <p>The space mode event will be raised regardless of the space "type" that is used. For embedded
 * spaces, an actual space mode event listener will be registered with the actual cluster member (if
 * not in cluster mode, the actual space). For remote space lookups (jini/rmi), no listener will be
 * registered and Space mode events will still be raised during context refresh with a
 * <code>PRIMARY</code> mode in order to allow beans to be written regardless of how the space is
 * looked up.
 *
 * <p>Derived classes should implement the {@link #doCreateSpace()} to obtain the {@link IJSpace}.
 *
 * @author kimchy
 */
public abstract class AbstractSpaceFactoryBean implements BeanNameAware, InitializingBean, DisposableBean, FactoryBean,
ApplicationContextAware, ApplicationListener, MemberAliveIndicator, ServiceDetailsProvider, InternalDumpProcessor {

    protected Log logger = LogFactory.getLog(getClass());

    private String beanName;

    private ISpaceProxy space;

    private ApplicationContext applicationContext;

    private SpaceMode currentSpaceMode;

    private PrimaryBackupListener appContextPrimaryBackupListener;

    private ISpaceModeListener primaryBackupListener;

    private Boolean registerForSpaceMode;

    private Boolean enableMemberAliveIndicator;

    private SecurityConfig securityConfig;

    /**
     * Sets if the space should register for primary backup (mode) notifications. Default behavior (if the flag was not set)
     * will register to primary backup notification if the space was found using an embedded
     * protocol, and will not register for notification if the space was found using <code>rmi</code>
     * or <code>jini</code> protocols.
     */
    public void setRegisterForSpaceModeNotifications(boolean registerForSpaceMode) {
        this.registerForSpaceMode = registerForSpaceMode;
    }
    
    /**
     * Sets the security configuration with the provided custom user details.
     * @param userDetails a custom user details.
     */
    public void setUserDetails(UserDetails userDetails) {
        setCredentialsProvider(new DefaultCredentialsProvider(userDetails));
    }
    
    /**
     * Sets the security configuration with the provided custom credentials provider.
     * @param credentialsProvider a custom credentials provider.
     */
    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.securityConfig = new SecurityConfig(credentialsProvider);
    }

    /**
     * Sets security configuration for the Space. If not set, no security will be used.
     */
    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    protected SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    /**
     * Injected by Spring thanks to {@link ApplicationContextAware}.
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    /**
     * Sets a custom primary backup listener
     */
    public void setPrimaryBackupListener(ISpaceModeListener primaryBackupListener) {
        this.primaryBackupListener = primaryBackupListener;
    }

    /**
     * Should this Space bean control if the cluster member is alive or not. Defaults to
     * <code>true</code> if the Space started is an embedded Space, and <code>false</code>
     * if the it is connected to a remote Space.
     */
    public void setEnableMemberAliveIndicator(Boolean enableMemberAliveIndicator) {
        this.enableMemberAliveIndicator = enableMemberAliveIndicator;
    }

    /**
     * Initializes the space by calling the {@link #doCreateSpace()}.
     *
     * <p>Registers with the Space an internal space mode listener in order to be able to send Spring
     * level {@link BeforeSpaceModeChangeEvent} and {@link AfterSpaceModeChangeEvent} for primary
     * and backup handling of different beans within the context. The registration is based on
     * {@link #isRegisterForSpaceModeNotifications()}.
     */
    public synchronized void afterPropertiesSet() throws DataAccessException {
        this.space = (ISpaceProxy) doCreateSpace();
        // apply security configuration if set, only for remote; Security is applied to embedded when space is created
        if (securityConfig != null && securityConfig.isFilled() && SpaceUtils.isRemoteProtocol(space)) {
            try {
                if (space.isServiceSecured()) {
                    space.login(securityConfig.getCredentialsProvider());
                }
            } catch (Exception e) {
                throw new CannotCreateSpaceException("Failed to login to secured space", e);
            }
        }

        // register the space mode listener with the space
        if (isRegisterForSpaceModeNotifications()) {
            appContextPrimaryBackupListener = new PrimaryBackupListener();
            try {
                IJSpace clusterMemberSpace = SpaceUtils.getClusterMemberSpace(space);
                currentSpaceMode = ((IInternalRemoteJSpaceAdmin) clusterMemberSpace.getAdmin()).addSpaceModeListener(appContextPrimaryBackupListener);
                if (logger.isDebugEnabled()) {
                    logger.debug("Space [" + clusterMemberSpace + "] mode is [" + currentSpaceMode + "]");
                }
            } catch (RemoteException e) {
                throw new CannotCreateSpaceException("Failed to register space mode listener with space [" + space
                        + "]", e);
            }
        } else {
            currentSpaceMode = SpaceMode.PRIMARY;
        }

        if (enableMemberAliveIndicator == null) {
            enableMemberAliveIndicator = !SpaceUtils.isRemoteProtocol(space);
        }
    }

    /**
     * Destroys the space and unregisters the internal space mode listener (if registered).
     */
    public synchronized void destroy() throws Exception {
        if (space == null) {
            return;
        }

        if (isRegisterForSpaceModeNotifications()) {
            // unregister the space mode listener
            IJSpace clusterMemberSpace = SpaceUtils.getClusterMemberSpace(space);
            try {
                ((IInternalRemoteJSpaceAdmin) clusterMemberSpace.getAdmin()).removeSpaceModeListener(appContextPrimaryBackupListener);
            } catch (RemoteException e) {
                logger.warn("Failed to unregister space mode listener with space [" + space + "]", e);
            }
        }
        try {
            
            IJSpaceContainer embeddedContainer = null;
            if (!SpaceUtils.isRemoteProtocol(space)) {
                // if we are in embedded mode, store space
                embeddedContainer = space.getDirectProxy().getContainer();
            }
            space.close();
            
            if (embeddedContainer != null) {
                // shutdown the space if we are in embedded mode
                embeddedContainer.shutdown();
            }
        } finally {
            space = null;
        }
    }

    /**
     * If {@link ContextRefreshedEvent} is raised will send two extra events:
     * {@link BeforeSpaceModeChangeEvent} and {@link AfterSpaceModeChangeEvent} with the current
     * space mode. This is done since other beans that use this events might not catch them while
     * the context is constructed.
     *
     * <p>Note, this will mean that events with the same Space mode might be raised, one after the
     * other, and Spring beans that listens for them should take it into account.
     */
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            if (applicationContext != null) {
                SpaceInitializationIndicator.setInitializer();
                try {
                    applicationContext.publishEvent(new BeforeSpaceModeChangeEvent(space, currentSpaceMode));
                    applicationContext.publishEvent(new AfterSpaceModeChangeEvent(space, currentSpaceMode));

                    if (currentSpaceMode == SpaceMode.BACKUP) {
                        fireSpaceBeforeBackupEvent();
                        fireSpaceAfterBackupEvent();
                    } else if (currentSpaceMode == SpaceMode.PRIMARY) {
                        fireSpaceBeforePrimaryEvent();
                        fireSpaceAfterPrimaryEvent();
                    }
                } finally {
                    SpaceInitializationIndicator.unsetInitializer();
                }
            }
        }
    }

    /**
     * Spring factory bean returning the {@link IJSpace} created during the bean initialization
     * ({@link #afterPropertiesSet()}).
     *
     * @return The {@link IJSpace} implementation
     */
    public Object getObject() {
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
     * Returns the {@link #setEnableMemberAliveIndicator(Boolean)} flag.
     */
    public boolean isMemberAliveEnabled() {
        return enableMemberAliveIndicator;
    }

    /**
     * Returns if this space is alive or not by pinging the Space and if it is considered healthy.
     */
    public boolean isAlive() throws Exception {
        ISpaceProxy spaceToPing;
        if (!SpaceUtils.isRemoteProtocol(space)) {
            spaceToPing = (ISpaceProxy) SpaceUtils.getClusterMemberSpace(space);
        } else {
            spaceToPing = space;
        }
        //Check if the space is considered healthy
        SpaceHealthStatus spaceHealthStatus = spaceToPing.getSpaceHealthStatus();
        if (!spaceHealthStatus.isHealthy())
            throw spaceHealthStatus.getUnhealthyReason();
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
     * Returns if the space should register for primary backup notifications. If {@link #setRegisterForSpaceModeNotifications(boolean)}
     * was set, will return this flag. If not, will register to primary backup notification if the space was
     * found using an embedded protocol, and will not register for notification if the space was found using <code>rmi</code>
     * or <code>jini</code> protocols.
     */
    protected boolean isRegisterForSpaceModeNotifications() {
        if (registerForSpaceMode != null) {
            return registerForSpaceMode;
        }
        return !SpaceUtils.isRemoteProtocol(space);
    }

    /**
     * Sends {@link BeforeSpaceModeChangeEvent} events with space mode {@link SpaceMode#BACKUP} to all beans in the application context
     * that implement the {@link SpaceBeforeBackupListener} interface.
     */
    protected void fireSpaceBeforeBackupEvent() {
        if (applicationContext != null) {
            Map<String, SpaceBeforeBackupListener> beans = applicationContext.getBeansOfType(SpaceBeforeBackupListener.class);
            for (SpaceBeforeBackupListener listener : beans.values()) {
                listener.onBeforeBackup(new BeforeSpaceModeChangeEvent(space, SpaceMode.BACKUP));
            }
        }
    }

    /**
     * Sends {@link AfterSpaceModeChangeEvent} events with space mode {@link SpaceMode#BACKUP} to all beans in the application context
     * that implement the {@link SpaceAfterBackupListener} interface.
     */
    protected void fireSpaceAfterBackupEvent() {
        if (applicationContext != null) {
            Map<String, SpaceAfterBackupListener> beans = applicationContext.getBeansOfType(SpaceAfterBackupListener.class);
            for (SpaceAfterBackupListener listener : beans.values()) {
                listener.onAfterBackup(new AfterSpaceModeChangeEvent(space, SpaceMode.BACKUP));
            }
        }
    }

    /**
     * Sends {@link BeforeSpaceModeChangeEvent} events with space mode {@link SpaceMode#PRIMARY} to all beans in the application context
     * that implement the {@link SpaceBeforePrimaryListener} interface.
     */
    protected void fireSpaceBeforePrimaryEvent() {
        if (applicationContext != null) {
            Map<String, SpaceBeforePrimaryListener> beans = applicationContext.getBeansOfType(SpaceBeforePrimaryListener.class);
            for (SpaceBeforePrimaryListener listener : beans.values()) {
                listener.onBeforePrimary(new BeforeSpaceModeChangeEvent(space, SpaceMode.PRIMARY));
            }
        }
    }

    /**
     * Sends {@link AfterSpaceModeChangeEvent} events with space mode {@link SpaceMode#PRIMARY} to all beans in the application context
     * that implement the {@link SpaceAfterPrimaryListener} interface.
     */
    protected void fireSpaceAfterPrimaryEvent() {
        if (applicationContext != null) {
            Map<String, SpaceAfterPrimaryListener> beans = applicationContext.getBeansOfType(SpaceAfterPrimaryListener.class);
            for (SpaceAfterPrimaryListener listener : beans.values()) {
                listener.onAfterPrimary(new AfterSpaceModeChangeEvent(space, SpaceMode.PRIMARY));
            }
        }
    }

    public ServiceDetails[] getServicesDetails() {
        return new ServiceDetails[]{new SpaceServiceDetails(beanName, space)};
    }

    public String getName() {
        return beanName;
    }

    public void process(InternalDump dump) throws InternalDumpProcessorFailedException {
        if (SpaceUtils.isRemoteProtocol(space)) {
            return;
        }
        dump.addPrefix("spaces/" + beanName + "/");
        try {
            IJSpace clusterMemberSpace = SpaceUtils.getClusterMemberSpace(space);
            PrintWriter writer = new PrintWriter(dump.createFileWriter("summary.txt"));
            writer.println("===== URL =====");
            writer.println(clusterMemberSpace.getFinderURL());
            writer.println();
            writer.println("===== RUNTIME INFO =====");
            IInternalRemoteJSpaceAdmin admin = ((IInternalRemoteJSpaceAdmin) clusterMemberSpace.getAdmin());
            SpaceRuntimeInfo runtimeInfo = admin.getRuntimeInfo();
            for (int i = 0; i < runtimeInfo.m_ClassNames.size(); i++) {
                writer.println("Class [" + runtimeInfo.m_ClassNames.get(i) + "], Entries [" + runtimeInfo.m_NumOFEntries.get(i) + "], Templates [" + runtimeInfo.m_NumOFTemplates.get(i) + "]");
            }
            
            writer.println();
            writer.println("===== REPLICATION INFO =====");
            writer.println(admin.getReplicationDump());
            writer.println();
            writer.close();
        } catch (Exception e) {
            throw new InternalDumpProcessorFailedException(getName(), "Failed to generate space dump", e);
        } finally {
            dump.removePrefix();
        }
    }

    private class PrimaryBackupListener implements ISpaceModeListener {

        public void beforeSpaceModeChange(SpaceMode spaceMode) throws RemoteException {
            currentSpaceMode = spaceMode;
            if (logger.isDebugEnabled()) {
                logger.debug("Space [" + space + "] BEFORE mode is [" + currentSpaceMode + "]");
            }
            if (applicationContext != null) {
                applicationContext.publishEvent(new BeforeSpaceModeChangeEvent(space, spaceMode));

                if (spaceMode == SpaceMode.BACKUP) {
                    fireSpaceBeforeBackupEvent();
                } else if (spaceMode == SpaceMode.PRIMARY) {
                    fireSpaceBeforePrimaryEvent();
                }
            }
            if (primaryBackupListener != null) {
                primaryBackupListener.beforeSpaceModeChange(spaceMode);
            }
        }

        public void afterSpaceModeChange(SpaceMode spaceMode) throws RemoteException {
            currentSpaceMode = spaceMode;
            if (logger.isDebugEnabled()) {
                logger.debug("Space [" + space + "] AFTER mode is [" + currentSpaceMode + "]");
            }
            if (applicationContext != null) {
                applicationContext.publishEvent(new AfterSpaceModeChangeEvent(space, spaceMode));

                if (spaceMode == SpaceMode.BACKUP) {
                    fireSpaceAfterBackupEvent();
                } else if (spaceMode == SpaceMode.PRIMARY) {
                    fireSpaceAfterPrimaryEvent();
                }
            }
            if (primaryBackupListener != null) {
                primaryBackupListener.afterSpaceModeChange(spaceMode);
            }
        }
    }
}
