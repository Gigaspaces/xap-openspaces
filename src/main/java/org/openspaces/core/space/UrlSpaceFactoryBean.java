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

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import com.gigaspaces.client.ClusterConfig;
import com.gigaspaces.client.SpaceProxyFactory;
import com.gigaspaces.internal.lookup.SpaceUrlUtils;
import com.gigaspaces.internal.sync.mirror.MirrorDistributedTxnConfig;
import net.jini.core.entry.UnusableEntryException;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.config.BlobStoreDataPolicyFactoryBean;
import org.openspaces.core.config.CustomCachePolicyFactoryBean;
import org.openspaces.core.executor.AutowireTask;
import org.openspaces.core.executor.AutowireTaskMarker;
import org.openspaces.core.executor.TaskGigaSpace;
import org.openspaces.core.executor.TaskGigaSpaceAware;
import org.openspaces.core.executor.internal.InternalSpaceTaskWrapper;
import org.openspaces.core.executor.support.DelegatingTask;
import org.openspaces.core.executor.support.ProcessObjectsProvider;
import org.openspaces.core.gateway.GatewayTargetsFactoryBean;
import org.openspaces.core.properties.BeanLevelMergedPropertiesAware;
import org.openspaces.core.space.filter.FilterProviderFactory;
import org.openspaces.core.space.filter.replication.ReplicationFilterProviderFactory;
import org.openspaces.core.transaction.DistributedTransactionProcessingConfigurationFactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.gigaspaces.datasource.ManagedDataSource;
import com.gigaspaces.datasource.SpaceDataSource;
import com.gigaspaces.internal.reflection.IField;
import com.gigaspaces.internal.reflection.ReflectionUtil;
import com.gigaspaces.internal.utils.collections.CopyOnUpdateMap;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.sync.SpaceSynchronizationEndpoint;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.filters.FilterOperationCodes;
import com.j_spaces.core.filters.FilterProvider;
import com.j_spaces.core.filters.ISpaceFilter;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;

/**
 * A space factory bean that creates a space ({@link IJSpace}) based on a url.
 *
 * <p>The factory allows to specify url properties using
 * {@link #setUrlProperties(java.util.Properties) urlProperties} and space parameters using
 * {@link #setParameters(java.util.Map) parameters} or using
 * {@link #setProperties(Properties) properties}. It also accepts a {@link ClusterInfo} using
 * {@link #setClusterInfo(ClusterInfo)} and translates it into the relevant space url properties
 * automatically.
 *
 * <p>Most url properties are explicitly exposed using different setters. Though they can also be set
 * using the {@link #setUrlProperties(java.util.Properties) urlProperties} the explicit setters
 * allow for more readable and simpler configuration. Some examples of explicit url properties are:
 * {@link #setSchema(String)}, {@link #setFifo(boolean)}.
 *
 * <p>The factory uses the {@link BeanLevelMergedPropertiesAware} in order to be injected with
 * properties that were not parameterized in advance (using ${...} notation). This will directly
 * inject additional properties in the Space creation/finding process.
 *
 * @author kimchy
 */
public class UrlSpaceFactoryBean extends AbstractSpaceFactoryBean implements BeanLevelMergedPropertiesAware, ClusterInfoAware {

    private final SpaceProxyFactory factory = new SpaceProxyFactory();
    private final boolean enableExecutorInjection = true;
    private String url;
    private String name;
    private Boolean isRemote;
    private ClusterInfo clusterInfo;

    /**
     * Creates a new url space factory bean. The url parameters is requires so the
     * {@link #setUrl(String)} must be called before the bean is initialized.
     */
    public UrlSpaceFactoryBean() {
    }

    /**
     * Creates a new url space factory bean based on the url provided.
     *
     * @param url The url to create the {@link com.j_spaces.core.IJSpace} with.
     */
    public UrlSpaceFactoryBean(String url) {
        this.url = url;
    }

    /**
     * Creates a new url space factory bean based on the url and map parameters provided.
     *
     * @param url    The url to create the {@link IJSpace} with.
     * @param params The parameters to create the {@link IJSpace} with.
     */
    public UrlSpaceFactoryBean(String url, Map<String, Object> params) {
        this(url);
        setParameters(params);
    }

