package org.polito.management;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import org.polito.model.yang.nat.NatYang;
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

	public static List<Network> getNetworks(Nffg nffg, String configurationService) throws VimDriverException {
		List<Network> networks = new ArrayList<>();
		List<Vnf> vnfList = NffgManager.getVnfsByDescription(nffg,NETWORK);
		for(Vnf vnf: vnfList)
			networks.add(getNetwork(nffg, vnf.getId(),configurationService));
		return networks;
	}

	public static Network getNetwork(Nffg nffg, String netId, String configurationService) throws VimDriverException {
		Vnf vnfNet = NffgManager.getVnfById(nffg, netId);
		Network net = new Network();
		net.setExtId(vnfNet.getId());
		net.setName(vnfNet.getName().replaceAll(NETWORK_PREFIX, ""));
		Set<Subnet> subnets = new HashSet<>();
		for(Vnf vnfSub: NffgManager.getVnfsByDescription(nffg, MANAGEMENT_SUBNET))
			for(Port vnfNetPort: vnfNet.getPorts())
				if(NffgManager.areConnected(nffg, vnfNet.getId(), vnfNetPort.getId(), vnfSub.getId()))
				{
					subnets.add(getSubnet(nffg, vnfSub, configurationService));
					break;
				}
		net.setSubnets(subnets);
		return net;
	}

	private static Subnet getSubnet(Nffg nffg, Vnf vnfSubnet, String configurationService) throws VimDriverException {
		DhcpYang dhcpYang = DatastoreProxy.getDhcpYang(configurationService, "openbaton", nffg.getId(), NffgManager.getMacControlPort(nffg, vnfSubnet.getId()));
		String gatewayIp = YangManager.getServerDefaultGatewayIp(dhcpYang);
		String gatewayMask = YangManager.getServerDefaultGatewayMask(dhcpYang);
		SubnetInfo subnetInfo = new SubnetUtils(gatewayIp,gatewayMask).getInfo();
		Subnet subnet = new Subnet();
		subnet.setName(vnfSubnet.getName().replaceAll(SUBNET_PREFIX, ""));
		subnet.setExtId(vnfSubnet.getId());
		subnet.setGatewayIp(gatewayIp);
		subnet.setCidr(subnetInfo.getCidrSignature());
		return subnet;
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

	public static void configureSubnet(Nffg nffg, Network createdNetwork, Subnet subnet, Properties properties, String configurationService) throws VimDriverException {
		// Calculate network parameters
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

		// Create Nat Yang
		NatYang natYang = new NatYang();
		Vnf router = NffgManager.getVnfsByDescription(nffg, MANAGEMENT_ROUTER).get(0);
		for(Port port: router.getPorts())
			if(port.isTrusted())
				YangManager.addInterface(natYang, port.getName(), "10.0.0.1", "dhcp", "config", "");
			else if(NffgManager.areConnected(nffg, router.getId(), port.getId(), createdNetwork.getExtId()))
				YangManager.addInterface(natYang, port.getName(), defaultGateway, "static", "lan", "");
			else if(NffgManager.areConnected(nffg, router.getId(), port.getId(), NffgManager.getEndpointByName(nffg,"managementInterface").get(0).getId()))
				YangManager.addInterface(natYang, port.getName(), "10.0.0.1", "dhcp", "wan", "");

		// Send the yang
		String routerMacControlPort = NffgManager.getMacControlPort(nffg,NffgManager.getVnfsByDescription(nffg, MANAGEMENT_ROUTER).get(0).getId());
		DatastoreProxy.sendNatYang(configurationService, natYang, "openbaton", nffg.getId(), routerMacControlPort);

		// Create Dhcp Yang
		DhcpYang dhcpYang = new DhcpYang();
		YangManager.setServerSection(dhcpYang,sectionStartIp,sectionStopIp);
		YangManager.setServerIpPoolParameters(dhcpYang,properties.getProperty("dhcp.defaultLeaseTime")
				,properties.getProperty("dhcp.maxLeaseTime"),properties.getProperty("dhcp.domainNameServer")
				,properties.getProperty("dhcp.domainName"));
		YangManager.setServerDefaultGateway(dhcpYang,defaultGateway,netmask);
		Vnf dhcp = NffgManager.getVnfById(nffg, subnet.getExtId());
		for(Port port: dhcp.getPorts())
			if(port.isTrusted())
				YangManager.addInterface(dhcpYang, port.getName(), "10.0.0.1", "dhcp", "config", "");
			else
				YangManager.addInterface(dhcpYang, port.getName(), dhcpUserPortIp, "static", "dhcp", defaultGateway);

		// Send the yang
		String dhcpMacControlPort = NffgManager.getMacControlPort(nffg,subnet.getExtId());
		DatastoreProxy.sendDhcpYang(configurationService, dhcpYang, "openbaton", nffg.getId(), dhcpMacControlPort);
	}

	public static void deleteNetwork(Nffg nffg, String id) {
		NffgManager.destroyVnf(nffg,id);
	}

	public static void deleteSubnet(Nffg nffg, String id) {
		NffgManager.destroyVnf(nffg,id);
	}

	public static List<String> getSubnetsIds(Nffg nffg, String networkId) {
		List<String> subnetsIds = new ArrayList<>();
		Vnf vnfNet = NffgManager.getVnfById(nffg, networkId);
		for(Vnf vnfSub: NffgManager.getVnfsByDescription(nffg, SUBNET))
			for(Port vnfNetPort: vnfNet.getPorts())
				if(NffgManager.areConnected(nffg, vnfNet.getId(), vnfNetPort.getId(), vnfSub.getId()))
				{
					subnetsIds.add(vnfSub.getId());
					break;
				}
		return subnetsIds;
	}

}
