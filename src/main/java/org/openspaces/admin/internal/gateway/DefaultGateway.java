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
package org.openspaces.admin.internal.gateway;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.GatewayDelegator;
import org.openspaces.admin.gateway.GatewayProcessingUnit;
import org.openspaces.admin.gateway.GatewaySink;
import org.openspaces.admin.gateway.GatewaySinkSource;
import org.openspaces.admin.internal.admin.DefaultAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.core.gateway.GatewayUtils;

import com.gigaspaces.internal.utils.concurrent.ExchangeCountDownLatch;

/**
 * 
 * @author eitany
 * @since 8.0.4
 */
public class DefaultGateway implements Gateway {

    private final String gatewayName;
    private final DefaultAdmin admin;

    public DefaultGateway(DefaultAdmin admin, String gatewayName) {
        this.admin = admin;
        this.gatewayName = gatewayName;
    }

    @Override
    public Iterator<GatewayProcessingUnit> iterator() {
        return Arrays.asList(getGatewayProcessingUnits()).iterator();
    }

    @Override
    public GatewayProcessingUnit[] getGatewayProcessingUnits() {
        List<GatewayProcessingUnit> result = new LinkedList<GatewayProcessingUnit>();
        for (ProcessingUnit processingUnit : admin.getProcessingUnits()) {
            ProcessingUnitInstance puInstance = GatewayUtils.extractInstanceIfPuOfGateway(gatewayName, processingUnit);
            if (puInstance != null){
            	GatewayProcessingUnit gatewayProcessingUnit = 
            		admin.getGatewayProcessingUnits().getGatewayProcessingUnit( puInstance.getUid() );
            	if( gatewayProcessingUnit == null ){
            		throw new IllegalStateException( "GatewayProcessingUnit cannot be null" );
            	}

           		result.add( gatewayProcessingUnit ); 
            }
        } 
        return result.toArray(new GatewayProcessingUnit[result.size()]);
    }

    @Override
    public String getName() {
        return gatewayName;
    }

