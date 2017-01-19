package org.polito.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.Subnet;
import org.openbaton.exceptions.VimDriverException;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.Port;
import org.polito.model.nffg.Vnf;
import org.polito.model.template.VnfTemplate;
import org.polito.model.nffg.AbstractEP.Type;
import org.polito.model.yang.dhcp.DhcpYang;
import org.polito.proxy.DatastoreProxy;


public class NetworkManager {
	private static final String NETWORK_PREFIX = "SW_";
	private static final String SUBNET_PREFIX = "DHCP_";
	private static final String ROUTER_PREFIX = "ROUTER_";
	private static final String NETWORK = "network";
	private static final String SUBNET = "subnet";
	private static final String MANAGEMENT_NETWORK = "manag_network";
	private static final String MANAGEMENT_SUBNET = "manag_subnet";
	private static final String MANAGEMENT_ROUTER = "manag_extNet";


	public static void createNetwork(Nffg nffg, Network network) {
		String vnfId = NffgManager.getNewId(nffg.getVnfs());
		NffgManager.createVnf(nffg,vnfId,NETWORK_PREFIX + network.getName(),NETWORK,"switch",null);
		network.setExtId(vnfId);
		// attach the new switch to the router
		String managemtnRouterId = NffgManager.getVnfsByDescription(nffg, MANAGEMENT_ROUTER).get(0).getId();
		NffgManager.connectVnfToVnf(nffg,vnfId,managemtnRouterId,false);
	}

	public static void createSubnet(Nffg nffg, Network network, Subnet subnet, String datastoreEndpoint) throws VimDriverException
	{
		String vnfId = NffgManager.getNewId(nffg.getVnfs());
		NffgManager.createVnf(nffg,vnfId,SUBNET_PREFIX + subnet.getName(),SUBNET,null,"configurableDhcp");
		if(datastoreEndpoint!=null)
		{
			VnfTemplate vnfTemplate = DatastoreProxy.getTemplate(datastoreEndpoint, "configurableDhcp");
			NffgManager.setTemplateToVnf(nffg,vnfId,vnfTemplate);
		}
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

	public static void createManagementNetwork(Nffg nffg, List<String> unPhisicalPorts) {
		String managementSwId = NffgManager.getNewId(nffg.getVnfs());
		String managementDhcpId = NffgManager.getNewId(nffg.getVnfs());
		String managementRouterId = NffgManager.getNewId(nffg.getVnfs());
		String managementHoststackId = NffgManager.getNewId(nffg.getEndpoints());
		String managementInterfaceId = NffgManager.getNewId(nffg.getEndpoints());
		NffgManager.createVnf(nffg, managementSwId, NETWORK_PREFIX + MANAGEMENT_NETWORK, MANAGEMENT_NETWORK, null, "managementSwitch");
		NffgManager.createVnf(nffg, managementDhcpId, SUBNET_PREFIX + MANAGEMENT_SUBNET, MANAGEMENT_SUBNET, null, "managementDhcp");
		NffgManager.createVnf(nffg, managementRouterId, ROUTER_PREFIX + MANAGEMENT_ROUTER, MANAGEMENT_ROUTER, null, "managementRouter");
		NffgManager.createEndpoint(nffg,managementHoststackId,"managementHoststack",Type.HOSTSTACK, "static", "192.168.4.1");
		NffgManager.createEndpoint(nffg,managementInterfaceId,"managementInterface",Type.INTERFACE, unPhisicalPorts.get(0));
		NffgManager.connectVnfToVnf(nffg,managementSwId,managementDhcpId,false);
		NffgManager.connectVnfToVnf(nffg,managementRouterId,managementSwId,true);
		NffgManager.connectEndpointToVnf(nffg,managementHoststackId,managementSwId);
		NffgManager.connectEndpointToVnf(nffg,managementInterfaceId,managementRouterId);
	}

	private static void writeDhcpConfiguration(Nffg nffg, DhcpYang yang, Subnet subnet, Properties properties) throws VimDriverException {
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

	private static void writeRouterConfiguration(Nffg nffg, DhcpYang yang, Subnet subnet, Properties properties) throws VimDriverException {
		// TODO
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

	public static void configureSubnet(Nffg nffg, Subnet subnet, Properties properties, String configurationService) throws VimDriverException {
		DhcpYang yang = new DhcpYang();
		NetworkManager.writeDhcpConfiguration(nffg,yang,subnet,properties);
		String mac = NffgManager.getMacControlPort(nffg,subnet.getExtId());
		DatastoreProxy.sendDhcpYang(configurationService, yang, "openbaton", nffg.getId(), mac);
		yang = new DhcpYang();
		NetworkManager.writeRouterConfiguration(nffg,yang,subnet,properties);
		mac = NffgManager.getMacControlPort(nffg,NffgManager.getVnfsByDescription(nffg, MANAGEMENT_ROUTER).get(0).getId());
		DatastoreProxy.sendDhcpYang(configurationService, yang, "openbaton", nffg.getId(), mac);
	}

}
