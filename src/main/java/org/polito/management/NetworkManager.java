package org.polito.management;

import java.util.ArrayList;
import java.util.List;

import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.Subnet;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.Vnf;

public class NetworkManager {

	public static void createNetwork(Nffg nffg, Network network) {
		String vnfId = NffgManager.getNewId();
		NffgManager.createVnf(nffg,"Net"+network.getName()+vnfId,"switch");
		network.setExtId(vnfId);
	}

	public static void createSubnet(Nffg nffg, Network network, Subnet subnet)
	{
		String vnfId = NffgManager.getNewId();
		NffgManager.createVnf(nffg,"SNet"+subnet.getName()+vnfId,"dhcp");
		subnet.setExtId(vnfId);
		//NffgManager.connectVnfs("Net"+network.getName()+network.getExtId(),"SNet"+subnet.getName()+vnfId);
		//TODO: Interact with the configuration Service in order to configure the dhcp
	}

	public static List<Network> getNetworks(Nffg nffg) {
		List<Network> networks = new ArrayList<>();
		if(nffg!=null)
			for(Vnf vnf: nffg.getVnfs())
			{
				String id = vnf.getId();
				if(id.substring(0,2).equals("Net"))
				{
					Network net = new Network();
					String netName = id.substring(3,id.length()-26);
					String netId = id.substring(id.length()-26);
					net.setExtId(netId);
					net.setName(netName);
					//TODO: Set subnets
				}
			}
		return networks;
	}

}
