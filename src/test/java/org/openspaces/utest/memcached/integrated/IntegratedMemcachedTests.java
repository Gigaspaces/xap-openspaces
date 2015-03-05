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
package org.openspaces.utest.memcached.integrated;

import junit.framework.TestCase;
import net.spy.memcached.*;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy (shay.banon)
 */
public class IntegratedMemcachedTests extends TestCase {

    private MemcachedClient memcachedClient;
    private static final int timeout = 10;


    public void testTextIntegratedProcessingUnit() throws Exception {
        testIntegratedProcessingUnit(false);
    }

    public void testBinaryIntegratedProcessingUnit() throws Exception {
        testIntegratedProcessingUnit(true);
    }

    private void testIntegratedProcessingUnit(boolean binary) throws Exception {
        IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();

        BeanLevelProperties levelProperties = new BeanLevelProperties();
        Properties props = new Properties();
        props.setProperty("binary", Boolean.toString(binary));
        levelProperties.setContextProperties(props);
        provider.setBeanLevelProperties(levelProperties);

        provider.addConfigLocation("memcached/META-INF/spring/pu.xml");
        // for now, change to 2,1
        provider.setClusterInfo(new ClusterInfo(null, null, null, 1, 0));

        ProcessingUnitContainer container = provider.createContainer();

        if (binary) {
            memcachedClient = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses("localhost:11211"));
        } else {
            memcachedClient = new MemcachedClient(AddrUtil.getAddresses("localhost:11211"));
        }

        for (int i = 0; i < 100; i++) {
            Future setResult = memcachedClient.set("key" + i, 0, "value" + i);
            getResult(setResult, Boolean.TRUE);
        }

        for (int i = 0; i < 100; i++) {
            String getResult = (String) memcachedClient.get("key" + i);
            assertEquals("value" + i, getResult);
        }

        Future<Boolean> booleanFuture = memcachedClient.replace("xkey", 0, "xvalue");
        getResult(booleanFuture, Boolean.FALSE);
        String getResult = (String) memcachedClient.get("xkey");
        assertNull(getResult);

        booleanFuture = memcachedClient.add("xkey", 0, "xvalue");
        getResult(booleanFuture, Boolean.TRUE);
        getResult = (String) memcachedClient.get("xkey");
        assertEquals("xvalue", getResult);

        booleanFuture = memcachedClient.add("xkey", 0, "xvalue1");
        getResult(booleanFuture, Boolean.FALSE);
        getResult = (String) memcachedClient.get("xkey");
        assertEquals("xvalue", getResult);


        booleanFuture = memcachedClient.replace("xkey", 0, "xvalue1");
        getResult(booleanFuture, Boolean.TRUE);
        getResult = (String) memcachedClient.get("xkey");
        assertEquals("xvalue1", getResult);

        booleanFuture = memcachedClient.append(0, "xkey", "append");
        getResult(booleanFuture, Boolean.TRUE);
        getResult = (String) memcachedClient.get("xkey");
        assertEquals("xvalue1append", getResult);

        booleanFuture = memcachedClient.prepend(0, "xkey", "prepend");
        getResult(booleanFuture, Boolean.TRUE);
        getResult = (String) memcachedClient.get("xkey");
        assertEquals("prependxvalue1append", getResult);

        booleanFuture = memcachedClient.delete("xkey");
        getResult(booleanFuture, Boolean.TRUE);
        getResult = (String) memcachedClient.get("xkey");
        assertNull(getResult);

        booleanFuture = memcachedClient.delete("xkey");
        getResult(booleanFuture, Boolean.FALSE);
        Future setResult = memcachedClient.set("xkey", 0, "xvalue");
        getResult(setResult, Boolean.TRUE);

        CASValue<Object> casValue = memcachedClient.gets("xkey");
        assertEquals("xvalue", casValue.getValue());

        CASResponse casResponse = memcachedClient.cas("xkey", casValue.getCas(), "xvalueC1");
        assertEquals(CASResponse.OK, casResponse);

        setResult = memcachedClient.set("xkey", 0, "xvalueU1");
        getResult(setResult, null);

        casResponse = memcachedClient.cas("xkey", casValue.getCas(), "xvalueC1");
        assertEquals(CASResponse.EXISTS, casResponse);

        booleanFuture = memcachedClient.delete("xkey");
        getResult(booleanFuture, Boolean.TRUE);

        casResponse = memcachedClient.cas("xkey", casValue.getCas(), "xvalueC1");
        assertEquals(CASResponse.NOT_FOUND, casResponse);

        setResult = memcachedClient.set("intKey", 0, "1");
        getResult(setResult, null);
        long value = memcachedClient.incr("intKey", 2);
        assertEquals(3, value);

        value = memcachedClient.decr("intKey", 1);
        assertEquals(2, value);

        // check underflow
        value = memcachedClient.decr("intKey", 10);
        assertEquals(0, value);

        // test invalid tokens in key
        setResult = memcachedClient.set("key^1", 0, "value");
        getResult(setResult, null);

        memcachedClient.shutdown();

        container.close();
    }

    private void getResult(Future result, Boolean expected){
        try {
            if (expected != null)
                assertEquals(expected, result.get(timeout, TimeUnit.SECONDS));
            else
                result.get(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            fail("failed getting result "+e);
        }
    }
}
