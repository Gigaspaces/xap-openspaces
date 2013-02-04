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
package org.openspaces.core.gateway;

import java.io.IOException;
import java.net.ServerSocket;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.pu.service.ServiceDetails;

public class GatewayUtils {
    /***
     * Checks is a post is available on the current machine, using new
     * ServerSocket(port).
     * 
     * @param port
     *            the port number.
     * @return true if available, false otherwise.
     */
    public static boolean checkPortAvailable(final int port) {
        if (port == 0)
            return true;
        ServerSocket sock = null;

        try {
            sock = new ServerSocket(port);
            sock.setReuseAddress(true);
            return true;
        } catch (final IOException e) {
            return false;
        } finally {
            if (sock != null) {
                try {
                    sock.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
        }
    }      
    
    public static boolean isPuInstanceOfGateway(final String gatewayName, ProcessingUnitInstance processingUnitInstance) {
        ServiceDetails[] serviceDetails = processingUnitInstance.getServicesDetailsByServiceType(GatewayServiceDetails.SERVICE_TYPE);
        if (serviceDetails != null && serviceDetails.length > 0){
            if (((GatewayServiceDetails)serviceDetails[0]).getLocalGatewayName().equals(gatewayName)){
                return true;
            }
        }
        
        return false;
    }
    
    public static ProcessingUnitInstance extractInstanceIfPuOfGateway(String gatewayName, ProcessingUnit processingUnit) {
        ProcessingUnitInstance[] instances = processingUnit.getInstances();
        if (instances == null || instances.length == 0)
            return null;
        ServiceDetails[] serviceDetails = instances[0].getServicesDetailsByServiceType(GatewayServiceDetails.SERVICE_TYPE);
        if (serviceDetails != null && serviceDetails.length > 0){
            if (((GatewayServiceDetails)serviceDetails[0]).getLocalGatewayName().equals(gatewayName)){
                return instances[0];
            }
        }
        return null;
    }

    public static String extractGatewayNameIfExists(ProcessingUnit processingUnit) {
        ProcessingUnitInstance[] instances = processingUnit.getInstances();
        if (instances == null || instances.length == 0)
            return null;
        
        return extractGatewayName( instances[0] );
    }

	public static String extractGatewayName(ProcessingUnitInstance instance) {
		ServiceDetails[] serviceDetails = 
				instance.getServicesDetailsByServiceType( GatewayServiceDetails.SERVICE_TYPE );
        if( serviceDetails == null || serviceDetails.length == 0 ){
            return null;
        }
        
        return ((GatewayServiceDetails)serviceDetails[0]).getLocalGatewayName();
	}
	
	public static GatewayServiceDetails extractGatewayDetails(ProcessingUnitInstance instance) {
        ServiceDetails[] serviceDetails = 
                instance.getServicesDetailsByServiceType( GatewayServiceDetails.SERVICE_TYPE );
        if( serviceDetails == null || serviceDetails.length == 0 ){
            return null;
        }
        
        return (GatewayServiceDetails)serviceDetails[0];
    }
}
