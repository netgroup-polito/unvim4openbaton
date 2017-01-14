package org.polito.management;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import org.polito.model.nffg.AbstractEP.Type;
import org.polito.model.nffg.Action;
import org.polito.model.nffg.BigSwitch;
import org.polito.model.nffg.EndpointWrapper;
import org.polito.model.nffg.FlowRule;
import org.polito.model.nffg.HoststackEndPoint;
import org.polito.model.nffg.IdAware;
import org.polito.model.nffg.InterfaceEndPoint;
import org.polito.model.nffg.Match;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.Port;
import org.polito.model.nffg.Vnf;

public class NffgManager {
	private static SecureRandom random = new SecureRandom();

	// 8 characters
	public static String getNewId(Object objectList) {
		@SuppressWarnings("unchecked")
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

	public static Nffg createBootNffg(String id) {
		Nffg nffg = new Nffg();
		nffg.setId(id);
		nffg.setBigSwitch(new BigSwitch());
		String managementSwId = getNewId(nffg.getVnfs());
		String managementDhcpId = getNewId(nffg.getVnfs());
		String managementHoststackId = getNewId(nffg.getEndpoints());
		createVnf(nffg, managementSwId, null, "managementSwitch", null, "managementSwitch");
		createVnf(nffg, managementDhcpId, null, "managementDhcp", null, "managementDhcp");
		createEndpoint(nffg,managementHoststackId,"managementHoststack",Type.HOSTSTACK, "STATIC", "192.168.1.1");
		connectVnfs(nffg,managementSwId,managementDhcpId);
		connectEndpointToVnf(nffg,managementHoststackId,managementSwId);
		return nffg;
	}

	public static void createVnf(Nffg nffg, String id, String name, String description, String functionalCapability, String template) {
		Vnf vnf = new Vnf();
		vnf.setId(id);
		vnf.setName(name);
		vnf.setDescription(description);
		if(functionalCapability!=null)
			vnf.setFunctionalCapability(functionalCapability);
		if(template!=null)
			vnf.setTemplate(template);
		nffg.addVnf(vnf);
	}

	public static void createEndpoint(Nffg nffg, String id, String name, Type type, String... endpointArgs) {
		EndpointWrapper wrappedEP = new EndpointWrapper();
		wrappedEP.setId(id);
		wrappedEP.setName(name);
		switch (type) {
		case INTERFACE:
			InterfaceEndPoint iep = new InterfaceEndPoint();
			iep.setIfName(endpointArgs[0]);
			wrappedEP.setEndpoint(iep);
			break;
		case HOSTSTACK:
			HoststackEndPoint hep = new HoststackEndPoint();
			hep.setConfiguration(endpointArgs[0]);
			if(hep.getConfiguration().equals("STATIC"))
				hep.setIp(endpointArgs[1]);
			wrappedEP.setEndpoint(hep);
			break;
		default:
			break;
		}
		nffg.addEndpoint(wrappedEP);
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

	private static void connectEndpointToVnf(Nffg nffg, String endpointId, String vnfId) {
		String portVnfId = createPort(nffg, vnfId);
		Match match = new Match();
		match.setInput("vnf:" + vnfId + ":" + portVnfId);
		Action action = new Action();
		action.setOutput("endpoint:" + endpointId);
		FlowRule flowRule = new FlowRule();
		flowRule.setMatch(match);
		flowRule.addAction(action);
		flowRule.setId(getNewId(nffg.getFlowRules()));
		nffg.addFlowRule(flowRule);
		match = new Match();
		match.setInput("endpoint:" + endpointId);
		action = new Action();
		action.setOutput("vnf:" + vnfId + ":" + portVnfId);
		flowRule = new FlowRule();
		flowRule.setMatch(match);
		flowRule.addAction(action);
		flowRule.setId(getNewId(nffg.getFlowRules()));
		nffg.addFlowRule(flowRule);
	}
	
}
