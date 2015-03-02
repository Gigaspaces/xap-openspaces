package org.openspaces.itest.core.space.proxy;

import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/core/space/proxy/proxy.xml")
public class SpaceProxyTest {
    @Autowired
    protected ApplicationContext applicationContext;

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/proxy/proxy.xml"};
    }

    @Test
    public void testGetSpaceFromContext() throws Exception {
        testGetSpaceByName("space1", "space1");
        testGetGigaSpaceByName("gigaSpace1", "space1");

        testGetSpaceByName("space2", "mySpace2");
        testGetGigaSpaceByName("gigaSpace2", "mySpace2");

        testGetSpaceByName("space3", "mySpace3");
        testGetGigaSpaceByName("gigaSpace3", "mySpace3");
    }

    private void testGetSpaceByName(String name, String expectedName) {
        Object bean = applicationContext.getBean(name);
        ISpaceProxy spaceProxy = (ISpaceProxy) bean;
        Assert.assertEquals(expectedName, spaceProxy.getName());
    }

    private void testGetGigaSpaceByName(String name, String expectedName) {
        Object bean = applicationContext.getBean(name);
        GigaSpace gigaSpace = (GigaSpace) bean;
        Assert.assertEquals(expectedName, gigaSpace.getSpace().getName());
    }
}
