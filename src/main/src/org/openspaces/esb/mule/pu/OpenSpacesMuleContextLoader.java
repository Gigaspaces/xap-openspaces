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

package org.openspaces.esb.mule.pu;

import org.mule.api.MuleContext;
import org.mule.api.context.MuleContextFactory;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * <code> OpenSpacesMuleContextLoader</code> used for loading Mule configuration that refrenced from PU configuration
 * file.
 *
 * <p>It sets the PU appliction context as the parent of Mule appliction context, giving it the ability to access beans
 * that declerd in the PU appliction context.
 *
 * @author yitzhaki
 */
public class OpenSpacesMuleContextLoader implements ApplicationContextAware, InitializingBean, DisposableBean, ApplicationListener {

    private static final String DEFAULT_LOCATION = "META-INF/spring/mule.xml";

    private String location;

    private ApplicationContext applicationContext;

    private MuleContextFactory muleContextFactory;

    private MuleContext muleContext;

    private volatile boolean contextCreated = false;

    public OpenSpacesMuleContextLoader() {
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    public void afterPropertiesSet() throws Exception {
        if (this.location == null) {
            this.location = DEFAULT_LOCATION;
        }
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            if (!contextCreated) {
                contextCreated = true;
                try {
                    muleContextFactory = new DefaultMuleContextFactory();
                    SpringXmlConfigurationBuilder muleXmlConfigurationBuilder = new SpringXmlConfigurationBuilder(location, this.applicationContext);
                    muleContext = muleContextFactory.createMuleContext(muleXmlConfigurationBuilder);
                    muleContext.start();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to start mule [" + location + "]", e);
                }
            }
        }
    }

    public void destroy() throws Exception {
        if (muleContext != null) {
            muleContext.dispose();
        }
    }
}
