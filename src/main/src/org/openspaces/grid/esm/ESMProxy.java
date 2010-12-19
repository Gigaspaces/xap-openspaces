package org.openspaces.grid.esm;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

import net.jini.id.Uuid;

import org.jini.rio.resources.servicecore.AbstractProxy;

import com.gigaspaces.security.SecurityException;
import com.gigaspaces.security.directory.UserDetails;
import com.gigaspaces.security.service.SecurityContext;

public class ESMProxy extends AbstractProxy implements ESM, Serializable {
    private static final long serialVersionUID = 1L;
    
    private final ESM esmServer;
    
    static ESMProxy getInstance(ESM esm, Uuid id) {
        return (new ESMProxy(esm, id));
    }

    ESMProxy(ESM esm, Uuid id) {
        super(esm, id);
        this.esmServer = esm;
    }

    private transient Integer cachedAgentId = null;
	public int getAgentId() throws RemoteException {
		if (cachedAgentId == null) {
			cachedAgentId = Integer.valueOf(esmServer.getAgentId());
		}
		return cachedAgentId.intValue();
	}

	private transient String cachedGSAServiceID = null;
	public String getGSAServiceID() throws RemoteException {
		if (cachedGSAServiceID == null) {
			cachedGSAServiceID = esmServer.getGSAServiceID();
		}
		return cachedGSAServiceID;
	}

    public String[] getManagedProcessingUnits() {
        return esmServer.getManagedProcessingUnits();
    }

    public boolean isServiceSecured() throws RemoteException {
        return esmServer.isServiceSecured();
    }

    public SecurityContext login(UserDetails userDetails) throws SecurityException, RemoteException {
        return esmServer.login( userDetails );
    }

    public Map<String, String> getProcessingUnitElasticConfig(String processingUnitName) throws RemoteException {
        return esmServer.getProcessingUnitElasticConfig(processingUnitName);
    }

    public void setProcessingUnitElasticConfig(String processingUnitName, Map<String, String> properties)
            throws RemoteException {
        esmServer.setProcessingUnitElasticConfig(processingUnitName, properties);
    }

}