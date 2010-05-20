package org.openspaces.memcached;

import java.util.Arrays;

/**
 * Represents a given key for lookup in the cache.
 *
 * Wraps a byte array with a precomputed hashCode.
 */
public class Key {
    public byte[] bytes;
    private int hashCode;

    public Key(byte[] bytes) {
        this.bytes = bytes;
        this.hashCode = Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key1 = (Key) o;

        if (!Arrays.equals(bytes, key1.bytes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }


}