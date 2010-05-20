package org.openspaces.memcached;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 */
public interface Cache<CACHE_ELEMENT extends CacheElement> {
    /**
     * Enum defining response statuses from set/add type commands
     */
    public enum StoreResponse {
        STORED, NOT_STORED, EXISTS, NOT_FOUND
    }

    /**
     * Enum defining responses statuses from removal commands
     */
    public enum DeleteResponse {
        DELETED, NOT_FOUND
    }

    /**
     * Handle the deletion of an item from the cache.
     *
     * @param key  the key for the item
     * @param time an amount of time to block this entry in the cache for further writes
     * @return the message response
     */
    DeleteResponse delete(Key key, int time);

    /**
     * Add an element to the cache
     *
     * @param e the element to add
     * @return the store response code
     */
    StoreResponse add(CACHE_ELEMENT e);

    /**
     * Replace an element in the cache
     *
     * @param e the element to replace
     * @return the store response code
     */
    StoreResponse replace(CACHE_ELEMENT e);

    /**
     * Append bytes to the end of an element in the cache
     *
     * @param element the element to append
     * @return the store response code
     */
    StoreResponse append(CACHE_ELEMENT element);

    /**
     * Prepend bytes to the end of an element in the cache
     *
     * @param element the element to append
     * @return the store response code
     */
    StoreResponse prepend(CACHE_ELEMENT element);

    /**
     * Set an element in the cache
     *
     * @param e the element to set
     * @return the store response code
     */
    StoreResponse set(CACHE_ELEMENT e);

    /**
     * Set an element in the cache but only if the element has not been touched
     * since the last 'gets'
     *
     * @param cas_key the cas key returned by the last gets
     * @param e       the element to set
     * @return the store response code
     */
    StoreResponse cas(Long cas_key, CACHE_ELEMENT e);

    /**
     * Increment/decremen t an (integer) element in the cache
     *
     * @param key the key to increment
     * @param mod the amount to add to the value
     * @return the message response
     */
    Integer get_add(Key key, int mod);

    /**
     * Get element(s) from the cache
     *
     * @param keys the key for the element to lookup
     * @return the element, or 'null' in case of cache miss.
     */
    CACHE_ELEMENT[] get(Key... keys);

    /**
     * Flush all cache entries
     *
     * @return command response
     */
    boolean flush_all();

    /**
     * Flush all cache entries with a timestamp after a given expiration time
     *
     * @param expire the flush time in seconds
     * @return command response
     */
    boolean flush_all(int expire);

    /**
     * Close the cache, freeing all resources on which it depends.
     *
     * @throws java.io.IOException
     */
    void close() throws IOException;


    /**
     * @return the # of items in the cache
     */
    long getCurrentItems();

    /**
     * @return the maximum size of the cache (in bytes)
     */
    long getLimitMaxBytes();

    /**
     * @return the current cache usage (in bytes)
     */
    long getCurrentBytes();

    /**
     * @return the number of get commands executed
     */
    long getGetCmds();

    /**
     * @return the number of set commands executed
     */
    long getSetCmds();

    /**
     * @return the number of get hits
     */
    long getGetHits();

    /**
     * @return the number of stats
     */
    long getGetMisses();

    /**
     * Retrieve stats about the cache. If an argument is specified, a specific category of stats is requested.
     *
     * @param arg a specific extended stat sub-category
     * @return a map of stats
     */
    Map<String, Set<String>> stat(String arg);

    /**
     * Called periodically by the network event loop to process any pending events.
     * (such as delete queues, etc.)
     */
    void asyncEventPing();

}