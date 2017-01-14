package org.polito.management;

import java.util.ArrayList;
import java.util.List;

import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.Subnet;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.Vnf;
import org.polito.model.nffg.AbstractEP.Type;

public class NetworkManager {
	private static final String NETWORK = "Network";
	private static final String SUBNET = "Subnet";
	private static final String MANAGEMENT_NETWORK = "ManagementNetwork";
	private static final String MANAGEMENT_SUBNET = "ManagementSubnet";


	public static void createNetwork(Nffg nffg, Network network) {
		String vnfId = NffgManager.getNewId(nffg.getVnfs());
		NffgManager.createVnf(nffg,vnfId,network.getName(),NETWORK,"switch",null);
		network.setExtId(vnfId);
	}

	public static void createSubnet(Nffg nffg, Network network, Subnet subnet)
	{
		String vnfId = NffgManager.getNewId(nffg.getVnfs());
		NffgManager.createVnf(nffg,vnfId,subnet.getName(),SUBNET,"dhcp",null);
		subnet.setExtId(vnfId);
		NffgManager.connectVnfs(nffg,network.getExtId(),subnet.getExtId());
		//TODO: Interact with the configuration Service in order to configure the dhcp
	}

	public static List<Network> getNetworks(Nffg nffg) {
		List<Network> networks = new ArrayList<>();
		List<Vnf> vnfList = NffgManager.getVnfsByDescription(nffg,NETWORK);
		for(Vnf vnf: vnfList)
		{
			Network net = new Network();
			net.setExtId(vnf.getId());
			net.setName(vnf.getName());
			networks.add(net);
			//TODO: Set subnets
		}
		return networks;
	}

	public static void createManagementNetwork(Nffg nffg) {
		String managementSwId = NffgManager.getNewId(nffg.getVnfs());
		String managementDhcpId = NffgManager.getNewId(nffg.getVnfs());
		String managementHoststackId = NffgManager.getNewId(nffg.getEndpoints());
		NffgManager.createVnf(nffg, managementSwId, null, MANAGEMENT_NETWORK, null, "managementSwitch");
		NffgManager.createVnf(nffg, managementDhcpId, null, MANAGEMENT_SUBNET, null, "managementDhcp");
		NffgManager.createEndpoint(nffg,managementHoststackId,"managementHoststack",Type.HOSTSTACK, "STATIC", "192.168.1.1");
		NffgManager.connectVnfs(nffg,managementSwId,managementDhcpId);
		NffgManager.connectEndpointToVnf(nffg,managementHoststackId,managementSwId);
	}

}
