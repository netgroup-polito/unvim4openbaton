package org.polito.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.exceptions.VimDriverException;
import org.polito.model.message.FloatingIpPool;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.Vnf;
import org.polito.unvim.UnClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComputeManager {
	private static String SERVER_INSTANCE = "Server";
	private static Logger log = LoggerFactory.getLogger(ComputeManager.class);

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

	public static Server getServerById(Nffg managementNffg, Nffg nffg,  String serverId, String configurationService) throws VimDriverException {
		log.debug("Obtaining private ip addresses of the server with id: " + serverId);
		Vnf vnfServer = NffgManager.getVnfById(nffg, serverId);
		Server server = new Server();
		server.setStatus("running");
		server.setExtendedStatus("running");
		server.setExtId(serverId);
		server.setHostName(vnfServer.getName());
		Map<String,List<String>> networkIpAddressAssociation = NetworkManager.getNetworkIpAddressAssociation(nffg,  vnfServer, configurationService);
		server.setIps(networkIpAddressAssociation);
		log.debug("Obtaining floating ip addresses of the server with id: " + serverId);
		Map<String, String> floatingIps = NetworkManager.getFloatingIps(managementNffg, nffg, vnfServer, configurationService);
		server.setFloatingIps(floatingIps);
		return server;
	}

	public static List<Server> getServers(Nffg managementNffg, Nffg nffg, String configurationService) throws VimDriverException {
		List<Server> servers = new ArrayList<>();
		List<Vnf> vnfs = NffgManager.getVnfsByDescription(nffg, SERVER_INSTANCE);
		for(Vnf vnf: vnfs)
			servers.add(getServerById(managementNffg, nffg, vnf.getId(), configurationService));
		return servers;
	}

	public static void destroyServer(Nffg managementNffg, Nffg nffg, String id, String configurationService) throws VimDriverException {
		Vnf vnfServer = NffgManager.getVnfById(nffg, id);
		Map<String,List<String>> networkIpAddressAssociation = NetworkManager.getNetworkIpAddressAssociation(nffg,  vnfServer, configurationService);
		NetworkManager.deleteFloatingIps(managementNffg, nffg, vnfServer, networkIpAddressAssociation, configurationService);
		NffgManager.destroyVnf(nffg,id);
	}

	public static void assigneFloatingIps(Nffg managementNffg, Server server, Map<String, String> floatingIps,
			String configurationServiceEndpoint, String externalNetwork, FloatingIpPool floatingIpPool) throws VimDriverException {
		Map<String,String> randomFloatIps = NetworkManager.implementFloatingIps(managementNffg, server.getIps(),floatingIps,configurationServiceEndpoint, externalNetwork, floatingIpPool);
		for(String network: randomFloatIps.keySet())
			floatingIps.replace(network,randomFloatIps.get(network));
		server.setFloatingIps(floatingIps);
	}

}
