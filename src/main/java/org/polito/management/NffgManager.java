package org.polito.management;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import org.polito.model.nffg.Action;
import org.polito.model.nffg.BigSwitch;
import org.polito.model.nffg.FlowRule;
import org.polito.model.nffg.IdAware;
import org.polito.model.nffg.Match;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.Port;
import org.polito.model.nffg.Vnf;

public class NffgManager {
	private static SecureRandom random = new SecureRandom();

	// 8 characters
	public static String getNewId(Object objectList) {
		List<IdAware> list = (List<IdAware>)objectList;
		String newId = null;
		boolean univocal = false;
		while(!univocal)
		{
			univocal = true;
			newId = new BigInteger(40, random).toString(32);
			for(IdAware obj: list)
				if(obj.getId().equals(newId))
				{
					univocal=false;
					break;
				}
		}
		return newId;
	}

	public static Nffg createEmptyNffg(String id) {
		Nffg nffg = new Nffg();
		nffg.setId(id);
		nffg.setBigSwitch(new BigSwitch());
		return nffg;
	}

	public static void createVnf(Nffg nffg, String id, String name, String description, String functionalCapability) {
		Vnf vnf = new Vnf();
		vnf.setId(id);
		vnf.setName(name);
		vnf.setDescription(description);
		vnf.setFunctionalCapability(functionalCapability);
		nffg.addVnf(vnf);
	}

	public static void connectVnfs(Nffg nffg, String firstVnfId, String secondVnfId) {
		String portFirstVnfId = createPort(nffg, firstVnfId);
		String portSeconfVnfId = createPort(nffg, secondVnfId);
		Match match = new Match();
		match.setInput("vnf:" + firstVnfId + ":" + portFirstVnfId);
		Action action = new Action();
		action.setOutput("vnf:" + secondVnfId + ":" + portSeconfVnfId);
		FlowRule flowRule = new FlowRule();
		flowRule.setMatch(match);
		flowRule.addAction(action);
		flowRule.setId(getNewId(nffg.getFlowRules()));
		nffg.addFlowRule(flowRule);
		match = new Match();
		match.setInput("vnf:" + secondVnfId + ":" + portSeconfVnfId);
		action = new Action();
		action.setOutput("vnf:" + firstVnfId + ":" + portFirstVnfId);
		flowRule = new FlowRule();
		flowRule.setMatch(match);
		flowRule.addAction(action);
		flowRule.setId(getNewId(nffg.getFlowRules()));
		nffg.addFlowRule(flowRule);
	}

	private static String createPort(Nffg nffg, String vnfId) {
		int portNumb=0;
		Vnf vnfToUpdate=null;
		for(Vnf vnf: nffg.getVnfs())
			if(vnf.getId().equals(vnfId))
			{
				vnfToUpdate=vnf;
				break;
			}
		List<Port> ports = vnfToUpdate.getPorts();
		portNumb+=ports.size();
		String newPortId = "port:"+portNumb;
		String newPortName = "eth"+portNumb;
		Port newPort = new Port();
		newPort.setId(newPortId);
		newPort.setName(newPortName);
		ports.add(newPort);
		return newPortId;

	}

	
}
