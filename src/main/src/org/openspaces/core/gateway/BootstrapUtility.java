package org.openspaces.core.gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gateway.GatewaySink;
import org.openspaces.admin.gateway.GatewaySinkSource;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * Gateway bootstrap utilities
 * @author eitany
 * @since 8.0.3
 * @deprecated Since 8.0.4 - use {@link GatewaySinkSource} API instead.
 */
@Deprecated
public class BootstrapUtility {
    
    /**
     * Bootstrap a gateway from a remote gateway
     * @param admin admin of the service grid environment that contains the gateway sink processing unit that should bootstrap
     * @param gatewaySinkPuName the name of the processing unit the gateway sink is deployed under
     * @param sourceGatewayName the source gateway name to bootstrap from
     * @param timeout timeout for operation
     * @param timeUnit
     * @throws Exception
     * @deprecated Since 8.0.4 - use {@link GatewaySinkSource#bootstrapFromGatewayAndWait()} instead.
     */
    @Deprecated
    public static void bootstrap(Admin admin, String gatewaySinkPuName, String sourceGatewayName, long timeout, TimeUnit timeUnit) throws Exception{
        ProcessingUnitInstance processingUnitInstance = locateSink(admin, gatewaySinkPuName, timeout, timeUnit);
        
        Map<String, Object> namedArgs = new HashMap<String, Object>();
        namedArgs.put("bootstrapFromGateway", sourceGatewayName);
        namedArgs.put("bootstrapTimeout", timeUnit.toMillis(timeout));
        Future<Object> future = ((InternalProcessingUnitInstance)processingUnitInstance).invoke("sink", namedArgs);
        future.get(timeout, timeUnit);
    }

    /**
     * Enables incoming replication to a gateway sink that require bootstrap but was not bootstrap yet.
     * @param admin admin of the service grid environment that contains the gateway sink processing unit
     * @param gatewaySinkPuName the name of the processing unit the gateway sink is deployed under
     * @throws TimeoutException 
     * @deprecated Since 8.0.4 - use {@link GatewaySink#enableIncomingReplication()} instead.
     */
    @Deprecated
    public static void enableIncomingReplication(Admin admin, String gatewaySinkPuName, long timeout, TimeUnit timeUnit) throws TimeoutException{
        ProcessingUnitInstance processingUnitInstance = locateSink(admin, gatewaySinkPuName, timeout, timeUnit);
        Map<String, Object> namedArgs = new HashMap<String, Object>();
        namedArgs.put("enableIncomingReplication", "");
        ((InternalProcessingUnitInstance)processingUnitInstance).invoke("sink", namedArgs);
    }

    private static ProcessingUnitInstance locateSink(Admin admin, String gatewaySinkPuName, long timeout, TimeUnit timeUnit) throws TimeoutException {
        ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(gatewaySinkPuName, timeout, timeUnit);
        if (processingUnit == null)
            throw new TimeoutException("Could not locate gateway sink processing unit [" + gatewaySinkPuName + "] with the specified timeout [" + timeUnit.toMillis(timeout) + " milliseconds]");
        processingUnit.waitFor(1);
        
        ProcessingUnitInstance processingUnitInstance = processingUnit.getInstances()[0];
        return processingUnitInstance;
    }
    
}
