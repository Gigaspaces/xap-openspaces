package org.openspaces.core.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class StringProperties {
    
    Map<String,String> properties;

    public StringProperties() {
        this(new HashMap<String,String>());
    }
    
    public StringProperties(Map<String,String> properties) {
        this.properties = properties;
    }
    
    public StringProperties put(String key, String value) {
        properties.put(key,value);
        return this;
    }
    
    public String get(String key) {
        return properties.get(key);
    }
    
    public int getInteger(String key, int defaultValue) throws NumberFormatException{
        return StringPropertiesUtils.getInteger(properties, key, defaultValue);
    }
    
    public int getIntegerIgnoreExceptions(String key, int defaultValue) {
        return StringPropertiesUtils.getIntegerIgnoreExceptions(properties, key, defaultValue);
    }
    
    public void store(OutputStream out, String comments) throws IOException {
        StringPropertiesUtils.store(properties, out, comments);
    }

   
    
}
