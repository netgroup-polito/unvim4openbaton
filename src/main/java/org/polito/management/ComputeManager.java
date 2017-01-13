package org.polito.management;

import java.util.Date;
import java.util.Set;

import org.openbaton.catalogue.nfvo.Server;
import org.polito.model.nffg.Nffg;

public class ComputeManager {

	public static Server createServer(Nffg nffg, String hostname, String templateImageId, String extId, String keyPair,
			Set<String> networks, Set<String> securityGroups, String s){
		String vnfId = NffgManager.getNewId(nffg.getVnfs());
		NffgManager.createVnf(nffg,vnfId,hostname,"Server",null,templateImageId);
		for(String networkId: networks)
			NffgManager.connectVnfs(nffg, networkId, vnfId);
		Server createdServer = new Server();
		createdServer.setCreated(new Date());
		createdServer.setExtendedStatus("running");
		createdServer.setExtId(vnfId);
		createdServer.setHostName(hostname);
		return createdServer;
	}

}
