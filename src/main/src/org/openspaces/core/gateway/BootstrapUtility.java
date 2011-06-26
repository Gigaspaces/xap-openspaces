package org.openspaces.core.gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * Gateway bootstrap utilities
 * @author eitany
 * @since 8.0.3
 */
public class BootstrapUtility {
    
    /**
     * Bootstrap a gateway from a remote gateway
     * @param admin admin of the service grid environment that contains the gateway sink processing unit that should bootstrap
     * @param gatewaySinkPuName the name of the processing unit the gateway sink is deployed under
     * @param sourceGatewayName the source gateway name to bootstrap from
     * @param timeout timeout for operation
     * @param timeUnit
     * @throws Exception
     */
    public static void bootstrap(Admin admin, String gatewaySinkPuName, String sourceGatewayName, long timeout, TimeUnit timeUnit) throws Exception{
        ProcessingUnitInstance processingUnitInstance = locateSink(admin, gatewaySinkPuName);
        
        Map<String, Object> namedArgs = new HashMap<String, Object>();
        namedArgs.put("bootstrapFromGateway", sourceGatewayName);
        namedArgs.put("bootstrapTimeout", timeUnit.toSeconds(timeout));
        Future<Object> future = ((InternalProcessingUnitInstance)processingUnitInstance).invoke("sink", namedArgs);
        future.get(timeout, timeUnit);
    }

    /**
     * Enables incoming replication to a gateway sink that require bootstrap but was not bootstrap yet.
     * @param admin admin of the service grid environment that contains the gateway sink processing unit
     * @param gatewaySinkPuName the name of the processing unit the gateway sink is deployed under
     */
    public static void enableIncomingReplication(Admin admin, String gatewaySinkPuName){
        ProcessingUnitInstance processingUnitInstance = locateSink(admin, gatewaySinkPuName);
        Map<String, Object> namedArgs = new HashMap<String, Object>();
        namedArgs.put("enableIncomingReplication", "");
        ((InternalProcessingUnitInstance)processingUnitInstance).invoke("sink", namedArgs);
    }

    private static ProcessingUnitInstance locateSink(Admin admin, String gatewaySinkPuName) {
        ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(gatewaySinkPuName);
        processingUnit.waitFor(1);
        
        ProcessingUnitInstance processingUnitInstance = processingUnit.getInstances()[0];
        return processingUnitInstance;
    }
    
}
