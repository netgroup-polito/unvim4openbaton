package org.polito.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.exceptions.VimDriverException;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.Port;
import org.polito.model.nffg.Vnf;
import org.polito.model.yang.dhcp.DhcpYang;
import org.polito.proxy.DatastoreProxy;

public class ComputeManager {
	private static String SERVER_INSTANCE = "Server";

	public static String createServer(Nffg nffg, String hostname, String templateImageId, String keyPair,
			Set<String> networks, Set<String> securityGroups, String userData){
		String vnfId = NffgManager.getNewId(nffg.getVnfs());
		NffgManager.createVnf(nffg,vnfId,hostname,SERVER_INSTANCE,null,templateImageId);
		if(userData!=null)
			NffgManager.setUserDataToVnf(nffg,vnfId,userData);
		for(String networkId: networks)
			NffgManager.connectVnfToVnf(nffg, networkId, vnfId, false);
		return vnfId;
	}

	public static Server getServerById(Nffg nffg, String serverId, String configurationService) throws VimDriverException {
		Vnf vnf = NffgManager.getVnfById(nffg, serverId);
		Server server = new Server();
		server.setStatus("running");
		server.setExtendedStatus("running");
		server.setExtId(serverId);
		server.setHostName(vnf.getName());
		server.setFloatingIps(new HashMap<String, String>());
		boolean gotAllAddresses = false;
		Map<String, List<String>> ipsServer = null;
		while(!gotAllAddresses)
		{
			gotAllAddresses=true;
			DhcpYang dhcpYang = DatastoreProxy.getDhcpYang(configurationService, "openbaton", nffg.getId(), NffgManager.getMacControlPort(nffg, serverId));
			Map<String, List<String>> ipsAll = YangManager.readClientAddresses(dhcpYang);
			ipsServer = new HashMap<String, List<String>>();
			for(Port port: vnf.getPorts())
			{
				List<String> list = ipsAll.get(port.getMacAddress());
				if(list==null)
				{
					gotAllAddresses=false;
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						throw new VimDriverException(e.getMessage());
					}
					break;
				}
				ipsServer.put(port.getMacAddress(), list);
			}
		}
		server.setIps(ipsServer);
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
