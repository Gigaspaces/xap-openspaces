package org.openspaces.utest.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openspaces.core.util.StringPropertiesUtils;

public class StringPropertiesUtilsTest {

    private final String key = "key";
    private final String missingkey = "missingkey";
    private Map<String,String> map;
    
    @Before
    public void setup() {
        map = new HashMap<String,String>();
    }
    
    @Test
    public void testBooleanTrue() {
        StringPropertiesUtils.putBoolean(map,key,true);
        assertTrue(StringPropertiesUtils.getBoolean(map, key, false));
        map.put(key, "true");
        assertTrue(StringPropertiesUtils.getBoolean(map, key, false));
        map.put(key, "TRUE");
        assertTrue(StringPropertiesUtils.getBoolean(map, key, false));
        assertTrue(StringPropertiesUtils.getBoolean(map, missingkey, true));
    }
    
    @Test
    public void testBooleanFalse() {
        StringPropertiesUtils.putBoolean(map,key,false);
        assertFalse(StringPropertiesUtils.getBoolean(map, key, true));
        map.put(key, "false");
        assertFalse(StringPropertiesUtils.getBoolean(map, key, true));
        map.put(key, "FALSE");
        assertFalse(StringPropertiesUtils.getBoolean(map, key, true));
        assertFalse(StringPropertiesUtils.getBoolean(map, missingkey, false));
    }
    
    @Test
    public void testInteger() {
        StringPropertiesUtils.putInteger(map,key,Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE,StringPropertiesUtils.getInteger(map, key, 0));
        assertEquals(1,StringPropertiesUtils.getInteger(map, missingkey, 1));
    }
    
    @Test(expected=NumberFormatException.class)
    public void testIntegerOverflowError() {
        StringPropertiesUtils.putLong(map,key,Long.MAX_VALUE);
        StringPropertiesUtils.getInteger(map, key, 0);
        fail();
    }
    
    @Test(expected=NumberFormatException.class)
    public void testIntegerUnderflowError() {
        StringPropertiesUtils.putLong(map,key,Long.MIN_VALUE);
        StringPropertiesUtils.getInteger(map, key, 0);
        fail();
    }
    
    @Test(expected=NumberFormatException.class)
    public void testIntegerParsingError() {
        map.put(key, "notaninteger");
        assertEquals(1,StringPropertiesUtils.getIntegerIgnoreExceptions(map, key, 1));
        StringPropertiesUtils.getInteger(map, key, 0);
        fail();
    }
    
    @Test
    public void testLong() {
        StringPropertiesUtils.putLong(map,key,Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE,StringPropertiesUtils.getLong(map, key, 0));
        assertEquals(1,StringPropertiesUtils.getInteger(map, missingkey, 1));
    }
    
    @Test(expected=NumberFormatException.class)
    public void testLongParsingError() {
        map.put(key, "notaninteger");
        assertEquals(1,StringPropertiesUtils.getLongIgnoreExceptions(map, key, 1));
        StringPropertiesUtils.getLong(map, key, 0);
        fail();
    }
    
    @Test
    public void testMap() {
        Map<String,String> inner = new HashMap<String,String>();
        inner.put(key, "value");
        StringPropertiesUtils.putMap(map, "prefix.", inner);
        assertEquals(inner,StringPropertiesUtils.getMap(map, "prefix.", inner));
    }
    
    @Test
    public void testArray() {
        String[] inner = new String[] { "a","b","c"};
        StringPropertiesUtils.putArray(map, key, inner," ");
        assertArrayEquals(inner,StringPropertiesUtils.getArray(map, key, " ", new String[]{}));
        assertArrayEquals(new String[]{},StringPropertiesUtils.getArray(map, missingkey, " ", new String[]{}));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testArrayIllegalArgument() {
        StringPropertiesUtils.putArray(map, key, new String[] { "a b","c"}," ");
        fail();
    }
    
    @Test
    public void testArgumentsArray() {
        String[] inner = new String[]   {"a b", "'b c'","\"c d\"", "\"'d e'\"", "'\"e f\"'"};
        String[] expected= new String[] {"a b",  "b c" ,  "c d"  ,   "'d e'"  ,  "\"e f\""};
        StringPropertiesUtils.putArgumentsArray(map, key, inner);
        assertArrayEquals(expected,StringPropertiesUtils.getArgumentsArray(map, key, new String[]{}));
        assertArrayEquals(new String[]{},StringPropertiesUtils.getArgumentsArray(map, missingkey, new String[]{}));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testArgumentsArrayIllegalArgument() {
        StringPropertiesUtils.putArray(map, key, new String[] { "'a' b'"}," ");
        fail();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testArgumentsArrayIllegalArgument2() {
        StringPropertiesUtils.putArray(map, key, new String[] { "\"a\" b\""}," ");
        fail();
    }
}
