package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.SpaceFinder;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.SpaceURLParser;
import org.openspaces.core.GigaSpaceException;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.properties.BeanLevelMergedPropertiesAware;
import org.openspaces.core.util.SpaceUtils;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;

/**
 * <p>A space factory bean that creates a space ({@link com.j_spaces.core.IJSpace IJSpace}) based on a url.
 *
 * <p>The factory allows to specify url properties using {@link #setUrlProperties(java.util.Properties)}
 * and space parameters using {@link #setParameters(java.util.Map)}. It also accepts a
 * {@link org.openspaces.core.cluster.ClusterInfo} using {@link #setClusterInfo(org.openspaces.core.cluster.ClusterInfo)}
 * and translates it into the relevant space url properties automatically.
 *
 * <p>Most url properties are explicitly exposed using different setters. Though they can also be set using
 * the {@link #setUrlProperties(java.util.Properties)} the explicit setters allow for more readable and simpler
 * configuration. Some examples of explicit url properties are: {@link #setSchema(String)}, {@link #setFifo(boolean)}.
 *
 * <p>The factory uses the {@link org.openspaces.core.properties.BeanLevelMergedPropertiesAware} in order to be injected
 * with properties that were not parameterized in advance (using ${...} notation). This will directly inject additional
 * properties in the Space creation/finding process.
 *
 * @author kimchy
 * @see com.j_spaces.core.client.SpaceURLParser
 * @see com.j_spaces.core.client.SpaceFinder
 */
public class UrlSpaceFactoryBean extends AbstractSpaceFactoryBean implements BeanLevelMergedPropertiesAware, ClusterInfoAware {

    private String url;

    private Map<String, Object> parameters;

    private Properties properties;

    private Properties urlProperties;

    private String schema;

    private String lookupGroups;

    private Integer lookupTimeout;

    private boolean versioned = true;

    private boolean noWriteLease = false;

    private boolean mirror = false;

    private boolean fifo = false;

    private Properties beanLevelProperties;

    private ClusterInfo clusterInfo;


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
    public UrlSpaceFactoryBean(String url, Map<String, Object> params) {
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
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Same as {@link #setParameters(java.util.Map) parameters} just with properties.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * <p>Sets the url properties. Note, most if not all url level properties can be set using
     * explicit setters.
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
        SpaceURL spaceURL = doGetSpaceUrl();
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

        props.put(SpaceUtils.spaceUrlProperty(SpaceURL.FIFO_MODE), Boolean.toString(fifo));

        if (lookupGroups != null) {
            props.put(SpaceUtils.spaceUrlProperty(SpaceURL.GROUPS), lookupGroups);
        }

        if (lookupTimeout != null) {
            props.put(SpaceUtils.spaceUrlProperty(SpaceURL.TIMEOUT), lookupTimeout.toString());
        }

        props.put(SpaceUtils.spaceUrlProperty(SpaceURL.VERSIONED), Boolean.toString(versioned));
        props.put(SpaceUtils.spaceUrlProperty(SpaceURL.NO_WRITE_LEASE), Boolean.toString(noWriteLease));
        props.put(SpaceUtils.spaceUrlProperty(SpaceURL.MIRROR), Boolean.toString(mirror));

        // copy over the external config overrides
        if (beanLevelProperties != null) {
            props.putAll(beanLevelProperties);
        }

        // if deploy info is provided, apply it to the space url
        if (clusterInfo != null) {
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
            if (clusterInfo.getSchema() != null) {
                props.setProperty(SpaceUtils.spaceUrlProperty(SpaceURL.CLUSTER_SCHEMA), clusterInfo.getSchema());
            }
        }

        try {
            return SpaceURLParser.parseURL(url, props);
        } catch (MalformedURLException e) {
            throw new CannotCreateSpaceException("Failed to parse url [" + url + "]", e);
        }
    }
}
