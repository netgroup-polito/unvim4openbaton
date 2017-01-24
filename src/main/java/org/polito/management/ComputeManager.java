package org.polito.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.exceptions.VimDriverException;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.Vnf;
import org.polito.unvim.UnClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComputeManager {
	private static String SERVER_INSTANCE = "Server";
	private static Logger log = LoggerFactory.getLogger(UnClient.class);

	public static String createServer(Nffg nffg, String hostname, String templateImageId, String keyPair,
			Set<String> networks, Set<String> securityGroups, String userData){
		String vnfId = NffgManager.getNewId(nffg.getVnfs());
		NffgManager.createVnf(nffg,vnfId,hostname,SERVER_INSTANCE,null,templateImageId);
		if(userData!=null)
			NffgManager.setUserDataToVnf(nffg,vnfId,userData);
		for(String networkId: networks)
			NffgManager.connectVnfToVnf(nffg, vnfId, networkId, true);
		return vnfId;
	}

	public static Server getServerById(Nffg nffg, String serverId, String configurationService) throws VimDriverException {
		log.debug("Obtaining ip addresses of the server with id: " + serverId);
		Vnf vnfServer = NffgManager.getVnfById(nffg, serverId);
		Server server = new Server();
		server.setStatus("running");
		server.setExtendedStatus("running");
		server.setExtId(serverId);
		server.setHostName(vnfServer.getName());
		server.setFloatingIps(new HashMap<String, String>());
		Map<String,List<String>> networkIpAddressAssociation = NetworkManager.getNetworkIpAddressAssociation(nffg,  vnfServer, configurationService);
		server.setIps(networkIpAddressAssociation);
		return server;
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
