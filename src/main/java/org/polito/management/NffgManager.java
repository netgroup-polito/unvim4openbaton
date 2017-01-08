package org.polito.management;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.polito.model.nffg.BigSwitch;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.Vnf;

public class NffgManager {
	private static SecureRandom random = new SecureRandom();

	// 26 characters
	public static String getNewId() {
		return new BigInteger(130, random).toString(32);
	}

	public static Nffg createEmptyNffg(String id) {
		Nffg nffg = new Nffg();
		nffg.setId(id);
		nffg.setBigSwitch(new BigSwitch());
		return nffg;
	}

	public static void createVnf(Nffg nffg, String vnfId, String vnfName) {
		Vnf vnf = new Vnf();
		vnf.setId(vnfId);
		vnf.setName(vnfName);
		nffg.addVnf(vnf);
	}

	
}
