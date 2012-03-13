package org.openspaces.pu.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple implementation of {@link ServiceMonitors}.
 *
 * @author kimchy
 */
public class PlainServiceMonitors implements ServiceMonitors, Externalizable {

    private static final long serialVersionUID = -2530051320077906919L;
    
    protected String id;

    protected ServiceDetails details;

    //volatile is needed to support double null check (lazy evaluation) idiom
    protected volatile Map<String, Object> monitors;

    // Just for externalizable

    public PlainServiceMonitors() {
        this.monitors = new LinkedHashMap<String,Object>();
    }

    public PlainServiceMonitors(String id) {
        this();
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public Map<String, Object> getMonitors() {
        return this.monitors;
    }

    public ServiceDetails getDetails() {
        return details;
    }

    public void setDetails(ServiceDetails details) {
        this.details = details;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(id);
        if (getMonitors() == null) {
            out.writeInt(0);
        } else {
            out.writeInt(getMonitors().size());
            for (Map.Entry<String, Object> entry : getMonitors().entrySet()) {
                out.writeObject(entry.getKey());
                out.writeObject(entry.getValue());
            }
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readUTF();
        int attributesSize = in.readInt();
        for (int i = 0; i < attributesSize; i++) {
            String key = (String) in.readObject();
            Object value = in.readObject();
            monitors.put(key, value);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id[").append(id).append("] ");
        for (Map.Entry<String, Object> entry : getMonitors().entrySet()) {
            sb.append(entry.getKey()).append("[").append(entry.getValue()).append("] ");
        }
        return sb.toString();
    }
}