    UrlSpaceFactoryBean(boolean isRemote) {
        this.isRemote = isRemote;
    }
    /**
     * Creates the space.
     */
    @Override
    protected IJSpace doCreateSpace() throws DataAccessException {
        if (isRemote == null) {
            Assert.notNull(url, "url property is required");
            isRemote = SpaceUrlUtils.isRemoteProtocol(url);
            factory.setClusterConfig(toClusterConfig(url, clusterInfo));
            beforeCreateSpace();
            try {
                return factory.createSpaceProxy(url);
            } catch (MalformedURLException e) {
                throw new CannotCreateSpaceException("Failed to parse url [" + url + "]", e);
            } catch (FinderException e) {
                if (isRemote) {
                    throw new CannotFindSpaceException("Failed to find space with url " + url + "", e);
                }
                throw new CannotCreateSpaceException("Failed to create space with url " + url + "", e);
            }
        } else {
            Assert.notNull(name, "name property is required");
            beforeCreateSpace();
            try {
                return factory.createSpaceProxy(name, isRemote);
            } catch (MalformedURLException e) {
                throw new CannotCreateSpaceException("Failed to build url for space [" + name + "]", e);
            } catch (FinderException e) {
                if (isRemote) {
                    throw new CannotFindSpaceException("Failed to find space " + name + "", e);
                }
                throw new CannotCreateSpaceException("Failed to create space " + name + "", e);
            }
        }
    }

    private void beforeCreateSpace() {
        if (!isRemote && enableExecutorInjection) {
            FilterProvider filterProvider = new FilterProvider("InjectionExecutorFilter", new ExecutorSpaceFilter());
            filterProvider.setOpCodes(FilterOperationCodes.BEFORE_EXECUTE);
            factory.addFilterProvider(filterProvider);
        }
    }

    private static ClusterConfig toClusterConfig(String url, ClusterInfo clusterInfo) {
        if (clusterInfo == null || SpaceUrlUtils.isRemoteProtocol(url))
            return null;

        if (url.indexOf(SpaceURL.CLUSTER_SCHEMA + "=") == -1 && !StringUtils.hasText(clusterInfo.getSchema()))
            return null;
        ClusterConfig clusterConfig = new ClusterConfig();
        if (url.indexOf(SpaceURL.CLUSTER_SCHEMA + "=") == -1)
            clusterConfig.setSchema(clusterInfo.getSchema());
        if (url.indexOf("&" + SpaceURL.CLUSTER_TOTAL_MEMBERS + "=") == -1 && url.indexOf("?" + SpaceURL.CLUSTER_TOTAL_MEMBERS + "=") == -1) {
            clusterConfig.setNumberOfInstances(clusterInfo.getNumberOfInstances());
            clusterConfig.setNumberOfBackups(clusterInfo.getNumberOfBackups());
        }
        if (url.indexOf("&" + SpaceURL.CLUSTER_MEMBER_ID + "=") == -1 && url.indexOf("?" + SpaceURL.CLUSTER_MEMBER_ID + "=") == -1)
            clusterConfig.setInstanceId(clusterInfo.getInstanceId());

        if (url.indexOf("&" + SpaceURL.CLUSTER_BACKUP_ID + "=") == -1 && url.indexOf("?" + SpaceURL.CLUSTER_BACKUP_ID + "=") == -1)
            clusterConfig.setBackupId(clusterInfo.getBackupId());

        return clusterConfig;
    }

    /**
     * Sets the space as secured. Note, when passing userName and password it will
     * automatically be secured.
     */
    public void setSecured(boolean secured) {
        factory.setSecured(secured);
    }

    @Override
    public void setSecurityConfig(SecurityConfig securityConfig) {
        super.setSecurityConfig(securityConfig);
        factory.setCredentialsProvider(securityConfig == null ? null : securityConfig.getCredentialsProvider());
    }

