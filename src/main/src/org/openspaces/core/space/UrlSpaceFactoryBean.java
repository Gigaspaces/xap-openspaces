package org.openspaces.core.space;

import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.SpaceURLParser;
import org.openspaces.core.GigaSpaceException;
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
public class UrlSpaceFactoryBean extends AbstractSpaceUrlFactoryBean {

    private String url;

    private Map parameters;

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
     * @param parameters The parameters to create the {@link com.j_spaces.core.IJSpace} with.
     */
    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    /**
     * Parses the given space url using {@link com.j_spaces.core.client.SpaceURLParser}
     * and returns the parsed {@link com.j_spaces.core.client.SpaceURL}.
     */
    protected SpaceURL doGetSpaceUrl() throws GigaSpaceException {
        Assert.notNull(url, "url property is required");
        Properties props = new Properties();
        if (parameters != null) {
            for (Iterator it = parameters.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                props.put(entry.getKey(), entry.getValue());
            }
        }
        // add ignore validation as it will be performed by the base class
        props.setProperty(SpaceURL.IGNORE_VALIDATION, "true");
        try {
            return SpaceURLParser.parseURL(url, props);
        } catch (MalformedURLException e) {
            throw new CannotCreateSpaceException("Failed to parse url [" + url + "]", e);
        }
    }
}
