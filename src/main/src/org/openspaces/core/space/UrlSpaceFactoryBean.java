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

import com.gigaspaces.datasource.ManagedDataSource;
import com.gigaspaces.reflect.IField;
import com.gigaspaces.reflect.ReflectionUtil;
import com.j_spaces.core.Constants;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.SpaceFinder;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.SpaceURLParser;
import com.j_spaces.core.filters.FilterOperationCodes;
import com.j_spaces.core.filters.FilterProvider;
import com.j_spaces.core.filters.ISpaceFilter;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;
import com.j_spaces.sadapter.datasource.DataAdapter;
import net.jini.core.entry.UnusableEntryException;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.executor.TaskGigaSpace;
import org.openspaces.core.executor.TaskGigaSpaceAware;
import org.openspaces.core.executor.internal.InternalSpaceTaskWrapper;
import org.openspaces.core.executor.support.DelegatingTask;
import org.openspaces.core.executor.support.ProcessObjectsProvider;
import org.openspaces.core.properties.BeanLevelMergedPropertiesAware;
import org.openspaces.core.space.filter.FilterProviderFactory;
import org.openspaces.core.space.filter.replication.ReplicationFilterProviderFactory;
import org.openspaces.core.util.SpaceUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

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
 * @see com.j_spaces.core.client.SpaceURLParser
 * @see com.j_spaces.core.client.SpaceFinder
 */
