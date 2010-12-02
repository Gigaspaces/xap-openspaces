package org.openspaces.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class StringPropertiesUtils {
    
    public static void store(Map<String,String> properties, OutputStream out, String comments) throws IOException {
        Properties properties2 = new Properties();
        properties2.putAll(properties);
        properties2.store(out, comments);
    }
    
    public static Map<String,String> load(InputStream in) throws IOException {
        Map<String,String> properties = new HashMap<String,String>();
        Properties properties2 = new Properties();
        properties2.load(in);
        for ( Object key : properties2.keySet()) {
            properties.put(key.toString(), properties2.get(key).toString());
        }
        return properties;
    }
    
    public static Map<String,String> load(String filename) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(filename));
        try {
            return load(fileInputStream);
        }
        finally {
            fileInputStream.close();
        }
    }
    
    public static int getInteger(Map<String,String> properties, String key, int defaultValue) throws NumberFormatException {
        
        int intValue = defaultValue;
        
        Object value = properties.get(key);
        if (value != null ) {
            intValue = Integer.valueOf(value.toString());
        }
    
        return intValue;
    }

    public static int getIntegerIgnoreExceptions(Map<String,String> properties, String key, int defaultValue) {
        int intValue = defaultValue;
        try {
            intValue = getInteger(properties, key, defaultValue);
        }
        catch (NumberFormatException e) {
            //fallthrough
        }
        return intValue;
    }

    public static void putLong(Map<String,String> properties, String key, long value) throws NumberFormatException {
        properties.put(key, String.valueOf(value));
    }
    
    public static long getLong(Map<String,String> properties, String key, long defaultValue) throws NumberFormatException {
        
        long longValue = defaultValue;
        
        Object value = properties.get(key);
        if (value != null ) {
            longValue = Long.valueOf(value.toString());
        }
    
        return longValue;
    }

    public static long getLongIgnoreExceptions(Map<String,String> properties, String key, long defaultValue) {
        long longValue = defaultValue;
        try {
            longValue = getLong(properties, key, defaultValue);
        }
        catch (NumberFormatException e) {
            //fallthrough
        }
        return longValue;
    }
    
    /**
     * Concatenates the specified array into a combined string using the specified separator
     * and puts the result as a value into the specified properties with the specified key.
     * The values in the array must not contain the separator otherwise an IllegalArgumentException is raised.  
     * @param properties
     * @param key
     * @param array
     * @param seperator
     */
    public static void putArray(Map<String,String> properties, String key, String[] array, String separator) {
        StringBuilder concat = new StringBuilder();
        for (int i = 0 ; i < array.length ; i++) {
            String value = array[i];
            if (value != null && value.length() > 0) {
                if (value.contains(separator)) {
                    throw new IllegalArgumentException("array contains an element '"+value+"' that contains the seperator '"+separator+"'");
                }
                concat.append(value);
                if (i < array.length-1) {
                    concat.append(separator);
                }
            }
        }
        properties.put(key, concat.toString());
    }
    
    public static String[] getArray(Map<String,String> properties, String key, String separator, String[] defaultValue) {
        String[] array = defaultValue;
        String value = properties.get(key);
        if (value != null) {
            array = value.split(java.util.regex.Pattern.quote(separator));
        }
        return array;
    }

    public static void putInteger(Map<String, String> properties, String key, int value) {
        properties.put(key,String.valueOf(value));
    }

    public static void putBoolean(Map<String, String> properties, String key, boolean value) {
        properties.put(key,String.valueOf(value));
    }
    
    public static boolean getBoolean(Map<String, String> properties, String key, boolean defaultValue) {
        boolean booleanValue = defaultValue;
        String value = properties.get(key);
        
        //don't use Boolean.valueOf() since it always defaults to false 
        if (value != null) {
            if (value.equalsIgnoreCase("true")) {
                booleanValue = true;
            }
            else if (value.equalsIgnoreCase("false")) {
                booleanValue = false;
            }
            else {
                throw new IllegalArgumentException(key + " must be either true or false. The value " + key + " is illegal.");
            }
        }
        return booleanValue;
    }

    public static Map<String, String> getMap(Map<String, String> properties, String keyPrefix, Map<String, String> defaultValue) {
        Map<String,String> value = new HashMap<String,String>();
        for (String key : properties.keySet()) {
            if (key.startsWith(keyPrefix)) {
                String newKey = key.substring(keyPrefix.length());
                value.put(newKey, properties.get(key));
            }
        }
        if (value.size() == 0) {
            value = defaultValue;
        }
        return value;

    }
    
    public static void putMap(Map<String, String> properties, String keyPrefix, Map<String, String> value) {
        if (properties == value) {
            throw new IllegalArgumentException("properties and value must be different objects");
          }
          
          // delete old properties starting with the key prefix
          Set<String> keysToDelete = new HashSet();
          for (String key : properties.keySet()) {
              if (key.toString().startsWith(keyPrefix)) {
                  keysToDelete.add(key);
              }
          }
          
          for (String key : keysToDelete) {
              properties.remove(key);
          }
      
          // add new properties with the new key prefix
          for (String key : value.keySet()) {
              properties.put(keyPrefix+key, value.get(key));
          }
    }
}

