package org.openspaces.utest.core.space;

import com.j_spaces.core.IJSpace;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.openspaces.core.space.EmbeddedSpaceFactoryBean;
import org.openspaces.core.space.SpaceProxyFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author yuvalm
 */
public class SpaceFactoryBeanWrappersTest extends TestCase {

    public static final String SPACE_NAME = "myspace";
    public static final String EMBEDDED_SPACE_BEAN_ID = "emb";
    public static final String SPACE_PROXY_BEAN_ID = "prx";
    private ApplicationContext applicationContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        applicationContext = new ClassPathXmlApplicationContext("./org/openspaces/utest/core/space/url-space-wrappers-beans.xml");
    }

    @Test
    public void testSpaceWrappers() {
        IJSpace embeddedSpace = (IJSpace) applicationContext.getBean(EMBEDDED_SPACE_BEAN_ID);

        Assert.assertNotNull(embeddedSpace);
        String realUrl = embeddedSpace.getFinderURL().getURL();
        Assert.assertEquals(realUrl.substring(0, realUrl.indexOf('?')), EmbeddedSpaceFactoryBean.URL_PREFIX +SPACE_NAME);

        IJSpace spaceProxy = (IJSpace) applicationContext.getBean(SPACE_PROXY_BEAN_ID);

        Assert.assertNotNull(spaceProxy);
        realUrl = spaceProxy.getFinderURL().getURL();
        Assert.assertEquals(realUrl.substring(0, realUrl.indexOf('?')), SpaceProxyFactoryBean.URL_PREFIX +SPACE_NAME);
    }

}