public class UrlSpaceFactoryBean extends AbstractSpaceFactoryBean implements BeanLevelMergedPropertiesAware,
        ClusterInfoAware {

    private String url;

    private Map<String, Object> parameters;

    private Properties properties;

    private Properties urlProperties;

    private String schema;

    private String lookupGroups;

    private String lookupLocators;

    private Integer lookupTimeout;

    private Boolean versioned;

    private Boolean noWriteLease;

    private Boolean mirror;

    private Boolean fifo;

    private FilterProviderFactory[] filterProviders;

    private ReplicationFilterProviderFactory replicationFilterProvider;

    private ManagedDataSource externalDataSource;

    private boolean enableExecutorInjection = true;


    private Properties beanLevelProperties;

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
        this(url, null);
    }

    /**
     * Creates a new url space factory bean based on the url and map parameters provided.
     *
     * @param url    The url to create the {@link IJSpace} with.
     * @param params The parameters to create the {@link IJSpace} with.
     */
    public UrlSpaceFactoryBean(String url, Map<String, Object> params) {
        this.url = url;
        this.parameters = params;
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
        this.parameters = parameters;
    }

    /**
     * Same as {@link #setParameters(java.util.Map) parameters} just with properties for simpler
     * configuration.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Sets the url properties. Note, most if not all url level properties can be set using explicit
     * setters.
     */
    public void setUrlProperties(Properties urlProperties) {
        this.urlProperties = urlProperties;
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
        this.schema = schema;
    }

    /**
     * Indicates that all take/write operations be conducted in FIFO mode. Default is
     * the Space default (<code>false</code>).
     */
    public void setFifo(boolean fifo) {
        this.fifo = fifo;
    }

    /**
     * The Jini Lookup Service group to find container or space using multicast (jini protocol).
     * Groups are comma separated list.
     */
    public void setLookupGroups(String lookupGroups) {
        this.lookupGroups = lookupGroups;
    }

    /**
     * The Jini Lookup locators for the Space. In the form of: <code>host1:port1,host2:port2</code>.
     */
    public void setLookupLocators(String lookupLocators) {
        this.lookupLocators = lookupLocators;
    }

    /**
     * The max timeout in <b>milliseconds</b> to find a Container or Space using multicast (jini
     * protocol). Defaults to <code>6000</code> (i.e. 6 seconds).
     */
    public void setLookupTimeout(Integer lookupTimeout) {
        this.lookupTimeout = lookupTimeout;
    }

    /**
     * When <code>false</code>, optimistic lock is disabled. Default to the Space default value.
     */
    public void setVersioned(boolean versioned) {
        this.versioned = versioned;
    }

    /**
     * If <code>true</code> - Lease object would not return from the write/writeMultiple
     * operations. Defaults to the Space default value (<code>false</code>).
     */
    public void setNoWriteLease(boolean noWriteLease) {
        this.noWriteLease = noWriteLease;
    }

    /**
     * When setting this URL property to <code>true</code> it will allow the space to connect to
     * the Mirror service to push its data and operations for asynchronous persistency. Defaults to
     * the Space default (which defaults to <code>false</code>).
     */
    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }

    /**
     * Inject a list of filter provider factories providing the ability to
     * inject actual Space filters.
     */
    public void setFilterProviders(FilterProviderFactory[] filterProviders) {
        this.filterProviders = filterProviders;
    }

    /**
     * Injects a replication provider allowing to directly inject actual replication
     * filters.
     */
    public void setReplicationFilterProvider(ReplicationFilterProviderFactory replicationFilterProvider) {
        this.replicationFilterProvider = replicationFilterProvider;
    }

    /**
     * A data source
     */
    public void setExternalDataSource(ManagedDataSource externalDataSource) {
        this.externalDataSource = externalDataSource;
    }

    /**
     * Externally managed override properties using open spaces extended config support. Should not
     * be set directly but allowed for different Spring context container to set it.
     */
    public void setMergedBeanLevelProperties(Properties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    /**
     * Injected thanks to this bean implementing {@link ClusterInfoAware}. If set will use the
     * cluster information in order to configure the url based on it.
     */
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    /**
     * Creates the space by calling {@link #doGetSpaceUrls()} and then using the returned
     * {@link SpaceURL} a space is found using {@link SpaceFinder#find(SpaceURL)}.
     */
    protected IJSpace doCreateSpace() throws DataAccessException {
        SpaceURL[] spaceURLs = doGetSpaceUrls();
        try {
            return (IJSpace) SpaceFinder.find(spaceURLs);
        } catch (FinderException e) {
            if (SpaceUtils.isRemoteProtocol(spaceURLs[0])) {
                throw new CannotFindSpaceException("Failed to find space with url " + Arrays.toString(spaceURLs) + "", e);
            }
            throw new CannotCreateSpaceException("Failed to create space with url " + Arrays.toString(spaceURLs) + "", e);
        }
    }

    /**
     * Parses the given space url using {@link SpaceURLParser} and returns the parsed
     * {@link SpaceURL}.
     *
     * <p>
     * Uses the {@link #setUrlProperties(java.util.Properties)} and
     * {@link #setParameters(java.util.Map)} as parameters for the space. Also uses the
     * {@link #setClusterInfo(org.openspaces.core.cluster.ClusterInfo)} by automatically translating
     * the cluster information into relevant Space url properties.
     */
    protected SpaceURL[] doGetSpaceUrls() throws DataAccessException {
        Assert.notNull(url, "url property is required");

        String[] urls = StringUtils.tokenizeToStringArray(url, ";");
        SpaceURL[] spacesUrls = new SpaceURL[urls.length];


        for (int urlIndex = 0; urlIndex < urls.length; urlIndex++) {
            String url = urls[urlIndex];

            Properties props = new Properties();
            // copy over the parameters
            if (parameters != null) {
                props.putAll(parameters);
            }
            if (properties != null) {
                props.putAll(properties);
            }

            // copy over the space properties
            if (urlProperties != null) {
                for (Map.Entry<Object, Object> entry : urlProperties.entrySet()) {
                    props.put(SpaceURL.PROPERTIES_SPACE_URL_ARG + "." + entry.getKey(), entry.getValue());
                }
            }

            if (schema != null) {
                props.put(SpaceUtils.spaceUrlProperty(SpaceURL.SCHEMA_NAME), schema);
            }

            if (lookupGroups != null) {
                props.put(SpaceUtils.spaceUrlProperty(SpaceURL.GROUPS), lookupGroups);
            }

            if (lookupLocators != null) {
                props.put(SpaceUtils.spaceUrlProperty(SpaceURL.LOCATORS), lookupLocators);
            }

            if (lookupTimeout != null) {
                props.put(SpaceUtils.spaceUrlProperty(SpaceURL.TIMEOUT), lookupTimeout.toString());
            }

            if (fifo != null) {
                props.put(SpaceUtils.spaceUrlProperty(SpaceURL.FIFO_MODE), Boolean.toString(fifo));
            }
            if (versioned != null) {
                props.put(SpaceUtils.spaceUrlProperty(SpaceURL.VERSIONED), Boolean.toString(versioned));
            }
            if (noWriteLease != null) {
                props.put(SpaceUtils.spaceUrlProperty(SpaceURL.NO_WRITE_LEASE), Boolean.toString(noWriteLease));
            }
            if (mirror != null) {
                props.put(SpaceUtils.spaceUrlProperty(SpaceURL.MIRROR), Boolean.toString(mirror));
            }

            if (!SpaceUtils.isRemoteProtocol(url) && enableExecutorInjection && getApplicationContext() != null) {
                if (filterProviders == null) {
                    filterProviders = new FilterProviderFactory[]{new ExecutorFilterProviderFactory()};
                } else {
                    ArrayList<FilterProviderFactory> tmpProviders = new ArrayList<FilterProviderFactory>(filterProviders.length + 1);
                    tmpProviders.addAll(Arrays.asList(filterProviders));
                    tmpProviders.add(new ExecutorFilterProviderFactory());
                    filterProviders = tmpProviders.toArray(new FilterProviderFactory[tmpProviders.size()]);
                }
            }

            if (filterProviders != null && filterProviders.length > 0) {
                if (SpaceUtils.isRemoteProtocol(url)) {
                    throw new IllegalArgumentException("Filters can only be used with an embedded Space");
                }
                FilterProvider[] spaceFilterProvider = new FilterProvider[filterProviders.length];
                for (int i = 0; i < filterProviders.length; i++) {
                    spaceFilterProvider[i] = filterProviders[i].getFilterProvider();
                }
                props.put(Constants.Filter.FILTER_PROVIDERS, spaceFilterProvider);
            }

            if (replicationFilterProvider != null) {
                if (SpaceUtils.isRemoteProtocol(url)) {
                    throw new IllegalArgumentException("Replication filter provider can only be used with an embedded Space");
                }
                props.put(Constants.ReplicationFilter.REPLICATION_FILTER_PROVIDER, replicationFilterProvider.getFilterProvider());
            }

            if (externalDataSource != null) {
                if (SpaceUtils.isRemoteProtocol(url)) {
                    throw new IllegalArgumentException("External data source can only be used with an embedded Space");
                }
                props.put(Constants.DataAdapter.DATA_SOURCE, externalDataSource);
                props.put(Constants.StorageAdapter.FULL_STORAGE_STORAGE_ADAPTER_CLASS_PROP, DataAdapter.class.getName());
                props.put(Constants.StorageAdapter.FULL_STORAGE_PERSISTENT_ENABLED_PROP, "true");
                if (logger.isDebugEnabled()) {
                    logger.debug("Data Source [" + externalDataSource + "] provided, enabling data source");
                }
            }

            // copy over the external config overrides
            if (beanLevelProperties != null) {
                props.putAll(beanLevelProperties);
            }

            // if deploy info is provided, apply it to the space url (only if it is an embedde Space).
            if (shouldApplyClusterInfo()) {
                if (clusterInfo.getNumberOfInstances() != null) {
                    String totalMembers = clusterInfo.getNumberOfInstances().toString();
                    if (clusterInfo.getNumberOfBackups() != null && clusterInfo.getNumberOfBackups() != 0) {
                        totalMembers += "," + clusterInfo.getNumberOfBackups();
                    }
                    props.setProperty(SpaceUtils.spaceUrlProperty(SpaceURL.CLUSTER_TOTAL_MEMBERS), totalMembers);
                }
                if (clusterInfo.getInstanceId() != null) {
                    props.setProperty(SpaceUtils.spaceUrlProperty(SpaceURL.CLUSTER_MEMBER_ID), clusterInfo.getInstanceId().toString());
                }
                if (clusterInfo.getBackupId() != null) {
                    props.setProperty(SpaceUtils.spaceUrlProperty(SpaceURL.CLUSTER_BACKUP_ID), clusterInfo.getBackupId().toString());
                }
                if (StringUtils.hasText(clusterInfo.getSchema())) {
                    props.setProperty(SpaceUtils.spaceUrlProperty(SpaceURL.CLUSTER_SCHEMA), clusterInfo.getSchema());
                }
            }

            // no need for a shutdown hook in the space as well
            props.setProperty(Constants.Container.CONTAINER_SHUTDOWN_HOOK_PROP, "false");

            if (logger.isDebugEnabled()) {
                logger.debug("Finding Space with URL [" + url + "] and properties [" + props + "]");
            }

            try {
                spacesUrls[urlIndex] = SpaceURLParser.parseURL(url, props);
            } catch (MalformedURLException e) {
                throw new CannotCreateSpaceException("Failed to parse url [" + url + "]", e);
            }
        }
        return spacesUrls;
    }

    /**
     * Should cluster info be applies to the space url.
     */
    private boolean shouldApplyClusterInfo() {
        if (SpaceUtils.isRemoteProtocol(url)) {
            return false;
        }
        if (clusterInfo == null) {
            return false;
        }
        // only apply if we have a specific cluster schema
        if (url.indexOf("cluster_schema") != -1 || StringUtils.hasText(clusterInfo.getSchema())) {
            return true;
        }
        return false;
    }

    private class ExecutorFilterProviderFactory implements FilterProviderFactory {

        public FilterProvider getFilterProvider() {
            FilterProvider filterProvider = new FilterProvider("InjectionExecutorFilter", new ExecutorSpaceFilter());
            filterProvider.setOpCodes(FilterOperationCodes.BEFORE_EXECUTE);
            return filterProvider;
        }
    }

    private Map<Class, Object> tasksGigaSpaceInjectionMap = new ConcurrentHashMap<Class, Object>();

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
            if (applicationContext == null) {
                return;
            }
            AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
            try {
                Object task = entry.getObject(space);
                if (task instanceof InternalSpaceTaskWrapper) {
                    task = ((InternalSpaceTaskWrapper) task).getTask();
                }
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
    }
}
