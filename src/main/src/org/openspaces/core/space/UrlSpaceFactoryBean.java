package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.SpaceFinder;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.SpaceURLParser;
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
 * <p>A space factory bean that creates a space based on a url and optional
 * map parameters.
 *
 * @author kimchy
 */
public class UrlSpaceFactoryBean extends AbstractSpaceFactoryBean implements BeanLevelMergedPropertiesAware, ClusterInfoAware {

    private String url;

    private Map parameters;

    private Properties urlProperties;

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
     * <p>Externally mananed override properties using open spaces extended config support. Should
     * not be set directly but allowed for different Spring context container to set it.
     */
    public void setMergedBeanLevelProperties(Properties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    protected boolean isEmbeddedSpace() {
        return !(spaceURL.getProtocol().equals(SpaceURL.JINI_PROTOCOL) || spaceURL.getProtocol().equals(SpaceURL.RMI_PROTOCOL));
    }

    protected IJSpace doCreateSpace() throws GigaSpaceException {
        spaceURL = doGetSpaceUrl();
        try {
            return (IJSpace) SpaceFinder.find(spaceURL);
        } catch (FinderException e) {
            throw new CannotCreateSpaceException("Failed to find space with url [" + spaceURL + "]");
        }
    }

    /**
     * Parses the given space url using {@link com.j_spaces.core.client.SpaceURLParser}
     * and returns the parsed {@link com.j_spaces.core.client.SpaceURL}.
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
                if (clusterInfo.getNumberOfBackups() != null) {
                    totalMembers += "," + clusterInfo.getNumberOfBackups();
                }
                props.setProperty(SpaceURL.PROPERTIES_SPACE_URL_ARG + "." + SpaceURL.CLUSTER_TOTAL_MEMBERS, totalMembers);
            }
            if (clusterInfo.getInstanceId() != null) {
                props.setProperty(SpaceURL.PROPERTIES_SPACE_URL_ARG + "." + SpaceURL.CLUSTER_MEMBER_ID, clusterInfo.getInstanceId().toString());
            }
            if (clusterInfo.getBackupId() != null) {
                props.setProperty(SpaceURL.PROPERTIES_SPACE_URL_ARG + "." + SpaceURL.CLUSTER_BACKUP_ID, clusterInfo.getBackupId().toString());
            }
            if (clusterInfo.getSchema() != null) {
                props.setProperty(SpaceURL.PROPERTIES_SPACE_URL_ARG + "." + SpaceURL.CLUSTER_SCHEMA, clusterInfo.getSchema());
            }
        }

        try {
            return SpaceURLParser.parseURL(url, props);
        } catch (MalformedURLException e) {
            throw new CannotCreateSpaceException("Failed to parse url [" + url + "]", e);
        }
    }
}
