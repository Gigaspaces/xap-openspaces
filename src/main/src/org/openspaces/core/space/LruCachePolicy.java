package org.openspaces.core.space;

import com.j_spaces.core.Constants;

import java.util.Properties;

/**
 * Configures the Space to run in LRU mode. Defaults value for all configuration will be based on
 * the schema chosen.
 *
 * @author kimchy
 */
public class LruCachePolicy implements CachePolicy {

    private Integer size;

    private Integer initialLoadPercentage;

    public LruCachePolicy() {
    }

    /**
     * The number of entries to keep in the space. In all bulit in schemas of GigaSpaces, if this is
     * not set, the value is 100000.
     */
    public LruCachePolicy size(int size) {
        setSize(size);
        return this;
    }

    /**
     * When a space is running in a persistent mode (i.e. using the HibernateDataSource implementation),
     * The initial_load sets the % of the space cache data to be loaded (default is 50%)
     * maximum size. To disable this initial load phase, you should configure the initial_load value to be 0.
     */
    public LruCachePolicy initialLoadPercentage(int initialLoadPercentage) {
        setInitialLoadPercentage(initialLoadPercentage);
        return this;
    }

    /**
     * The number of entries to keep in the space. In all bulit in schemas of GigaSpaces, if this is
     * not set, the value is 100000.
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * When a space is running in a persistent mode (i.e. using the HibernateDataSource implementation),
     * The initial_load sets the % of the space cache data to be loaded (default is 50%)
     * maximum size. To disable this initial load phase, you should configure the initial_load value to be 0.
     */
    public void setInitialLoadPercentage(int initialLoadPercentage) {
        this.initialLoadPercentage = initialLoadPercentage;
    }

    public Properties toProps() {
        Properties props = new Properties();
        props.setProperty(Constants.CacheManager.FULL_CACHE_POLICY_PROP, "" + Constants.CacheManager.CACHE_POLICY_LRU);
        if (size != null) {
            props.setProperty(Constants.CacheManager.FULL_CACHE_MANAGER_SIZE_PROP, size.toString());
        }
        if (initialLoadPercentage != null) {
            props.setProperty(Constants.CacheManager.FULL_CACHE_MANAGER_INITIAL_LOAD_PROP, initialLoadPercentage.toString());
        }
        return props;
    }
}
