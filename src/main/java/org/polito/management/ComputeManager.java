package org.polito.management;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openbaton.catalogue.nfvo.Server;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.Vnf;

public class ComputeManager {
	private static String SERVER_INSTANCE = "Server";

	public static Server createServer(Nffg nffg, String hostname, String templateImageId, String keyPair,
			Set<String> networks, Set<String> securityGroups, String userData){
		String vnfId = NffgManager.getNewId(nffg.getVnfs());
		NffgManager.createVnf(nffg,vnfId,hostname,SERVER_INSTANCE,null,templateImageId);
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

	public static List<Server> getServers(Nffg nffg) {
		List<Server> servers = new ArrayList<>();
		List<Vnf> vnfs = NffgManager.getVnfsByDescription(nffg, SERVER_INSTANCE);
		for(Vnf vnf: vnfs)
		{
			Server server = new Server();
			server.setExtendedStatus("running");
			server.setExtId(vnf.getId());
			server.setHostName(vnf.getName());
			servers.add(server);
		}
		return servers;
	}

	public static void destroyServer(Nffg nffg, String id) {
		NffgManager.destroyVnf(nffg,id);
	}

}