    /**
     * Sets the url the {@link IJSpace} will be created with. Note this url does not take affect
     * after the bean has been initialized.
     *
     * @param url The url to create the {@link IJSpace} with.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    void setName(String name) {
        this.name = name;
    }

    void setInstanceId(String instanceId) {
        factory.setInstanceId(instanceId);
    }

    /**
     * Sets the parameters the {@link IJSpace} will be created with. Note this parameters does not
     * take affect after the bean has been initialized.
     *
     * <p>
     * Note, this should not be confused with {@link #setUrlProperties(java.util.Properties)}. The
     * parameters here are the ones referred to as custom properties and allows for example to
     * control the xpath injection to space schema.
     *
     * @param parameters The parameters to create the {@link com.j_spaces.core.IJSpace} with.
     */
    public void setParameters(Map<String, Object> parameters) {
        factory.setParameters(parameters);
    }

    /**
     * Same as {@link #setParameters(java.util.Map) parameters} just with properties for simpler
     * configuration.
     */
    public void setProperties(Properties properties) {
        factory.setProperties(properties);
    }

    /**
     * Sets the url properties. Note, most if not all url level properties can be set using explicit
     * setters.
     */
    public void setUrlProperties(Properties urlProperties) {
        factory.setUrlProperties(urlProperties);
    }

    /**
     * The space instance is created using a space schema file which can be used as a template
     * configuration file for creating a space. The user specifies one of the pre-configured schema
     * names (to create a space instance from its template) or a custom one using this property.
     *
     * <p>If a schema name is not defined, a default schema name called <code>default</code> will be
     * used.
     */
    public void setSchema(String schema) {
        factory.setSchema(schema);
    }

    /**
     * Indicates that all take/write operations be conducted in FIFO mode. Default is
     * the Space default (<code>false</code>).
     */
    public void setFifo(boolean fifo) {
        factory.setFifo(fifo);
    }

    /**
     * The Jini Lookup Service group to find container or space using multicast (jini protocol).
     * Groups are comma separated list.
     */
    public void setLookupGroups(String lookupGroups) {
        factory.setLookupGroups(lookupGroups);
    }

    /**
     * The Jini Lookup locators for the Space. In the form of: <code>host1:port1,host2:port2</code>.
     */
    public void setLookupLocators(String lookupLocators) {
        factory.setLookupLocators(lookupLocators);
    }

    /**
     * The max timeout in <b>milliseconds</b> to find a Container or Space using multicast (jini
     * protocol). Defaults to <code>6000</code> (i.e. 6 seconds).
     */
    public void setLookupTimeout(Integer lookupTimeout) {
        factory.setLookupTimeout(lookupTimeout);
    }

    /**
     * When <code>false</code>, optimistic lock is disabled. Default to the Space default value.
     */
    public void setVersioned(boolean versioned) {
        factory.setVersioned(versioned);
    }

    /**
     * If <code>true</code> - Lease object would not return from the write/writeMultiple
     * operations. Defaults to the Space default value (<code>false</code>).
     */
    public void setNoWriteLease(boolean noWriteLease) {
        // Ignore - NoWriteLease is no longer supported.
    }

    /**
     * When setting this URL property to <code>true</code> it will allow the space to connect to
     * the Mirror service to push its data and operations for asynchronous persistency. Defaults to
     * the Space default (which defaults to <code>false</code>).
     */
    public void setMirror(boolean mirror) {
        factory.setMirror(mirror);
    }

    /**
     * Inject a list of filter provider factories providing the ability to
     * inject actual Space filters.
     */
    public void setFilterProviders(FilterProviderFactory[] filterProviders) {
        FilterProvider[] spaceFilterProviders = null;
        if (filterProviders != null) {
            spaceFilterProviders = new FilterProvider[filterProviders.length];
            for (int i = 0; i < filterProviders.length; i++) {
                spaceFilterProviders[i] = filterProviders[i].getFilterProvider();
            }
        }
        factory.setFilterProviders(spaceFilterProviders);
    }

