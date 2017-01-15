package org.polito.management;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
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
			if(hep.getConfiguration().equals("static"))
				hep.setIp(endpointArgs[1]);
			wrappedEP.setEndpoint(hep);
			break;
		default:
			break;
		}
		nffg.addEndpoint(wrappedEP);
	}

	public static void connectVnfToVnf(Nffg nffg, String firstVnfId, String secondVnfId, boolean trustedConnection) {
		String portFirstVnfId = createPort(nffg, firstVnfId, trustedConnection);
		String portSeconfVnfId = createPort(nffg, secondVnfId, false);
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

	private static String createPort(Nffg nffg, String vnfId, boolean trusted) {
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
		if(trusted)
		{
			newPort.setTrusted(true);
			newPort.setMacAddress(generateMacAddress());
		}
		ports.add(newPort);
		return newPortId;

	}

	private static String generateMacAddress() {
	    byte[] macAddr = new byte[6];
	    random.nextBytes(macAddr);
	    macAddr[0] = (byte)(macAddr[0] & (byte)254);  //zeroing last 2 bytes to make it unicast and locally adminstrated
	    StringBuilder sb = new StringBuilder(18);
	    for(byte b : macAddr){
	        if(sb.length() > 0)
	            sb.append(":");
	        sb.append(String.format("%02x", b));
	    }
	    return sb.toString();
	}

	public static void connectEndpointToVnf(Nffg nffg, String endpointId, String vnfId) {
		String portVnfId = createPort(nffg, vnfId, false);
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

	public static List<Vnf> getVnfsByDescription(Nffg nffg, String description) {
		List<Vnf> vnfs = new ArrayList<Vnf>();
		if(nffg!=null)
			for(Vnf vnf: nffg.getVnfs())
				if(vnf.getDescription().equals(description))
					vnfs.add(vnf);
		return vnfs;
	}

	public static Vnf getVnfById(Nffg nffg, String id) {
		for(Vnf vnf: nffg.getVnfs())
			if(vnf.getId().equals(id))
				return vnf;
		return null;
	}

	public static void setUserDataToVnf(Nffg nffg, String vnfId, String userData) {
		for(Vnf vnf: nffg.getVnfs())
			if(vnf.getId().equals(vnfId))
			{
				vnf.setUserData(userData);
				break;
			}
	}

	public static String getMacControlPort(Nffg nffg, String vnfId) {
		for(Vnf vnf: nffg.getVnfs())
			if(vnf.getId().equals(vnfId))
				for(Port port: vnf.getPorts())
					if(port.isTrusted())
						return port.getMacAddress();
		return null;
	}
	
}
