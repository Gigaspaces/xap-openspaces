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


    private final static String KEY = "key";
    private final static String MISSING_KEY = "missingkey";
    private Map<String,String> map;
    
    @Before
    @Override
    public void setUp() {
        map = new HashMap<String,String>();
    }
    
    @Test
    public void testBooleanTrue() {
        StringPropertiesUtils.putBoolean(map,KEY,true);
        assertTrue(StringPropertiesUtils.getBoolean(map, KEY, false));
        map.put(KEY, "true");
        assertTrue(StringPropertiesUtils.getBoolean(map, KEY, false));
        map.put(KEY, "TRUE");
        assertTrue(StringPropertiesUtils.getBoolean(map, KEY, false));
        assertTrue(StringPropertiesUtils.getBoolean(map, MISSING_KEY, true));
    }
    
    @Test
    public void testBooleanFalse() {
        StringPropertiesUtils.putBoolean(map,KEY,false);
        assertFalse(StringPropertiesUtils.getBoolean(map, KEY, true));
        map.put(KEY, "false");
        assertFalse(StringPropertiesUtils.getBoolean(map, KEY, true));
        map.put(KEY, "FALSE");
        assertFalse(StringPropertiesUtils.getBoolean(map, KEY, true));
        assertFalse(StringPropertiesUtils.getBoolean(map, MISSING_KEY, false));
    }
    
    @Test
    public void testInteger() {
        StringPropertiesUtils.putInteger(map,KEY,Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE,StringPropertiesUtils.getInteger(map, KEY, 0));
        assertEquals(1,StringPropertiesUtils.getInteger(map, MISSING_KEY, 1));
    }
    
    @Test
    public void testIntegerOverflowError() {
        try {
            StringPropertiesUtils.putLong(map,KEY,Long.MAX_VALUE);
            StringPropertiesUtils.getInteger(map, KEY, 0);
            fail();
        } catch(NumberFormatException e) {
            /*expected result*/
        }
    }
    
    @Test
    public void testIntegerUnderflowError() {
        try {
        StringPropertiesUtils.putLong(map,KEY,Long.MIN_VALUE);
        StringPropertiesUtils.getInteger(map, KEY, 0);
        fail();
        } catch(NumberFormatException e) {
            /*expected result*/
        }
    }
    
    @Test
    public void testIntegerParsingError() {
        try {
            map.put(KEY, "notaninteger");
            assertEquals(1,StringPropertiesUtils.getIntegerIgnoreExceptions(map, KEY, 1));
            StringPropertiesUtils.getInteger(map, KEY, 0);
            fail();
        } catch(NumberFormatException e) {
            /*expected result*/
        }
    }
    
    @Test
    public void testLong() {
        StringPropertiesUtils.putLong(map,KEY,Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE,StringPropertiesUtils.getLong(map, KEY, 0));
        assertEquals(1,StringPropertiesUtils.getInteger(map, MISSING_KEY, 1));
    }
    
    @Test
    public void testLongParsingError() {
        try {
        map.put(KEY, "notaninteger");
        assertEquals(1,StringPropertiesUtils.getLongIgnoreExceptions(map, KEY, 1));
        StringPropertiesUtils.getLong(map, KEY, 0);
        fail();
        } catch(NumberFormatException e) {
            /*expected result*/
        }
    }
    
    @Test
    public void testMap() {
        Map<String,String> inner = new HashMap<String,String>();
        inner.put(KEY, "value");
        StringPropertiesUtils.putMap(map, "prefix.", inner);
        assertEquals(inner,StringPropertiesUtils.getMap(map, "prefix.", inner));
    }
    
    @Test
    public void testArray() {
        String[] inner = new String[] { "a","b","c"};
        StringPropertiesUtils.putArray(map, KEY, inner," ");
        assertEquals(Arrays.asList(inner),Arrays.asList(StringPropertiesUtils.getArray(map, KEY, " ", new String[]{})));
        assertEquals(0,StringPropertiesUtils.getArray(map, MISSING_KEY, " ", new String[]{}).length);
    }

    @Test
    public void testArrayIllegalArgument() {
        try {
            StringPropertiesUtils.putArray(map, KEY, new String[] { "a b","c"}," ");
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
        StringPropertiesUtils.putArgumentsArray(map, KEY, inner);
        assertEquals(Arrays.asList(expected),
                     Arrays.asList(StringPropertiesUtils.getArgumentsArray(map, KEY, new String[]{})));
        assertEquals(0,StringPropertiesUtils.getArgumentsArray(map, MISSING_KEY, new String[]{}).length);
    }
    
    @Test
    public void testArgumentsArrayIllegalArgument() {
        try {
            StringPropertiesUtils.putArray(map, KEY, new String[] { "'a' b'"}," ");
            fail();
        }
        catch(IllegalArgumentException e) {
            /*expected result*/
        }
    }
    
    @Test
    public void testArgumentsArrayIllegalArgument2() {
        try {
            StringPropertiesUtils.putArray(map, KEY, new String[] { "\"a\" b\""}," ");
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
        StringPropertiesUtils.putKeyValuePairs(map, KEY, inner, ",", "=");
        assertEquals(inner, StringPropertiesUtils.getKeyValuePairs(map, KEY, ",", "=", new HashMap<String,String>()));
    }
    
    @Test
    public void testConfig() {
        Map<String,String> objectProperties = new HashMap<String, String>();
        objectProperties.put("foo", "bar");
        ConfigMock config = new ConfigMock(objectProperties);
        
        StringPropertiesUtils.putConfig(map, KEY, config);
        ConfigMock recoveredConfig = (ConfigMock) StringPropertiesUtils.getConfig(map, KEY, null);
        assertNotNull(recoveredConfig);
        assertEquals(config.getProperties(),recoveredConfig.getProperties());
        assertEquals(config,recoveredConfig);
    }

    @Test
    public void testObject() {
        Object stringWrapperObject = new Integer("1");
        StringPropertiesUtils.putStringWrapperObject(map,KEY,stringWrapperObject);
        Object recoveredStringWrapperObject = StringPropertiesUtils.getStringWrapperObject(map,KEY,null);
        assertNotNull(recoveredStringWrapperObject);
        assertEquals(stringWrapperObject,recoveredStringWrapperObject);
    }
}
