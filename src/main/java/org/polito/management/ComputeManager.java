package org.polito.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
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

	/***
	 * @param managementNffg
	 * @param tenantNffg
	 * @param hostname
	 * @param templateImageId
	 * @param keyPair
	 * @param networks
	 * @param securityGroups
	 * @param userData
	 * @return
	 */
	public static String createServer(Nffg managementNffg, Nffg tenantNffg, String hostname, String templateImageId, String keyPair,
			Set<VNFDConnectionPoint> networks, Set<String> securityGroups, String userData){
		String vnfId = NffgManager.getNewId(tenantNffg.getVnfs());
		NffgManager.createVnf(tenantNffg,vnfId,hostname,SERVER_INSTANCE,null,templateImageId);
		// This is the first connection of the vnf, so the used port will have the name 'eth0'
		// Otherwise the information of the control port could be read from the template
		NetworkManager.connectToManagementNetwork(managementNffg,tenantNffg,vnfId);
		if(userData!=null)
		{
			userData=userData.replace("export MANAGEMENT_PORT=", "export MANAGEMENT_PORT=eth0");
			NffgManager.setUserDataToVnf(tenantNffg,vnfId,userData);
		}
		for(VNFDConnectionPoint vnfdConnectionPoint: networks)
			log.debug("VNFD_CONNECTION_POINT LIST: " + vnfdConnectionPoint.toString());//ELIMINARE
		for(VNFDConnectionPoint vnfdConnectionPoint: networks) {
			/*The UN implements a network with a Switch (VNF), in order to connect a generic VNF A to a network
			I have to connect the VNF A with the Switch VNF of the network*/
			String networkId = NetworkManager.getNetworkIdByName(tenantNffg, vnfdConnectionPoint.getVirtual_link_reference());
			NffgManager.connectVnfToVnf(tenantNffg, vnfId, networkId, true);
		}
		log.debug("Server interfaces: "+NffgManager.getVnfById(tenantNffg, vnfId).getPorts());
		
		return vnfId;
	}

	public static Server getServerById(Nffg operatorNffg, Nffg nffg,  String serverId, String configurationService) throws VimDriverException {
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
		Map<String, String> floatingIps = NetworkManager.getFloatingIps(operatorNffg, nffg, vnfServer, configurationService);
		server.setFloatingIps(floatingIps);
		return server;
	}

	public static List<Server> getServers(Nffg operatorNffg, Nffg nffg, String configurationService) throws VimDriverException {
		List<Server> servers = new ArrayList<>();
		List<Vnf> vnfs = NffgManager.getVnfsByDescription(nffg, SERVER_INSTANCE);
		for(Vnf vnf: vnfs)
			servers.add(getServerById(operatorNffg, nffg, vnf.getId(), configurationService));
		return servers;
	}

	public static void destroyServer(Nffg managementNffg, Nffg operatorNffg, Nffg tenantNffg, String id, String configurationService, boolean forced) throws VimDriverException {
		Vnf vnfServer = NffgManager.getVnfById(tenantNffg, id);
		if(!forced)
		{
			Map<String,List<String>> networkIpAddressAssociation = NetworkManager.getNetworkIpAddressAssociation(tenantNffg,  vnfServer, configurationService);
			NetworkManager.deleteFloatingIps(operatorNffg, tenantNffg, vnfServer, networkIpAddressAssociation, configurationService);
		}
		NetworkManager.disconnectToManagementNetwork(managementNffg, tenantNffg, id);
		NffgManager.destroyVnf(tenantNffg,id);
	}

	public static void assigneFloatingIps(Nffg operatorNffg, Server server, Map<String, String> floatingIps,
			String configurationServiceEndpoint, String externalNetwork, FloatingIpPool floatingIpPool) throws VimDriverException {
		Map<String,String> randomFloatIps = NetworkManager.implementFloatingIps(operatorNffg, server.getIps(),floatingIps,configurationServiceEndpoint, externalNetwork, floatingIpPool);
		for(String network: randomFloatIps.keySet())
			floatingIps.put(network,randomFloatIps.get(network));
		server.setFloatingIps(floatingIps);
	}

}
