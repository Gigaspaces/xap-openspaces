/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.utest.core.properties;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author shaiw
 */
public class BeanLevelPropertiesTest extends AbstractDependencyInjectionSpringContextTests {

    public BeanLevelPropertiesTest() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/utest/core/properties/properties.xml"};
    }

    public void testClusterInfoAnnotations() {
        BeanLevelPropertiesBean bean = (BeanLevelPropertiesBean)getApplicationContext().getBean("beanLevelPropertiesBean");
        assertNotNull(bean);
        assertNotNull(bean.beanLevelMergedProperties);
        assertNotNull(bean.beanLevelProperties);
        assertEquals(2, bean.beanLevelMergedProperties.size());
        assertTrue(bean.beanLevelMergedProperties.containsKey("key1"));
        assertTrue(bean.beanLevelMergedProperties.containsKey("key2"));
        assertEquals("value1", bean.beanLevelMergedProperties.getProperty("key1"));
        assertEquals("value2", bean.beanLevelMergedProperties.getProperty("key2"));
    }
}