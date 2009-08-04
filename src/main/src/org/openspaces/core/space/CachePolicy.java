package org.openspaces.core.space;

import java.util.Properties;

/**
 * A generic interface for cache policies.
 *
 * @author kimchy
 */
public interface CachePolicy {

    Properties toProps();
}
