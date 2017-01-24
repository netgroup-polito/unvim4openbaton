package org.polito.management;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.polito.model.template.VnfTemplate;

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
		Vnf vnfToUpdate=getVnfById(nffg, vnfId);
		VnfTemplate template = vnfToUpdate.getTemplateObject();
		List<Port> ports = vnfToUpdate.getPorts();
		portNumb+=ports.size();
		String newPortName=null;
		if(template==null)
			newPortName = "eth"+portNumb;
		else
		{
			String possiblePortName=null;
			boolean unboundedCase=false;
			boolean foundCandidate=false;
			for(org.polito.model.template.Port templatePort: template.getPorts())
			{
				String prefix = templatePort.getName(); // example: eth | ens
				String position = templatePort.getPosition(); //example: 0-10 | 0-N | 0-0
				int firstPosition, lastPosition=100;
				firstPosition = Integer.parseInt(position.substring(0, position.indexOf('-')));
				String lastPositionString = position.substring(position.indexOf('-')+1);
				if(lastPositionString.equals("N"))
					unboundedCase=true;
				else
					lastPosition=Integer.parseInt(lastPositionString);

				for(int pos=firstPosition; foundCandidate==false && (unboundedCase==true || pos<=lastPosition); pos++ )
				{
					possiblePortName=prefix+pos;
					foundCandidate=true;
					for(Port vnfExistingPort: ports)
						if(vnfExistingPort.getName().equals(possiblePortName))
						{
							foundCandidate=false;
							break;
						}
				}

				if(foundCandidate)
				{
					newPortName=possiblePortName;
					break;
				}
			}
			//TODO: if(!foundCandidate)
			// launch exception
		}

		String newPortId = "port:"+portNumb;
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

	public static void setTemplateToVnf(Nffg nffg, String vnfId, VnfTemplate vnfTemplate) {
		getVnfById(nffg,vnfId).setTemplateObject(vnfTemplate);
	}

	public static List<EndpointWrapper> getEndpointByName(Nffg nffg, String endpointName) {
		List<EndpointWrapper> endpoints = new ArrayList<EndpointWrapper>();
		if(nffg!=null)
			for(EndpointWrapper endpoint: nffg.getEndpoints())
				if(endpoint.getName().equals(endpointName))
					endpoints.add(endpoint);
		return endpoints;
	}

	public static boolean areConnected(Nffg nffg, String vnfId, String portId, String otherId) {
		String matchToSerach = "vnf:"+ vnfId + ":" + portId;
		boolean found=false;
		for(FlowRule flowRule: nffg.getFlowRules())
		{
			if(flowRule.getMatch().getInput().equals(matchToSerach))
				for(Action action: flowRule.getActions())
					if(action.getOutput().contains(otherId))
					{
						found=true;
						break;
					}
			if(found)
				break;
		}
		return found;
	}

	public static void destroyVnf(Nffg nffg, String id) {
		// Firstly destroy links:
		List<FlowRule> flowRulesToDelete = new ArrayList<>();
		String ruleEndpointToDelete = "vnf:"+ id ;
		for(FlowRule flowRule: nffg.getFlowRules())
		{
			Match match = flowRule.getMatch();
			if(match.getInput().contains(ruleEndpointToDelete))
			{
				flowRulesToDelete.add(flowRule);
				continue;
			}
			for(Action action: flowRule.getActions())
			{
				if(action.getOutput().contains(ruleEndpointToDelete))
				{
					flowRulesToDelete.add(flowRule);
					String[] splittedMatch = match.getInput().split(":");
					if(splittedMatch[0].equals("vnf"))
						deleteVnfPort(nffg,splittedMatch[1],splittedMatch[2]+":"+splittedMatch[3]);
				}

			}
		}
		nffg.getFlowRules().removeAll(flowRulesToDelete);
		// Then destroy the VNF
		nffg.getVnfs().remove(getVnfById(nffg, id));
	}

	private static void deleteVnfPort(Nffg nffg, String vnfId, String portId) {
		Vnf vnf = getVnfById(nffg, vnfId);
		Port portToDelete = null;
		for(Port port: vnf.getPorts())
			if(port.getId().equals(portId))
			{
				portToDelete = port;
				break;
			}
		vnf.getPorts().remove(portToDelete);
	}

}
