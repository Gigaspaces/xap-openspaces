package org.openspaces.core.space.cache;

import com.j_spaces.core.client.SpaceURL;

import java.util.Properties;

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
 * @author kimchy
 */
public class LocalCacheSpaceFactoryBean extends AbstractLocalCacheSpaceFactoryBean {

    private static final String LOCAL_CACHE_UPDATE_MODE_PUSH = "push";

    private static final String LOCAL_CACHE_UPDATE_MODE_PULL = "pull";
    
    private int localCacheUpdateMode = SpaceURL.UPDATE_MODE_PULL;

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

    
    protected Properties createCacheProeprties() {
        Properties props = new Properties();
        props.put(SpaceURL.LOCAL_CACHE_UPDATE_MODE, Integer.toString(localCacheUpdateMode));
        return props;
    }
}
