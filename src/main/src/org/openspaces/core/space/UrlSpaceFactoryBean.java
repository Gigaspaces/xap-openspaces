package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.SpaceFinder;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.SpaceURLParser;
import com.j_spaces.core.client.view.View;
import org.openspaces.core.GigaSpaceException;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.config.BeanLevelMergedPropertiesAware;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * <p>A space factory bean that creates a space based on a url.
 *
 * <p>The factory allows to specify url properties using {@link #setUrlProperties(java.util.Properties)}
 * and space parameters using {@link #setParameters(java.util.Map)}. It also accepts a
 * {@link org.openspaces.core.cluster.ClusterInfo} using {@link #setClusterInfo(org.openspaces.core.cluster.ClusterInfo)}
 * and translates it into the relevant space url properties automatically.
 *
 * <p>The factory uses the {@link org.openspaces.core.config.BeanLevelMergedPropertiesAware} in order to be injected
 * with properties that were not parameterized in advance (using ${...} notation). This will directly inject additional
 * properties in the Space creation/finding process.
 *
 * @author kimchy
 * @see com.j_spaces.core.client.SpaceURLParser
 * @see com.j_spaces.core.client.SpaceFinder
 */
public class UrlSpaceFactoryBean extends AbstractSpaceFactoryBean implements BeanLevelMergedPropertiesAware, ClusterInfoAware {

    private static final String LOCAL_CACHE_UPDATE_MODE_PUSH = "push";

    private static final String LOCAL_CACHE_UPDATE_MODE_PULL = "pull";

    private String url;

    private Map parameters;

    private Properties urlProperties;

    private String schema;

    private String lookupGroups;

    private Integer lookupTimeout;

    private boolean versioned = true;

    private boolean noWriteLease = false;

    private boolean mirror = false;

    private boolean useLocalCache = false;

    private int localCacheUpdateMode = SpaceURL.UPDATE_MODE_PULL;

    private View[] localViews;

    private boolean fifo = false;

    private Properties beanLevelProperties;

    private ClusterInfo clusterInfo;


    private SpaceURL spaceURL;

    /**
     * Creates a new url space factory bean. The url parameres is requires
     * so the {@link #setUrl(String)} must be called before the bean is initalized.
     */
    public UrlSpaceFactoryBean() {

    }

    /**
     * <p>Creates a new url space factory bean based on the url provided.
     *
     * @param url The url to create the {@link com.j_spaces.core.IJSpace} with.
     */
    public UrlSpaceFactoryBean(String url) {
        this(url, null);
    }

    /**
     * <p>Creates a new url space factory bean based on the url and map parameters provided.
     *
     * @param url    The url to create the {@link com.j_spaces.core.IJSpace} with.
     * @param params The parameters to create the {@link com.j_spaces.core.IJSpace} with.
     */
    public UrlSpaceFactoryBean(String url, Map params) {
        this.url = url;
        this.parameters = params;
    }

    /**
     * <p>Sets the url the {@link com.j_spaces.core.IJSpace} will be created with. Note
     * this url does not take affect after the bean has been initalized.
     *
     * @param url The url to create the {@link com.j_spaces.core.IJSpace} with.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * <p>Sets the parameters the {@link com.j_spaces.core.IJSpace} will be created with.
     * Note this paramerters does not take affect after the bean has been initalized.
     *
     * <p>Note, this should not be confused with {@link #setUrlProperties(java.util.Properties)}. The
     * parameters here are the ones refered to as custom properties and allows for example to control
     * the xpath injection to space schema.
     *
     * @param parameters The parameters to create the {@link com.j_spaces.core.IJSpace} with.
     */
    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    /**
     * <p>Sets the url properties (cluster_schema, total_members, ...).
     */
    public void setUrlProperties(Properties urlProperties) {
        this.urlProperties = urlProperties;
    }

    /**
     * <p>The space instance is created using a space schema file which can be used as a template configuration
     * file for creating a space. The user specifies one of the pre-configured schema names (to create a space
     * instance from its template) or a custom one using this property.
     *
     * <p>If a schema name is not defined, a default schema name called <code>default</code> will be used.
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Indicates that all take/write operations be conducted in FIFO mode. Default is <code>false</code>.
     */
    public void setFifo(boolean fifo) {
        this.fifo = fifo;
    }

    /**
     * The Jini Lookup Service group to find container or space using multicast (jini protocol). Groups
     * are comma separated list.
     */
    public void setLookupGroups(String lookupGroups) {
        this.lookupGroups = lookupGroups;
    }


    /**
     * The max timeout in <b>milliseconds</b> to find a Container or Space using multicast (jini protocol).
     * Defaults to <code>6000</code> (i.e. 6 seconds).
     */
    public void setLookupTimeout(Integer lookupTimeout) {
        this.lookupTimeout = lookupTimeout;
    }

    /**
     * When <code>false</code>, optimistic lock is disabled. Default is <code>true</code>.
     */
    public void setVersioned(boolean versioned) {
        this.versioned = versioned;
    }

    /**
     * If <code>true</code> - Lease object would not return from the write/writeMultiple operations.
     * Defaults to <code>false</code>.
     */
    public void setNoWriteLease(boolean noWriteLease) {
        this.noWriteLease = noWriteLease;
    }

    /**
     * When setting this URL property to <code>true</code> it will allow the space to connect to the Mirror
     * service to push its data and operations for asynchronous persistency. Defaults to <code>false</code>.
     */
    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }

    /**
     * <p>In some cases, the memory capacity of an individual application is not capable of holding all the
     * information in the local application memory. When this happens, the desired solution will be to hold
     * only a portion of the information in the application's memory and the rest in a separate process(s).
     * This mode is also known as two-level cache. In this mode, the cache is divided into two components,
     * local cache and master cache. The local cache always resides in the physical address space of the
     * application and the master cache runs in a different process. The master cache is used to share data
     * among the different embedded local caches running within other application instances.
     *
     * <p>In this mode, when a read/get operation is called, a matching object is first looked up in the local
     * embedded cache. If the object is not found in the local cache, it will be searched for in the master
     * cache. If it is not found in the master cache, it will reload the data from the data source. Updates
     * on the central cache will be propagated into all local embedded cache instances in either pull or
     * push mode, using unicast or multicast protocol.
     *
     * @see #setLocalCacheUpdateMode(int)
     * @see #setLocalCacheUpdateModeName(String)
     */
    public void setUseLocalCache(boolean useLocalCache) {
        this.useLocalCache = useLocalCache;
    }

    /**
     * <p>If set to {@link com.j_spaces.core.client.SpaceURL#UPDATE_MODE_PULL} (<code>1</code>) each update triggers
     * an invalidation event at every cache instance. The invalidate event marks the object in the local cache
     * instances as invalid. Therefore, an attempt to read this object triggers a reload process in the master
     * space. This configuration is useful in cases where objects are updated frequently, but the updated value
     * is required by the application less frequently.
     *
     * <p>If set to {@link com.j_spaces.core.client.SpaceURL#UPDATE_MODE_PUSH} (<code>2</code>) the master pushes
     * the updates to the local cache, which holds a reference to the same updated object.
     *
     * <p>This flag only applies when setting {@link #setUseLocalCache(boolean)} to <code>true</code>.
     *
     * @see #setLocalCacheUpdateModeName(String)
     */
    public void setLocalCacheUpdateMode(int localCacheUpdateMode) {
        this.localCacheUpdateMode = localCacheUpdateMode;
    }

    /**
     * Allows to set the local cahce update mode using a descriptive name instead of integer constants using
     * {@link #setLocalCacheUpdateMode(int)}. Accepts either <code>push</code> or <code>pull</code>.
     *
     * @see #setLocalCacheUpdateMode(int)
     */
    public void setLocalCacheUpdateModeName(String localCacheUpdateModeName) {
        if (LOCAL_CACHE_UPDATE_MODE_PULL.equalsIgnoreCase(localCacheUpdateModeName)) {
            setLocalCacheUpdateMode(SpaceURL.UPDATE_MODE_PULL);
        } else if (LOCAL_CACHE_UPDATE_MODE_PUSH.equalsIgnoreCase(localCacheUpdateModeName)) {
            setLocalCacheUpdateMode(SpaceURL.UPDATE_MODE_PUSH);
        } else {
            throw new IllegalArgumentException("Wrong localCacheUpdateModeName [" + localCacheUpdateModeName + "], " +
                    "shoudl be either '" + LOCAL_CACHE_UPDATE_MODE_PULL + "' or '" + LOCAL_CACHE_UPDATE_MODE_PUSH + "'");
        }
    }

    /**
     * <p>The space local view proxy maintains a subset of the master space's data, allowing the client to
     * read distributed data without any remote operations.
     *
     * <p>Data is streamed into the client view in an implicit manner Ð as opposed to local cache data, that is
     * loaded into the client on-demand, and later updated or evicted.
     *
     * <p>The local view is defined via a filter Ð regular template or SQLQuery Ð as opposed to the local cache
     * that caches data at the client side only after the data has been read
     *
     * <p>If local views are set this factory will automatically set <code>useLocalCache</code> for the space.
     */
    public void setLocalViews(View[] localViews) {
        this.localViews = localViews;
    }

    /**
     * <p>Externally mananed override properties using open spaces extended config support. Should
     * not be set directly but allowed for different Spring context container to set it.
     */
    public void setMergedBeanLevelProperties(Properties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    protected IJSpace doCreateSpace() throws GigaSpaceException {
        spaceURL = doGetSpaceUrl();
        try {
            return (IJSpace) SpaceFinder.find(spaceURL);
        } catch (FinderException e) {
            throw new CannotCreateSpaceException("Failed to find space with url [" + spaceURL + "]", e);
        }
    }

    /**
     * <p>Parses the given space url using {@link com.j_spaces.core.client.SpaceURLParser}
     * and returns the parsed {@link com.j_spaces.core.client.SpaceURL}.
     *
     * <p>Uses the {@link #setUrlProperties(java.util.Properties)} and
     * {@link #setParameters(java.util.Map)} as parameters for the space. Also uses the
     * {@link #setClusterInfo(org.openspaces.core.cluster.ClusterInfo)} by automatically
     * translating the cluster information into relevant Space url properties.
     */
    protected SpaceURL doGetSpaceUrl() throws GigaSpaceException {
        Assert.notNull(url, "url property is required");
        Properties props = new Properties();
        // copy over the parameters
        if (parameters != null) {
            for (Iterator it = parameters.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                props.put(entry.getKey(), entry.getValue());
            }
        }

        // copy over the space properties
        if (urlProperties != null) {
            for (Iterator it = urlProperties.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                props.put(SpaceURL.PROPERTIES_SPACE_URL_ARG + "." + entry.getKey(), entry.getValue());
            }
        }

        if (schema != null) {
            props.put(spaceUrlProperty(SpaceURL.SCHEMA_NAME), schema);
        }

        props.put(spaceUrlProperty(SpaceURL.FIFO_MODE), Boolean.toString(fifo));

        if (lookupGroups != null) {
            props.put(spaceUrlProperty(SpaceURL.GROUPS), lookupGroups);
        }

        if (lookupTimeout != null) {
            props.put(spaceUrlProperty(SpaceURL.TIMEOUT), lookupTimeout.toString());
        }

        props.put(spaceUrlProperty(SpaceURL.VERSIONED), Boolean.toString(versioned));
        props.put(spaceUrlProperty(SpaceURL.NO_WRITE_LEASE), Boolean.toString(noWriteLease));
        props.put(spaceUrlProperty(SpaceURL.MIRROR), Boolean.toString(mirror));

        // handle local views and local caches
        if (localViews != null && localViews.length != 0) {
            props.put(spaceUrlProperty(SpaceURL.VIEWS), localViews);
            useLocalCache = true;
        }

        if (useLocalCache) {
            props.put(spaceUrlProperty(SpaceURL.USE_LOCAL_CACHE), Boolean.toString(useLocalCache).toLowerCase());
            props.put(spaceUrlProperty(SpaceURL.LOCAL_CACHE_UPDATE_MODE), Integer.toString(localCacheUpdateMode));
        }

        // copy over the external config overrides
        if (beanLevelProperties != null) {
            for (Iterator it = beanLevelProperties.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                props.put(entry.getKey(), entry.getValue());
            }
        }

        // if deploy info is provided, apply it to the space url
        if (clusterInfo != null) {
            if (clusterInfo.getNumberOfInstances() != null) {
                String totalMembers = clusterInfo.getNumberOfInstances().toString();
                if (clusterInfo.getNumberOfBackups() != null && clusterInfo.getNumberOfBackups().intValue() != 0) {
                    totalMembers += "," + clusterInfo.getNumberOfBackups();
                }
                props.setProperty(spaceUrlProperty(SpaceURL.CLUSTER_TOTAL_MEMBERS), totalMembers);
            }
            if (clusterInfo.getInstanceId() != null) {
                props.setProperty(spaceUrlProperty(SpaceURL.CLUSTER_MEMBER_ID), clusterInfo.getInstanceId().toString());
            }
            if (clusterInfo.getBackupId() != null) {
                props.setProperty(spaceUrlProperty(SpaceURL.CLUSTER_BACKUP_ID), clusterInfo.getBackupId().toString());
            }
            if (clusterInfo.getSchema() != null) {
                props.setProperty(spaceUrlProperty(SpaceURL.CLUSTER_SCHEMA), clusterInfo.getSchema());
            }
        }

        try {
            return SpaceURLParser.parseURL(url, props);
        } catch (MalformedURLException e) {
            throw new CannotCreateSpaceException("Failed to parse url [" + url + "]", e);
        }
    }

    private String spaceUrlProperty(String propertyName) {
        return SpaceURL.PROPERTIES_SPACE_URL_ARG + "." + propertyName;
    }
}
