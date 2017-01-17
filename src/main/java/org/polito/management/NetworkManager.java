package org.polito.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.Subnet;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.VimDriverException;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.Port;
import org.polito.model.nffg.Vnf;
import org.polito.model.nffg.AbstractEP.Type;
import org.polito.model.yang.dhcp.DhcpYang;
import org.polito.proxy.UniversalNodeProxy;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NetworkManager {
	private static final String NETWORK_PREFIX = "SW_";
	private static final String SUBNET_PREFIX = "DHCP_";
	private static final String NETWORK = "network";
	private static final String SUBNET = "subnet";
	private static final String MANAGEMENT_NETWORK = "manag_network";
	private static final String MANAGEMENT_SUBNET = "manag_subnet";


	public static void createNetwork(Nffg nffg, Network network) {
		String vnfId = NffgManager.getNewId(nffg.getVnfs());
		network.setExtId(vnfId);
		// the switch representing the network will be deployed on the createSubnet command
	}

	public static void createSubnet(Nffg nffg, Network network, Subnet subnet)
	{
		boolean netAlreadyDeployed = false;
		for(Vnf vnf: NffgManager.getVnfsByDescription(nffg, NETWORK))
			if(vnf.getId().equals(network.getExtId()))
			{
				netAlreadyDeployed=true;
				break;
			}
		if(!netAlreadyDeployed)
			NffgManager.createVnf(nffg,network.getExtId(),NETWORK_PREFIX + network.getName(),NETWORK,"switch",null);

		String vnfId = NffgManager.getNewId(nffg.getVnfs());
		NffgManager.createVnf(nffg,vnfId,SUBNET_PREFIX + subnet.getName(),SUBNET,null,"configurableDhcp");
		subnet.setExtId(vnfId);
		String managementNetId = NffgManager.getVnfsByDescription(nffg,MANAGEMENT_NETWORK).get(0).getId();
		NffgManager.connectVnfToVnf(nffg,vnfId,managementNetId,true);
		NffgManager.connectVnfToVnf(nffg,vnfId,network.getExtId(),false);
	}

	public static List<Network> getNetworks(Nffg nffg) {
		List<Network> networks = new ArrayList<>();
		List<Vnf> vnfList = NffgManager.getVnfsByDescription(nffg,NETWORK);
		for(Vnf vnf: vnfList)
		{
			Network net = new Network();
			net.setExtId(vnf.getId());
			net.setName(vnf.getName().replaceAll(NETWORK_PREFIX, ""));
			networks.add(net);
			//TODO: Set subnets
		}
		return networks;
	}

	public static void createManagementNetwork(Nffg nffg) {
		String managementSwId = NffgManager.getNewId(nffg.getVnfs());
		String managementDhcpId = NffgManager.getNewId(nffg.getVnfs());
		String managementHoststackId = NffgManager.getNewId(nffg.getEndpoints());
		NffgManager.createVnf(nffg, managementSwId, NETWORK_PREFIX + MANAGEMENT_NETWORK, MANAGEMENT_NETWORK, null, "managementSwitch");
		NffgManager.createVnf(nffg, managementDhcpId, SUBNET_PREFIX + MANAGEMENT_SUBNET, MANAGEMENT_SUBNET, null, "managementDhcp");
		NffgManager.createEndpoint(nffg,managementHoststackId,"managementHoststack",Type.HOSTSTACK, "static", "192.168.4.1");
		NffgManager.connectVnfToVnf(nffg,managementSwId,managementDhcpId,false);
		NffgManager.connectEndpointToVnf(nffg,managementHoststackId,managementSwId);
	}

	public static void writeSubnetConfiguration(Nffg nffg, DhcpYang yang, Subnet subnet, Properties properties) throws VimDriverException {
		SubnetInfo subnetInfo = new SubnetUtils(subnet.getCidr()).getInfo();
		String netmask = subnetInfo.getNetmask();
		String defaultGateway = subnetInfo.getLowAddress();
		String sectionStopIp = subnetInfo.getHighAddress();
		String dhcpUserPortIp = nextIpAddress(defaultGateway);
		if(!subnetInfo.isInRange(dhcpUserPortIp))
			throw new VimDriverException("Network range is too small");
		String sectionStartIp = nextIpAddress(dhcpUserPortIp);
		if(!subnetInfo.isInRange(sectionStartIp))
			throw new VimDriverException("Network range is too small");
		YangManager.setServerSection(yang,sectionStartIp,sectionStopIp);
		YangManager.setServerIpPoolParameters(yang,properties.getProperty("dhcp.defaultLeaseTime")
				,properties.getProperty("dhcp.maxLeaseTime"),properties.getProperty("dhcp.domainNameServer")
				,properties.getProperty("dhcp.domainName"));
		YangManager.setServerDefaultGateway(yang,defaultGateway,netmask);
		Vnf dhcp = NffgManager.getVnfById(nffg, subnet.getExtId());
		for(Port port: dhcp.getPorts())
			if(port.isTrusted())
				YangManager.addInterface(yang, port.getName(), "", "dhcp", "config", defaultGateway);
			else
				YangManager.addInterface(yang, port.getName(), dhcpUserPortIp, "static", "dhcp", "");
	}

	private static final String nextIpAddress(final String input) {
	    final String[] tokens = input.split("\\.");
	    if (tokens.length != 4)
	        throw new IllegalArgumentException();
	    for (int i = tokens.length - 1; i >= 0; i--) {
	        final int item = Integer.parseInt(tokens[i]);
	        if (item < 255) {
	            tokens[i] = String.valueOf(item + 1);
	            for (int j = i + 1; j < 4; j++) {
	                tokens[j] = "0";
	            }
	            break;
	        }
	    }
	    return new StringBuilder()
	    .append(tokens[0]).append('.')
	    .append(tokens[1]).append('.')
	    .append(tokens[2]).append('.')
	    .append(tokens[3])
	    .toString();
	}

}