    @Override
    public boolean waitFor(int numberOfGatewayProcessingUnits) {
        return waitFor(numberOfGatewayProcessingUnits, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public boolean waitFor(int numberOfGatewayProcessingUnits, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfGatewayProcessingUnits);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            
            private final Set<String> gatewayProcessingUnitNames = new HashSet<String>();
            
            @Override
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnitInstance)){
                    if (gatewayProcessingUnitNames.add(processingUnitInstance.getProcessingUnit().getName())){
                        latch.countDown();
                    }
                }
            }
        };
        admin.getProcessingUnits().getProcessingUnitInstanceAdded().add(added);
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(added);
        }
    }

    @Override
    public GatewayProcessingUnit waitForGatewayProcessingUnit(String processingUnitName) {
        return waitForGatewayProcessingUnit(processingUnitName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public GatewayProcessingUnit waitForGatewayProcessingUnit(String processingUnitName, long timeout, TimeUnit timeUnit) {
        //TODO WAN: calculate new timeout
        ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(processingUnitName, timeout, timeUnit);
        if (processingUnit.waitFor(1, timeout, timeUnit)){
            if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnit.getInstances()[0])){
            	GatewayProcessingUnit gatewayProcessingUnit = 
            			admin.getGatewayProcessingUnits().getGatewayProcessingUnit( 
        				processingUnit.getInstances()[0].getUid() );
            	
            	if( gatewayProcessingUnit == null ){
            		throw new IllegalStateException( "GatewayProcessingUnit cannot be null" );
            	}
            	
            	return gatewayProcessingUnit;
            }
            throw new IllegalArgumentException("requested processing unit is not part of this gateway [" + processingUnitName + "]");
        }
        return null;
    }

    @Override
    public GatewayProcessingUnit getGatewayProcessingUnit(String processingUnitName) {
        ProcessingUnit processingUnit = admin.getProcessingUnits().getProcessingUnit(processingUnitName);
        ProcessingUnitInstance[] instances = processingUnit.getInstances();
        if (instances == null || instances.length == 0)
            return null;
        
        if (GatewayUtils.isPuInstanceOfGateway(gatewayName, instances[0])){
        	GatewayProcessingUnit  gatewayProcessingUnit = 
        			admin.getGatewayProcessingUnits().getGatewayProcessingUnit( instances[0].getUid() );
        	if( gatewayProcessingUnit == null ){
        		throw new IllegalStateException( "GatewayProcessingUnit cannot be null" );
        	}
        	
        	return gatewayProcessingUnit;
        }
        throw new IllegalArgumentException("requested processing unit is not part of this gateway [" + processingUnitName + "]");
    }

    @Override
    public Map<String, GatewayProcessingUnit> getNames() {
        Map<String, GatewayProcessingUnit> names = new HashMap<String, GatewayProcessingUnit>();
        for (GatewayProcessingUnit gatewayProcessingUnit : this) {
            names.put(gatewayProcessingUnit.getProcessingUnit().getName(), gatewayProcessingUnit);
        }
        return names;
    }

    @Override
    public GatewaySink getSink(String sourceGatewayName) {
        for (GatewayProcessingUnit gatewayProcessingUnit: this) {
            GatewaySink sink = gatewayProcessingUnit.getSink();
            if (sink != null && sink.containsSource(sourceGatewayName)){
                return sink; 
            }
        }
        return null;
    }

    @Override
    public GatewaySink waitForSink(String sourceGatewayName) {
        return waitForSink(sourceGatewayName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public GatewaySink waitForSink(final String sourceGatewayName, long timeout, TimeUnit timeUnit) {
        final ExchangeCountDownLatch<GatewaySink> latch = new ExchangeCountDownLatch<GatewaySink>();
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            
            @Override
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnitInstance)){
                    GatewayProcessingUnit tempPUI =
                    		admin.getGatewayProcessingUnits().getGatewayProcessingUnit( processingUnitInstance.getUid() );
                	if( tempPUI == null ){
                		throw new IllegalStateException( "GatewayProcessingUnit cannot be null" );
                	}                                        
                    GatewaySink sink = tempPUI.getSink();
                    if (sink != null && sink.containsSource(sourceGatewayName)){
                        latch.countDown(sink);
                    }
                }
            }
        };
        
        admin.getProcessingUnits().getProcessingUnitInstanceAdded().add(added);
        try {
            if (latch.await(timeout, timeUnit))
                return latch.get();
            return null;
        } catch (InterruptedException e) {
            return null;
        } finally {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(added);
        }
    }

    @Override
    public GatewaySinkSource getSinkSource(String sourceGatewayName) {
        GatewaySink sink = getSink(sourceGatewayName);
        if (sink != null)
            return sink.getSourceByName(sourceGatewayName);
        
        return null;
    }
    
    @Override
    public GatewaySinkSource waitForSinkSource(String sourceGatewayName) {
        return waitForSinkSource(sourceGatewayName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public GatewaySinkSource waitForSinkSource(String sourceGatewayName, long timeout, TimeUnit timeUnit) {
        GatewaySink sink = waitForSink(sourceGatewayName, timeout, timeUnit);
        if (sink != null)
            return sink.getSourceByName(sourceGatewayName);
        
        return null;
    }

    @Override
    public GatewayDelegator getDelegator(String targetGatewayName) {
        for (GatewayProcessingUnit gatewayProcessingUnit: this) {
            GatewayDelegator delegator = gatewayProcessingUnit.getDelegator();
            if (delegator != null && delegator.containsTarget(targetGatewayName)){
                return delegator; 
            }
        }
        return null;
    }

    @Override
    public GatewayDelegator waitForDelegator(String targetGatewayName) {
        return waitForDelegator(targetGatewayName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }
    
    @Override
    public GatewayDelegator waitForDelegator(final String targetGatewayName, long timeout, TimeUnit timeUnit) {
        final ExchangeCountDownLatch<GatewayDelegator> latch = new ExchangeCountDownLatch<GatewayDelegator>();
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            
            @Override
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnitInstance)){
                    GatewayProcessingUnit tempPUI =
                    		admin.getGatewayProcessingUnits().getGatewayProcessingUnit( processingUnitInstance.getUid() );
                	if( tempPUI == null ){
                		throw new IllegalStateException( "GatewayProcessingUnit cannot be null" );
                	}                    
                    GatewayDelegator delegator = tempPUI.getDelegator();
                    if (delegator != null && delegator.containsTarget(targetGatewayName)){
                        latch.countDown(delegator);
                    }
                }
            }
        };
        
        admin.getProcessingUnits().getProcessingUnitInstanceAdded().add(added);
        try {
            if (latch.await(timeout, timeUnit))
                return latch.get();
            return null;
        } catch (InterruptedException e) {
            return null;
        } finally {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(added);
        }
    }

    @Override
    public int getSize() {
        return getGatewayProcessingUnits().length;
    }

    @Override
    public boolean isEmpty() {
        return getSize() == 0;
    }

}
