package org.openspaces.core.space.cache;

import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.view.View;
import org.springframework.util.Assert;

import java.util.Properties;

/**
 * <p>The space local view proxy maintains a subset of the master space's data, allowing the client to
 * read distributed data without any remote operations.
 *
 * <p>Data is streamed into the client view in an implicit manner Ð as opposed to local cache data
 * ({@link org.openspaces.core.space.cache.LocalCacheSpaceFactoryBean}), that is loaded into the client on-
 * demand, and later updated or evicted.
 *
 * <p>The local view is defined via a filter ({@link #setLocalViews(com.j_spaces.core.client.view.View[])} localViews)
 * Ð as opposed to the local cache that caches data at the client side only after the data has been read
 *
 * @author kimchy
 */
public class LocalViewSpaceFactoryBean extends AbstractLocalCacheSpaceFactoryBean {

    private View[] localViews;

    /**
     * Sets an array of filters/views that define what portion of the data from the master space
     * wiil be streamed to this local view.
     */
    public void setLocalViews(View[] localViews) {
        this.localViews = localViews;
    }


    public void afterPropertiesSet() throws Exception {
        Assert.notNull(localViews, "localViews must be set");
        Assert.isTrue(localViews.length > 0, "At least one local view must be defined");
        super.afterPropertiesSet();
    }

    /**
     * Creates newly created properties that holds the views set using
     * {@link #setLocalViews(com.j_spaces.core.client.view.View[]) locaViews}.
     */
    protected Properties createCacheProeprties() {
        Properties props = new Properties();
        props.put(SpaceURL.VIEWS, localViews);
        return props;
    }
}
