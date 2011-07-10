package org.openspaces.core.gateway;

import java.io.IOException;
import java.net.ServerSocket;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitType;
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
        if (processingUnitInstance.getProcessingUnit().getType() == ProcessingUnitType.GATEWAY){
            ServiceDetails[] serviceDetails = processingUnitInstance.getServicesDetailsByServiceType(GatewayServiceDetails.SERVICE_TYPE);
            if (serviceDetails != null && serviceDetails.length > 0){
                if (((GatewayServiceDetails)serviceDetails[0]).getLocalGatewayName().equals(gatewayName)){
                    return true;
                }
            }
        }
        return false;
    }
    
    public static ProcessingUnitInstance extractInstanceIfPuOfGateway(String gatewayName, ProcessingUnit processingUnit) {
        if (processingUnit.getType() == ProcessingUnitType.GATEWAY){
            ProcessingUnitInstance[] instances = processingUnit.getInstances();
            if (instances == null || instances.length == 0)
                return null;
            ServiceDetails[] serviceDetails = instances[0].getServicesDetailsByServiceType(GatewayServiceDetails.SERVICE_TYPE);
            if (serviceDetails != null && serviceDetails.length > 0){
                if (((GatewayServiceDetails)serviceDetails[0]).getLocalGatewayName().equals(gatewayName)){
                    return instances[0];
                }
            }
        }
        return null;
    }

    public static String extractGatewayNameIfExists(ProcessingUnit processingUnit) {
        if (processingUnit.getType() != ProcessingUnitType.GATEWAY)
            return null;
        
        ProcessingUnitInstance[] instances = processingUnit.getInstances();
        if (instances == null || instances.length == 0)
            return null;
        
        ProcessingUnitInstance instance = instances[0];
        ServiceDetails[] serviceDetails = instance.getServicesDetailsByServiceType(GatewayServiceDetails.SERVICE_TYPE);
        if (serviceDetails == null || serviceDetails.length == 0)
            return null;
        
        return ((GatewayServiceDetails)serviceDetails[0]).getLocalGatewayName();
    }
}
