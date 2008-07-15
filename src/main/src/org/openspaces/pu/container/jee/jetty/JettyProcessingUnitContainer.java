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

package org.openspaces.pu.container.jee.jetty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.webapp.WebAppContext;
import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ContextLoader;

/**
 * @author kimchy
 */
public class JettyProcessingUnitContainer implements ApplicationContextProcessingUnitContainer {

    private static final Log logger = LogFactory.getLog(JettyProcessingUnitContainer.class);

    private ApplicationContext applicationContext;

    private ApplicationContext webApplicationContext;

    private WebAppContext webAppContext;

    private HandlerContainer container;

    private JettyHolder jettyHolder;

    /**
     */
    public JettyProcessingUnitContainer(ApplicationContext applicationContext, WebAppContext webAppContext,
                                        HandlerContainer container, JettyHolder jettyHolder) {
        this.applicationContext = applicationContext;
        this.webAppContext = webAppContext;
        this.container = container;
        this.jettyHolder = jettyHolder;
        this.webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
        if (webApplicationContext == null) {
            webApplicationContext = applicationContext;
        }
    }

    /**
     * Returns the spring application context this processing unit container wraps.
     */
    public ApplicationContext getApplicationContext() {
        return webApplicationContext;
    }

    /**
     * Closes the processing unit container by destroying the Spring application context.
     */
    public void close() throws CannotCloseContainerException {

        if (webAppContext.isRunning()) {
            try {
                webAppContext.stop();
                webAppContext.destroy();
            } catch (Exception e) {
                logger.warn("Failed to stop/destroy web context", e);
            }

            if (container != null) {
                container.removeHandler(webAppContext);
            }
        }

        // close the application context anyhow (it might be closed by the webapp context, but it
        // might not if it is not a pure Spring application).
        ConfigurableApplicationContext confAppContext = (ConfigurableApplicationContext) applicationContext;
        confAppContext.close();

        try {
            jettyHolder.stop();
        } catch (Exception e) {
            logger.warn("Failed to stop jetty server", e);
        }
    }

}