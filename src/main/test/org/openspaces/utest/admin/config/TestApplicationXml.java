/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.utest.admin.config;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.openspaces.admin.application.ApplicationDeployment;
import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.dependency.ProcessingUnitDeploymentDependenciesConfigurer;
import org.openspaces.admin.space.SpaceDeployment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests that reading an application.xml is the same as using fluent admin API. 
 * @author itaif
 * @since 9.0.1
 */

public class TestApplicationXml extends TestCase {

    private final String TEST_APPLICATION_RAW_XML = "/org/openspaces/utest/admin/config/test-application-raw.xml";
    private final String TEST_APPLICATION_XML = "/org/openspaces/utest/admin/config/test-application.xml";
    
    public void testRawXml() {
        Assert.assertEquals(createApplicationWithAdminApi(), createApplicationFromXml(TEST_APPLICATION_RAW_XML));
    }

    public void testOsAdminXml() {
        Assert.assertEquals(createApplicationWithAdminApi(), createApplicationFromXml(TEST_APPLICATION_XML));
    }
    
    private ApplicationConfig createApplicationFromXml(String relativePath) {
        
        final ApplicationContext context = new ClassPathXmlApplicationContext(relativePath);
        final ApplicationConfig applicationConfig = context.getBean(org.openspaces.admin.application.config.ApplicationConfig.class);
        Assert.assertNotNull(applicationConfig);
        return applicationConfig;
    }

    private ApplicationConfig createApplicationWithAdminApi() {
        ApplicationDeployment applicationDeployment = new ApplicationDeployment("test-application")
        
        .addProcessingUnitDeployment(
                new SpaceDeployment("space")
                .addDependencies(
                        new ProcessingUnitDeploymentDependenciesConfigurer()
                        .dependsOnDeployed("a")
                        .dependsOnMinimumNumberOfDeployedInstances("b", 1)
                        .dependsOnMinimumNumberOfDeployedInstancesPerPartition("a", 1)
                        .create())
                .addZone("zone1")
                .addZone("zone2")
                .maxInstancesPerZone("zone", 1)
                .secured(true)
                .setContextProperty("key", "value")
                .slaLocation("slaLocation")
                .userDetails("username", "password")
                .partitioned(1,1)
                .maxInstancesPerVM(1)
                .maxInstancesPerMachine(0))
        
        .addProcessingUnitDeployment(
                new ProcessingUnitDeployment("processor.jar")
                .addDependencies(
                        new ProcessingUnitDeploymentDependenciesConfigurer()
                        .dependsOnDeployed("a")
                        .dependsOnMinimumNumberOfDeployedInstances("b", 1)
                        .dependsOnMinimumNumberOfDeployedInstancesPerPartition("a", 1)
                        .create())
                .addZone("zone1")
                .addZone("zone2")
                .maxInstancesPerZone("zone", 1)
                .secured(true)
                .setContextProperty("key", "value")
                .slaLocation("slaLocation")
                .userDetails("username", "password")
                .partitioned(1,1)
                .maxInstancesPerVM(1)
                .maxInstancesPerMachine(0));
        
        return applicationDeployment.create();
    }
}