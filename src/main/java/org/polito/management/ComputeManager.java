package org.polito.management;

import java.util.Date;
import java.util.Set;

import org.openbaton.catalogue.nfvo.Server;
import org.polito.model.nffg.Nffg;

public class ComputeManager {

	public static Server createServer(Nffg nffg, String hostname, String templateImageId, String keyPair,
			Set<String> networks, Set<String> securityGroups, String userData){
		String vnfId = NffgManager.getNewId(nffg.getVnfs());
		NffgManager.createVnf(nffg,vnfId,hostname,"Server",null,templateImageId);
		if(userData!=null)
			NffgManager.setUserDataToVnf(nffg,vnfId,userData);
		for(String networkId: networks)
			NffgManager.connectVnfToVnf(nffg, networkId, vnfId, false);
		Server createdServer = new Server();
		createdServer.setCreated(new Date());
		createdServer.setExtendedStatus("running");
		createdServer.setExtId(vnfId);
		createdServer.setHostName(hostname);
		return createdServer;
	}

}
