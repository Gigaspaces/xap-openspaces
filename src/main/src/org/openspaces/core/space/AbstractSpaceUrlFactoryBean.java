package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.SpaceFinder;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.SpaceURLValidator;
import org.openspaces.core.GigaSpaceException;
import org.openspaces.core.config.BeanLevelMergedPropertiesAware;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * <p>Base class for space factory beans that create the space based on a
 * {@link com.j_spaces.core.client.SpaceURL} using {@link SpaceFinder}.
 *
 * @author kimchy
 * @see com.j_spaces.core.client.SpaceURL
 * @see com.j_spaces.core.client.SpaceFinder
 */
public abstract class AbstractSpaceUrlFactoryBean extends AbstractSpaceFactoryBean implements BeanLevelMergedPropertiesAware {

    private Properties beanLevelProperties;

    public void setMergedBeanLevelProperties(Properties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    protected IJSpace doCreateSpace() throws GigaSpaceException {
        SpaceURL spaceURL = doGetSpaceUrl();
        // apply cluster information if provided
        if (beanLevelProperties != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Space uses bean level properties " + beanLevelProperties);
            }
            for (Iterator it = beanLevelProperties.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                if (entry.getValue() instanceof String) {
                    spaceURL.setProperty((String) entry.getKey(), (String) entry.getValue());
                } else {
                    spaceURL.put(entry.getKey(), entry.getValue());
                }
            }
        }
        // now perform validation on the Space URL
        // TODO validation fails when passing properties that get injected using ${spaceUrl} since it gets injected as properties as well
//        try {
//            SpaceURLValidator.validate(spaceURL);
//        } catch (Exception e) {
//            throw new CannotCreateSpaceException("Failed ot validate space url [" + spaceURL + "]", e);
//        }
        try {
            SpaceURLValidator.validateClusterSchemaAttributes(spaceURL);
        } catch (MalformedURLException e) {
            throw new CannotCreateSpaceException("Failed to validate space url cluster information [" + spaceURL + "]", e);
        }
        try {
            return (IJSpace) SpaceFinder.find(spaceURL);
        } catch (FinderException e) {
            throw new CannotCreateSpaceException("Failed to find space with url [" + spaceURL + "]", e);
        }
    }

    /**
     * Returns the {@link com.j_spaces.core.client.SpaceURL} used to create the space. Note, the SpaceURL
     * should not perform any validation as the {@link #doCreateSpace()} will perform the validation after
     * the addition of cluster information.
     */
    protected abstract SpaceURL doGetSpaceUrl() throws GigaSpaceException;
}
