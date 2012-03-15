/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.utest.core.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.openspaces.core.util.StringPropertiesUtils;

public class StringPropertiesUtilsTest extends TestCase {


    private final String key = "key";
    private final String missingkey = "missingkey";
    private Map<String,String> map;
    
    @Before
    @Override
    public void setUp() {
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
    
    @Test
    public void testIntegerOverflowError() {
        try {
            StringPropertiesUtils.putLong(map,key,Long.MAX_VALUE);
            StringPropertiesUtils.getInteger(map, key, 0);
            fail();
        } catch(NumberFormatException e) {
            /*expected result*/
        }
    }
    
    @Test
    public void testIntegerUnderflowError() {
        try {
        StringPropertiesUtils.putLong(map,key,Long.MIN_VALUE);
        StringPropertiesUtils.getInteger(map, key, 0);
        fail();
        } catch(NumberFormatException e) {
            /*expected result*/
        }
    }
    
    @Test
    public void testIntegerParsingError() {
        try {
            map.put(key, "notaninteger");
            assertEquals(1,StringPropertiesUtils.getIntegerIgnoreExceptions(map, key, 1));
            StringPropertiesUtils.getInteger(map, key, 0);
            fail();
        } catch(NumberFormatException e) {
            /*expected result*/
        }
    }
    
    @Test
    public void testLong() {
        StringPropertiesUtils.putLong(map,key,Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE,StringPropertiesUtils.getLong(map, key, 0));
        assertEquals(1,StringPropertiesUtils.getInteger(map, missingkey, 1));
    }
    
    @Test
    public void testLongParsingError() {
        try {
        map.put(key, "notaninteger");
        assertEquals(1,StringPropertiesUtils.getLongIgnoreExceptions(map, key, 1));
        StringPropertiesUtils.getLong(map, key, 0);
        fail();
        } catch(NumberFormatException e) {
            /*expected result*/
        }
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
        assertEquals(Arrays.asList(inner),Arrays.asList(StringPropertiesUtils.getArray(map, key, " ", new String[]{})));
        assertEquals(0,StringPropertiesUtils.getArray(map, missingkey, " ", new String[]{}).length);
    }

    @Test
    public void testArrayIllegalArgument() {
        try {
            StringPropertiesUtils.putArray(map, key, new String[] { "a b","c"}," ");
            fail();
        }
        catch(IllegalArgumentException e) {
            /*expected result*/
        }
    }
    
    @Test
    public void testArgumentsArray() {
        String[] inner = new String[]   {"a b", "'b c'","\"c d\"", "\"'d e'\"", "'\"e f\"'"};
        String[] expected= new String[] {"a b",  "b c" ,  "c d"  ,   "'d e'"  ,  "\"e f\""};
        StringPropertiesUtils.putArgumentsArray(map, key, inner);
        assertEquals(Arrays.asList(expected),
                     Arrays.asList(StringPropertiesUtils.getArgumentsArray(map, key, new String[]{})));
        assertEquals(0,StringPropertiesUtils.getArgumentsArray(map, missingkey, new String[]{}).length);
    }
    
    @Test
    public void testArgumentsArrayIllegalArgument() {
        try {
            StringPropertiesUtils.putArray(map, key, new String[] { "'a' b'"}," ");
            fail();
        }
        catch(IllegalArgumentException e) {
            /*expected result*/
        }
    }
    
    @Test
    public void testArgumentsArrayIllegalArgument2() {
        try {
            StringPropertiesUtils.putArray(map, key, new String[] { "\"a\" b\""}," ");
            fail();
        }
        catch(IllegalArgumentException e) {
            /*expected result*/
        }
    }
    
    public void testKeyValuePairs() {
        Map<String, String> inner = new HashMap<String,String>();
        inner.put("a","1");
        inner.put("b","2");
        inner.put("c","x=3");
        StringPropertiesUtils.putKeyValuePairs(map, key, inner, ",", "=");
        assertEquals(inner, StringPropertiesUtils.getKeyValuePairs(map, key, ",", "=", new HashMap<String,String>()));
    }
}
