package org.openspaces.core.space;

import com.j_spaces.core.Constants;

import java.util.Properties;

/**
 * A cache policy that stores all the data in the space.
 *
 * @author kimchy
 */
public class AllInCachePolicy implements CachePolicy {

    public Properties toProps() {
        Properties props = new Properties();
        props.setProperty(Constants.CacheManager.FULL_CACHE_POLICY_PROP, "" + Constants.CacheManager.CACHE_POLICY_ALL_IN_CACHE);
        return props;
    }
}
