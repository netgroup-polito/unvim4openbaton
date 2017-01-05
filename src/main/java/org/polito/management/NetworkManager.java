package org.polito.management;

import org.openbaton.catalogue.nfvo.Network;
import org.polito.model.nffg.Nffg;

public class NetworkManager {

	public static void createNetwork(Nffg nffg, Network network) {
		String vnfId = NffgManager.getNewId();
		NffgManager.createVnf(nffg,"Net"+network.getName()+vnfId,"switch");
		network.setExtId(vnfId);
	}

}
