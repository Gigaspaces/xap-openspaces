package org.openspaces.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gigaspaces.internal.utils.StringUtils;

public class StringPropertiesUtils {
    
    public static void store(Map<String,String> properties, OutputStream out, String comments) throws IOException {
        Properties properties2 = new Properties();
        properties2.putAll(properties);
        properties2.store(out, comments);
    }
    
    public static Map<String,String> load(InputStream in) throws IOException {
        
        Properties properties = new Properties();
        properties.load(in);
        return convertPropertiesToMapStringString(properties);
    }

    public static Map<String, String> convertPropertiesToMapStringString(Properties properties2) {
        Map<String,String> properties = new HashMap<String,String>();
        for ( Map.Entry<Object, Object> entry : properties2.entrySet()) {
            properties.put(entry.getKey().toString(), entry.getValue().toString());
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

    public static void putLong(Map<String,String> properties, String key, long value) {
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

    public static void putDouble(Map<String,String> properties, String key, double value) {
        properties.put(key, String.valueOf(value));
    }
    
    public static double getDouble(Map<String,String> properties, String key, double defaultValue) throws NumberFormatException {
        
        double doubleValue = defaultValue;
        
        Object value = properties.get(key);
        if (value != null ) {
            doubleValue = Double.valueOf(value.toString());
        }
    
        return doubleValue;
    }

    public static double getDoubleIgnoreExceptions(Map<String,String> properties, String key, double defaultValue) {
        double doubleValue = defaultValue;
        try {
            doubleValue = getDouble(properties, key, defaultValue);
        }
        catch (NumberFormatException e) {
            //fallthrough
        }
        return doubleValue;
    }

    /**
     * Concatenates the specified array into a combined string using the specified separator
     * and puts the result as a value into the specified properties with the specified key.
     * The values in the array must not contain the separator otherwise an IllegalArgumentException is raised.  
     * @param properties
     * @param key
     * @param array
     * @param separator
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

    /**
     * Concatenates the specified array into a combined string using the space separator
     * and puts the result as a value into the specified properties with the specified key.
     * If the values in the array contains whitespace it is enclosed with " or ' characters
     */
    public static void putArgumentsArray(Map<String,String> properties, String key, String[] array) {
        StringBuilder concat = new StringBuilder();
        for (int i = 0 ; i < array.length ; i++) {
            String value = array[i];
            if (value != null && value.length() > 0) {
                if (value.contains(" ")) {
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        if (value.substring(1,value.length()-1).contains("\"")) {
                            throw new IllegalArgumentException("Argument " + value + " contains both a whitespace and a \" character.");
                        }
                    }
                    else if (value.startsWith("'") && value.endsWith("'")) {
                        if (value.substring(1,value.length()-1).contains("'")) {
                            throw new IllegalArgumentException("Argument " + value + " contains both a whitespace and a ' character.");
                        }
                    }
                    else if (!value.contains("\"")) {
                        value = "\"" + value + "\"";
                    }
                    else if (!value.contains("'")) {
                        value = "'" + value + "'";
                    }
                    else {
                        throw new IllegalArgumentException("Argument " + value + " contains both a whitespace and \" and '");
                    }
                }
                concat.append(value);
                if (i < array.length-1) {
                    concat.append(' ');
                }
            }
        }
        properties.put(key, concat.toString());
    }
    
    public static String[] getArgumentsArray(Map<String,String> properties, String key, String[] defaultValue) {
        String[] array = defaultValue;
        String value = properties.get(key);
        if (value != null) {
            List<String> matchList = new ArrayList<String>();
            Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
            Matcher regexMatcher = regex.matcher(value);
            while (regexMatcher.find()) {
                if (regexMatcher.group(1) != null) {
                    // Add double-quoted string without the quotes
                    matchList.add(regexMatcher.group(1));
                } else if (regexMatcher.group(2) != null) {
                    // Add single-quoted string without the quotes
                    matchList.add(regexMatcher.group(2));
                } else {
                    // Add unquoted word
                    matchList.add(regexMatcher.group());
                }
           }
           array = matchList.toArray(new String[matchList.size()]);
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

    public static String toString(Map<String, String> properties) {
        //sort and print
        return new TreeMap(properties).toString();
    }

    public static void putKeyValuePairs(
            Map<String, String> properties, String key, Map<String, String> value,
            String pairSeperator, String keyValueSeperator) {
        
        putArray(properties,key, StringUtils.convertKeyValuePairsToArray(value, keyValueSeperator), pairSeperator);
    }

    public static Map<String,String> getKeyValuePairs(Map<String, String> properties, String key, String pairSeperator, String keyValueSeperator, Map<String,String> defaultValue) {
        String[] pairs = getArray(properties, key, pairSeperator, StringUtils.convertKeyValuePairsToArray(defaultValue, keyValueSeperator));
        return StringUtils.convertArrayToKeyValuePairs(pairs, keyValueSeperator);
    }

}

