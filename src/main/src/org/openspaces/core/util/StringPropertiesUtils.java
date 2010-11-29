package org.openspaces.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
    
    public static int getInteger(Map<String,String> properties, String key, int defaultValue) throws NumberFormatException {
        
        int intValue = defaultValue;
        
        String value = properties.get(key);
        if (value != null ) {
            intValue = Integer.valueOf(value);
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

    public static void setLong(Map<String,String> properties, String key, long value) throws NumberFormatException {
        properties.put(key, String.valueOf(value));
    }
    
    public static long getLong(Map<String,String> properties, String key, long defaultValue) throws NumberFormatException {
        
        long longValue = defaultValue;
        
        String value = properties.get(key);
        if (value != null ) {
            longValue = Long.valueOf(value);
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
    public static void setArray(Map<String,String> properties, String key, String[] array, String separator) {
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
    
    public static String[] getArray(Map<String,String> properties, String key, String sep, String[] defaultArray) {
        String[] array = defaultArray;
        String value = properties.get(key);
        if (value != null) {
            array = value.split(java.util.regex.Pattern.quote(sep));
        }
        return array;
    }
    
}