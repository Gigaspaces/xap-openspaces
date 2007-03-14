package org.openspaces.core.space.cache;

import java.util.List;
import java.util.Properties;

import org.springframework.util.Assert;

import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.view.View;

/**
 * The space local view proxy maintains a subset of the master space's data, allowing the client to
 * read distributed data without any remote operations.
 * 
 * <p>
 * Data is streamed into the client view in an implicit manner Ð as opposed to local cache data ({@link LocalCacheSpaceFactoryBean}),
 * that is loaded into the client on- demand, and later updated or evicted.
 * 
 * <p>
 * The local view is defined via a filter ({@link #setLocalViews(java.util.List)} localViews) Ð as
 * opposed to the local cache that caches data at the client side only after the data has been read
 * 
 * @author kimchy
 */
public class LocalViewSpaceFactoryBean extends AbstractLocalCacheSpaceFactoryBean {

    private List<View<?>> localViews;

    /**
     * Sets an array of filters/views that define what portion of the data from the master space
     * wiil be streamed to this local view.
     */
    public void setLocalViews(List<View<?>> localViews) {
        this.localViews = localViews;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(localViews, "localViews must be set");
        Assert.isTrue(localViews.size() > 0, "At least one local view must be defined");
        super.afterPropertiesSet();
    }

    /**
     * Creates newly created properties that holds the views set using
     * {@link #setLocalViews(java.util.List)} locaViews}.
     */
    protected Properties createCacheProeprties() {
        Properties props = new Properties();
        props.put(SpaceURL.VIEWS, localViews.toArray(new View<?>[localViews.size()]));
        return props;
    }
}
