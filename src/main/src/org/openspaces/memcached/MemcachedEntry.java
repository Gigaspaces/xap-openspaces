package org.openspaces.memcached;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.gigaspaces.annotation.pojo.SpaceVersion;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author kimchy (shay.banon)
 */
@SpaceClass
public class MemcachedEntry implements Externalizable {

    private Key key;

    private byte[] value;

    private int flags;

    private int version;

    public MemcachedEntry() {
    }

    public MemcachedEntry(Key key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    @SpaceId(autoGenerate = false)
    @SpaceRouting
    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    @SpaceVersion
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        if (key == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            key.writeExternal(out);
        }
        if (value == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeInt(value.length);
            out.write(value);
        }
        out.writeInt(flags);
        out.writeInt(version);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            key = new Key();
            key.readExternal(in);
        }
        if (in.readBoolean()) {
            value = new byte[in.readInt()];
            in.readFully(value);
        }
        flags = in.readInt();
        version = in.readInt();
    }
}
