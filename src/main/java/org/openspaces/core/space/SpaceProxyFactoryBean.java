/*
 * Copyright 2014 the original author or authors.
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

package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;

import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.properties.BeanLevelMergedPropertiesAware;
import org.springframework.dao.DataAccessException;

import java.util.Properties;

/**
 * @author yuvalm
 * @since 10.0
 */
public class SpaceProxyFactoryBean extends AbstractSpaceFactoryBean implements BeanLevelMergedPropertiesAware {

    private final UrlSpaceFactoryBean factoryBean;

    public SpaceProxyFactoryBean() {
        this.factoryBean = new UrlSpaceFactoryBean(true);
    }

    public SpaceProxyFactoryBean(String name) {
        this();
        setName(name);
    }

    @Override
    protected IJSpace doCreateSpace() throws DataAccessException {
        return factoryBean.doCreateSpace();
    }

    public void setName(String name) {
        factoryBean.setName(name);
    }

    public void setInstanceId(String instanceId) {
        factoryBean.setInstanceId(instanceId);
    }

    @Override
    public void setSecurityConfig(SecurityConfig securityConfig) {
        super.setSecurityConfig(securityConfig);
        factoryBean.setSecurityConfig(securityConfig);
    }

    public void setProperties(Properties properties) {
        factoryBean.setProperties(properties);
    }

    public void setLookupGroups(String lookupGroups) {
        factoryBean.setLookupGroups(lookupGroups);
    }

    public void setLookupLocators(String lookupLocators) {
        factoryBean.setLookupLocators(lookupLocators);
    }

    public void setLookupTimeout(int lookupTimeout) {
        factoryBean.setLookupTimeout(lookupTimeout);
    }

    public void setVersioned(boolean versioned) {
        factoryBean.setVersioned(versioned);
    }
    
    @Override
	public void setMergedBeanLevelProperties(Properties beanLevelProperties) {
		factoryBean.setMergedBeanLevelProperties(beanLevelProperties);
	}
}