    /**
     * Injects a replication provider allowing to directly inject actual replication
     * filters.
     */
    public void setReplicationFilterProvider(ReplicationFilterProviderFactory replicationFilterProvider) {
        factory.setReplicationFilterProvider(replicationFilterProvider == null ? null : replicationFilterProvider.getFilterProvider());
    }

    /**
     * A data source
     */
    public void setExternalDataSource(ManagedDataSource externalDataSource) {
        factory.setExternalDataSource(externalDataSource);
    }
    
    /**
     * Sets the {@link SpaceDataSource} which will be used as a data source for the space.
     * @param spaceDataSource The {@link SpaceDataSource} instance.
     */
    public void setSpaceDataSource(SpaceDataSource spaceDataSource) {
        factory.setSpaceDataSource(spaceDataSource);
    }

    /**
     * @param spaceSynchronizationEndpoint
     */
    public void setSpaceSynchronizationEndpoint(SpaceSynchronizationEndpoint spaceSynchronizationEndpoint) {
        factory.setSpaceSynchronizationEndpoint(spaceSynchronizationEndpoint);
    }

    /**
     * Inject a list of space types.
     */
    public void setSpaceTypes(SpaceTypeDescriptor[] typeDescriptors) {
        factory.setTypeDescriptors(typeDescriptors);
    }

    /**
     * Sets the cache policy that the space will use. If not set, will default to the one configured
     * in the space schema.
     *
     * @see org.openspaces.core.space.AllInCachePolicy
     * @see org.openspaces.core.space.LruCachePolicy
     * @see org.openspaces.core.space.CustomCachePolicy
     * @see org.openspaces.core.space.BlobStoreDataCachePolicy
     */
    public void setCachePolicy(CachePolicy cachePolicy) {
        factory.setCachePolicyProperties(cachePolicy == null ? null : cachePolicy.toProps());
    }

    /**
     * Externally managed override properties using open spaces extended config support. Should not
     * be set directly but allowed for different Spring context container to set it.
     */
    public void setMergedBeanLevelProperties(Properties beanLevelProperties) {
        factory.setBeanLevelProperties(beanLevelProperties);
    }

    /**
     * Injected thanks to this bean implementing {@link ClusterInfoAware}. If set will use the
     * cluster information in order to configure the url based on it.
     */
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    /**
     * Sets the gateway replication targets to be used with the constructed space.
     * @param gatewayTargets The gateway targets.
     */
    public void setGatewayTargets(GatewayTargetsFactoryBean gatewayTargets) {
        factory.setGatewayPolicy(gatewayTargets == null ? null : gatewayTargets.asGatewaysPolicy());
    }

    /**
     * Sets the distributed transaction processing configuration for the Mirror component.
     * @param distributedTransactionProcessingConfiguration The distributed transaction processing configuration to set.
     */
    public void setDistributedTransactionProcessingConfiguration(
            DistributedTransactionProcessingConfigurationFactoryBean distributedTransactionProcessingConfiguration) {
        MirrorDistributedTxnConfig mirrorDistributedTxnConfig = null;
        if (distributedTransactionProcessingConfiguration != null) {
            mirrorDistributedTxnConfig = new MirrorDistributedTxnConfig()
                    .setDistributedTransactionWaitForOperations(distributedTransactionProcessingConfiguration.getDistributedTransactionWaitForOperations())
                    .setDistributedTransactionWaitTimeout(distributedTransactionProcessingConfiguration.getDistributedTransactionWaitTimeout());
        }
        factory.setMirrorDistributedTxnConfig(mirrorDistributedTxnConfig);
    }

    public void setCustomCachePolicy(CustomCachePolicyFactoryBean customCachePolicy) {
        if (customCachePolicy != null)
            setCachePolicy(customCachePolicy.asCachePolicy());
    }

    public void setBlobStoreDataPolicy(BlobStoreDataPolicyFactoryBean blobStoreDataPolicy) {
        if (blobStoreDataPolicy != null)
            setCachePolicy(blobStoreDataPolicy.asCachePolicy());
    }

    private final Map<Class, Object> tasksGigaSpaceInjectionMap = new CopyOnUpdateMap<Class, Object>();

