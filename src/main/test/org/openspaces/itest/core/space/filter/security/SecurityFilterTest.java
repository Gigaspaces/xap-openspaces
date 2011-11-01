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

package org.openspaces.itest.core.space.filter.security;

import java.rmi.RemoteException;
import java.util.Properties;

import com.gigaspaces.security.SecurityException;
import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.SecurityContext;
import com.j_spaces.core.filters.FilterOperationCodes;

import org.junit.Assert;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SecurityConfig;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.itest.core.space.filter.AllOperationsFilterUtil;
import org.openspaces.itest.core.space.filter.SimpleFilter;
import org.openspaces.itest.core.space.filter.adapter.Message;
import org.openspaces.security.spring.SpringSecurityManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class SecurityFilterTest extends AbstractDependencyInjectionSpringContextTests {

    protected SecurityFilter securityFilterCodeName;
    protected SecurityFilter securityFilterCode;
    protected ProviderManager authenticationManager;
    protected GigaSpace gigaSpace;
    protected SecurityFilter[] filters = new SecurityFilter[2];
    
    public SecurityFilterTest() {
        setPopulateProtectedVariables(true);
    }

    @Override
    protected String[] getConfigLocations() {
        Properties props = new Properties();
        props.setProperty(SpringSecurityManager.SPRING_SECURITY_CONFIG_LOCATION, "classpath:/org/openspaces/itest/core/space/filter/security/in-memory-security-config.xml");
        new SpringSecurityManager().init(props);
        try {
            super.onSetUp();
        } catch (Exception e) {
           Assert.assertFalse(true);
        }
        return new String[]{"/org/openspaces/itest/core/space/filter/security/securityFilter.xml"};                      
    }
  
    public void beforeTest(){
        filters[0] = securityFilterCodeName;
        filters[1] = securityFilterCode;
        
//        gigaSpace.takeMultiple(new Message());
        AllOperationsFilterUtil.restartStats(filters);
        AllOperationsFilterUtil.initialAssert(securityFilterCodeName.getStats() , "simpleFilterCodeName"); 
        AllOperationsFilterUtil.initialAssert(securityFilterCode.getStats() , "simpleFilterCode"); 
    }
    
//    public void testWrite() throws SecurityException, RemoteException {
//        beforeTest();
//        Message message = new Message(1);
//        LeaseContext<Message> lease = gigaSpace.write(message);
//        assertNotNull(lease);
//        
//        AllOperationsFilterUtil.assertAfterWriteAuthentication(securityFilterCode.getStats(), "securityFilterCode");
////        AllOperationsFilterUtil.assertAfterAuthentication(securityFilterCodeName.getStats(), "securityFilterCodeName");
//        
//    }
//    
//    public void testRead() {    
//        beforeTest();
//        Message message = new Message(1);        
//        LeaseContext<Message> lease = gigaSpace.write(message);
//        assertNotNull(lease);
//        AllOperationsFilterUtil.restartStats(filters);
//        message = gigaSpace.read(message);
//        assertNotNull(message);
//        
//        AllOperationsFilterUtil.assertAfterReadAuthentication(securityFilterCode.getStats(), "securityFilterCode");
////        AllOperationsFilterUtil.assertAfterAuthentication(securityFilterCodeName.getStats(), "securityFilterCodeName");
//        
//    }
//    
//    public void testTake() {    
//        beforeTest();
//        Message message = new Message(1);        
//        LeaseContext<Message> lease = gigaSpace.write(message);
//        assertNotNull(lease);
//        AllOperationsFilterUtil.restartStats(filters);
//        message = gigaSpace.take(message);
//        assertNotNull(message);
//        
//        AllOperationsFilterUtil.assertAfterTakeAuthentication(securityFilterCode.getStats(), "securityFilterCode");
////        AllOperationsFilterUtil.assertAfterAuthentication(securityFilterCodeName.getStats(), "securityFilterCodeName");
//        
//    }
}
