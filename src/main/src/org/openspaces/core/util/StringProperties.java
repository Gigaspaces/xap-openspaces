package org.openspaces.core.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StringProperties {
    
    Map<String,String> properties;

    public StringProperties() {
        this(new HashMap<String,String>());
    }
    
    public StringProperties(Properties properties) {
        this(StringPropertiesUtils.convertPropertiesToMapStringString(properties));
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
    
    public String get(String key, String defaultValue) {
        String value = properties.get(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
    
    public StringProperties putInteger(String key, int value) {
        StringPropertiesUtils.putInteger(properties,key,value);
        return this;
    }
    
    public int getInteger(String key, int defaultValue) throws NumberFormatException{
        return StringPropertiesUtils.getInteger(properties, key, defaultValue);
    }
    
    public int getIntegerIgnoreExceptions(String key, int defaultValue) {
        return StringPropertiesUtils.getIntegerIgnoreExceptions(properties, key, defaultValue);
    }
    
    public StringProperties putLong(String key, long value) {
        StringPropertiesUtils.putLong(properties,key,value);
        return this;
    }
    
    public long getLong(String key, long defaultValue) throws NumberFormatException{
        return StringPropertiesUtils.getLong(properties, key, defaultValue);
    }
    
    public long getLongIgnoreExceptions(String key, long defaultValue) {
        return StringPropertiesUtils.getLongIgnoreExceptions(properties, key, defaultValue);
    }

    public StringProperties putDouble(String key, double value) {
        StringPropertiesUtils.putDouble(properties,key,value);
        return this;
    }
    
    public double getDouble(String key, double defaultValue) throws NumberFormatException{
        return StringPropertiesUtils.getDouble(properties, key, defaultValue);
    }
    
    public double getDoubleIgnoreExceptions(String key, double defaultValue) {
        return StringPropertiesUtils.getDoubleIgnoreExceptions(properties, key, defaultValue);
    }
    
    public String[] getArray(String key, String separator, String[] defaultValue) {
        return StringPropertiesUtils.getArray(properties, key, separator, defaultValue);
    }
    
    public StringProperties putArray(String key, String[] value, String separator) {
        StringPropertiesUtils.putArray(properties, key, value, separator);
        return this;
    }
    public void store(OutputStream out, String comments) throws IOException {
        StringPropertiesUtils.store(properties, out, comments);
    }

   public Map<String,String> getProperties() {
       return this.properties;
   }

   public void putBoolean(String key, boolean value) {
       StringPropertiesUtils.putBoolean(properties , key , value);
   }
   
   public boolean getBoolean(String key, boolean defaultValue) {
       return StringPropertiesUtils.getBoolean(properties , key , defaultValue);
   }

   public Map<String,String> getMap(String keyPrefix, Map<String,String> defaultValue) {
       return StringPropertiesUtils.getMap(properties,keyPrefix,defaultValue);
   }

   public void putMap(String keyPrefix, Map<String,String> value) {
       StringPropertiesUtils.putMap(properties,keyPrefix,value);
   }
   
   public void putArgumentsArray(String key, String[] value) {
       StringPropertiesUtils.putArgumentsArray(properties, key, value);
   }
 
   public String[] getArgumentsArray(String key, String[] defaultValue) {
       return StringPropertiesUtils.getArgumentsArray(properties, key, defaultValue);
   }

   public void clear() {
       this.properties.clear();
   }
   
   public void putAll(Map<String, String> properties) {
       this.properties.putAll(properties);
   }

   public void remove(String key) {
       this.properties.remove(key);
   }
   
   public void putKeyValuePairs(String key, Map<String, String> value, String pairSeperator, String keyValueSeperator) {
       StringPropertiesUtils.putKeyValuePairs(properties, key, value, pairSeperator, keyValueSeperator);
   }

   public Map<String,String> getKeyValuePairs(String key, String pairSeperator, String keyValueSeperator, Map<String, String> defaultValue ) {
       return StringPropertiesUtils.getKeyValuePairs(properties, key, pairSeperator, keyValueSeperator, defaultValue);
   }
   
   @Override
   public String toString() {
       return StringPropertiesUtils.toString(properties);
   }

   @Override
   public boolean equals(Object other) {
       return other instanceof StringProperties &&
              this.properties.equals(((StringProperties)other).properties);
   }
   
   @Override
   public int hashCode() {
       return this.properties.hashCode();
   }

}