    private static Object NO_FIELD = new Object();

    private class ExecutorSpaceFilter implements ISpaceFilter {

        private IJSpace space;

        private GigaSpace gigaSpace;

        public void init(IJSpace space, String filterId, String url, int priority) throws RuntimeException {
            this.space = space;
            this.gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
        }

        public void process(SpaceContext context, ISpaceFilterEntry entry, int operationCode) throws RuntimeException {
            if (operationCode != FilterOperationCodes.BEFORE_EXECUTE) {
                return;
            }
            ApplicationContext applicationContext = getApplicationContext();
            AutowireCapableBeanFactory beanFactory = null;
            if (applicationContext != null) {
                beanFactory = applicationContext.getAutowireCapableBeanFactory();
            }
            try {
                Object task = entry.getObject(space);
                if (task instanceof InternalSpaceTaskWrapper) {
                    task = ((InternalSpaceTaskWrapper) task).getTask();
                }
                // go over the task and inject what can be injected
                // break when there is no more DelegatingTasks
                while (true) {
                    if (task instanceof TaskGigaSpaceAware) {
                        ((TaskGigaSpaceAware) task).setGigaSpace(gigaSpace);
                    } else {
                        Object field = tasksGigaSpaceInjectionMap.get(task.getClass());
                        if (field == NO_FIELD) {
                            // do nothing
                        } else if (field != null) {
                            try {
                                ((IField) field).set(task, gigaSpace);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException("Failed to set task GigaSpace field", e);
                            }
                        } else {
                            final AtomicReference<Field> ref = new AtomicReference<Field>();
                            ReflectionUtils.doWithFields(task.getClass(), new ReflectionUtils.FieldCallback() {
                                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                                    if (field.isAnnotationPresent(TaskGigaSpace.class)) {
                                        ref.set(field);
                                    }
                                }
                            });
                            if (ref.get() == null) {
                                tasksGigaSpaceInjectionMap.put(task.getClass(), NO_FIELD);
                            } else {
                                ref.get().setAccessible(true);
                                IField fastField = ReflectionUtil.createField(ref.get());
                                tasksGigaSpaceInjectionMap.put(task.getClass(), fastField);
                                try {
                                    fastField.set(task, gigaSpace);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException("Failed to set task GigaSpace field", e);
                                }
                            }
                        }
                    }

                    if (isAutowire(task)) {
                        if (beanFactory == null) {
                            throw new IllegalStateException("Task [" + task.getClass().getName() + "] is configured to do autowiring but the space was not started with application context");
                        }
                        beanFactory.autowireBeanProperties(task, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
                        beanFactory.initializeBean(task, task.getClass().getName());
                        if (task instanceof ProcessObjectsProvider) {
                            Object[] objects = ((ProcessObjectsProvider) task).getObjectsToProcess();
                            if (objects != null) {
                                for (Object obj : objects) {
                                    if (obj != null) {
                                        beanFactory.autowireBeanProperties(obj, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
                                        beanFactory.initializeBean(obj, obj.getClass().getName());
                                    }
                                }
                            }
                        }
                    } else {
                        if (applicationContext != null && task instanceof ApplicationContextAware) {
                            ((ApplicationContextAware) task).setApplicationContext(applicationContext);
                        }
                        if (clusterInfo != null && task instanceof ClusterInfoAware) {
                            ((ClusterInfoAware) task).setClusterInfo(clusterInfo);
                        }
                    }

                    if (task instanceof DelegatingTask) {
                        task = ((DelegatingTask) task).getDelegatedTask();
                    } else {
                        break;
                    }
                }
            } catch (UnusableEntryException e) {
                // won't happen
            }
        }

        public void process(SpaceContext context, ISpaceFilterEntry[] entries, int operationCode) throws RuntimeException {

        }

        public void close() throws RuntimeException {

        }

        private boolean isAutowire(Object obj) {
            if (obj instanceof AutowireTaskMarker) {
                return true;
            }
            return obj.getClass().isAnnotationPresent(AutowireTask.class);
        }
    }
}
