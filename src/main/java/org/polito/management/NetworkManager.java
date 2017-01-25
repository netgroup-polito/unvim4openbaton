package org.polito.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.polito.unvim.UnClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NetworkManager {
	private static final String NETWORK_PREFIX = "SW_";
	private static final String SUBNET_PREFIX = "DHCP_";
	private static final String ROUTER_PREFIX = "ROUTER_";
	private static final String NETWORK = "network";
	private static final String SUBNET = "subnet";
	private static final String MANAGEMENT_NETWORK = "manag_network";
	private static final String MANAGEMENT_SUBNET = "manag_subnet";
	private static final String MANAGEMENT_ROUTER = "manag_extNet";
	private static Logger log = LoggerFactory.getLogger(UnClient.class);


	public static void createNetwork(Nffg managementNffg, Nffg tenantNffg, Network network) {
		String vnfNetId = NffgManager.getNewId(tenantNffg.getVnfs());
		NffgManager.createVnf(tenantNffg,vnfNetId,NETWORK_PREFIX + network.getName(),NETWORK,"switch",null);
		network.setExtId(vnfNetId);
		// attach the new switch (in the tenant graph) to the router (in the management graph)
		String managemtnRouterId = NffgManager.getVnfsByDescription(managementNffg, MANAGEMENT_ROUTER).get(0).getId();
		NffgManager.connectGraphToGraph(tenantNffg,vnfNetId,managementNffg,managemtnRouterId);
	}

	public static void createSubnet(Nffg managementNffg, Nffg tenantNffg, Network network, Subnet subnet, String datastoreEndpoint) throws VimDriverException
	{
		String vnfSubId = NffgManager.getNewId(tenantNffg.getVnfs());
		NffgManager.createVnf(tenantNffg,vnfSubId,SUBNET_PREFIX + subnet.getName(),SUBNET,null,"configurableDhcp");
		if(datastoreEndpoint!=null)
		{
			VnfTemplate vnfTemplate = DatastoreProxy.getTemplate(datastoreEndpoint, "configurableDhcp");
			NffgManager.setTemplateToVnf(tenantNffg,vnfSubId,vnfTemplate);
		}
		subnet.setExtId(vnfSubId);
		String managementNetId = NffgManager.getVnfsByDescription(managementNffg,MANAGEMENT_NETWORK).get(0).getId();
		NffgManager.connectGraphToGraph(tenantNffg,vnfSubId,managementNffg,managementNetId,true); // trusted port!!
		NffgManager.connectVnfToVnf(tenantNffg,vnfSubId,network.getExtId(),false);
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

	public static void configureSubnet(Nffg managementNffg, Nffg tenantNffg, Network createdNetwork, Subnet subnet, Properties properties, String configurationService) throws VimDriverException {
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
		Vnf router = NffgManager.getVnfsByDescription(managementNffg, MANAGEMENT_ROUTER).get(0);
		for(Port port: router.getPorts())
			if(port.isTrusted())
				YangManager.addInterface(natYang, port.getName(), "10.0.0.1", "dhcp", "config", "");
			else if(NffgManager.areConnected(managementNffg, router.getId(), port.getId(), tenantNffg, createdNetwork.getExtId()))
				YangManager.addInterface(natYang, port.getName(), defaultGateway, "static", "lan", "");
			else if(NffgManager.areConnected(managementNffg, router.getId(), port.getId(), NffgManager.getEndpointByName(managementNffg,"managementInterface").get(0).getId()))
				YangManager.addInterface(natYang, port.getName(), "10.0.0.1", "dhcp", "wan", "");

		// Send the yang
		String routerMacControlPort = NffgManager.getMacControlPort(managementNffg,NffgManager.getVnfsByDescription(managementNffg, MANAGEMENT_ROUTER).get(0).getId());
		DatastoreProxy.sendNatYang(configurationService, natYang, tenantNffg.getId(), managementNffg.getId(), routerMacControlPort);

		// Create Dhcp Yang
		DhcpYang dhcpYang = new DhcpYang();
		YangManager.setServerSection(dhcpYang,sectionStartIp,sectionStopIp);
		YangManager.setServerIpPoolParameters(dhcpYang,properties.getProperty("dhcp.defaultLeaseTime")
				,properties.getProperty("dhcp.maxLeaseTime"),properties.getProperty("dhcp.domainNameServer")
				,properties.getProperty("dhcp.domainName"));
		YangManager.setServerDefaultGateway(dhcpYang,defaultGateway,netmask);
		Vnf dhcp = NffgManager.getVnfById(tenantNffg, subnet.getExtId());
		for(Port port: dhcp.getPorts())
			if(port.isTrusted())
				YangManager.addInterface(dhcpYang, port.getName(), "10.0.0.1", "dhcp", "config", "");
			else
				YangManager.addInterface(dhcpYang, port.getName(), dhcpUserPortIp, "static", "dhcp", defaultGateway);

		// Send the yang
		String dhcpMacControlPort = NffgManager.getMacControlPort(tenantNffg,subnet.getExtId());
		DatastoreProxy.sendDhcpYang(configurationService, dhcpYang, tenantNffg.getId(), tenantNffg.getId(), dhcpMacControlPort);

		subnet.setGatewayIp(defaultGateway);
	}

	public static void deleteNetwork(Nffg tenantNffg, Nffg managementNffg, String id) {
		NffgManager.disconnectGraphs(tenantNffg,id,managementNffg);
		NffgManager.destroyVnf(tenantNffg,id);
	}

	public static void deleteSubnet(Nffg tenantNffg, Nffg managementNffg, String id) {
		NffgManager.disconnectGraphs(tenantNffg,id,managementNffg);
		NffgManager.destroyVnf(tenantNffg,id);
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

	public static List<Vnf> getBelongingNetworks(Nffg nffg, String serverId) {
		List<Vnf> belongingNetworks = new ArrayList<>();
		for(Vnf vnfNet: NffgManager.getVnfsByDescription(nffg, NETWORK))
			for(Port vnfNetPort: vnfNet.getPorts())
				if(NffgManager.areConnected(nffg, vnfNet.getId(), vnfNetPort.getId(), serverId))
				{
					belongingNetworks.add(vnfNet);
					break;
				}
		return belongingNetworks;
	}

	public static Vnf getBelongingSubnet(Nffg nffg, String netowrkId) {
		for(Vnf vnfSub: NffgManager.getVnfsByDescription(nffg, SUBNET))
			for(Port vnfSubPort: vnfSub.getPorts())
				if(NffgManager.areConnected(nffg, vnfSub.getId(), vnfSubPort.getId(), netowrkId))
				return vnfSub;
		return null;
	}

	public static Map<String, List<String>> getNetworkIpAddressAssociation(Nffg nffg, Vnf vnfServer, String configurationService) throws VimDriverException {
		List<Vnf> vnfNetworks = getBelongingNetworks(nffg,vnfServer.getId());
		Map<String,List<String>> networkMacAddressAssociation = getNetworkAndMacAddressAssociation(nffg,vnfServer,vnfNetworks);
		Map<String,List<String>> networkIpAddressAssociation = new HashMap<>();

		boolean gotAllAddresses = false;
		while(!gotAllAddresses)
		{
			gotAllAddresses=true;
			for(Vnf vnfNet: vnfNetworks)
			{
				String netName = vnfNet.getName().replaceAll(NETWORK_PREFIX, "");
				if(networkIpAddressAssociation.get(netName) == null || (networkIpAddressAssociation.get(netName).size()
						!= networkMacAddressAssociation.get(netName).size()))
				{
					Vnf vnfSubnet = NetworkManager.getBelongingSubnet(nffg,vnfNet.getId());
					DhcpYang dhcpYang = DatastoreProxy.getDhcpYang(configurationService, "openbaton", nffg.getId(), NffgManager.getMacControlPort(nffg, vnfSubnet.getId()));
					if(dhcpYang==null)
					{
						log.debug("The Dhcp with id " + vnfSubnet.getId() + " is not registered to the configuration service yet. Sleeping 3 seconds..");
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							log.debug("Sleeping 3 seconds.. failed!");
						}
						gotAllAddresses=false;
						continue;
					}
					Map<String, String> macIpAssociation = YangManager.readClientAddresses(dhcpYang);
					List<String> macAddressesToMatch = networkMacAddressAssociation.get(netName);
					for(String mac: macAddressesToMatch)
					{
						String ipAddress = macIpAssociation.get(mac);
						if(ipAddress==null)
						{
							log.debug("The Dhcp with id " + vnfSubnet.getId() + " has not assigned an Ip address to the interface with mac address: '"+ mac + "' yet. Sleeping 3 seconds..");
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								log.debug("Sleeping 3 seconds.. failed!");
							}
							gotAllAddresses=false;
							break;
						}
						List<String> ipAddressesInNet = networkIpAddressAssociation.get(netName);
						if(ipAddressesInNet==null)
						{
							ipAddressesInNet = new ArrayList<>();
							networkIpAddressAssociation.put(netName,ipAddressesInNet);
						}
						ipAddressesInNet.add(ipAddress);
					}
				}
					
			}
		}
		return networkIpAddressAssociation;
	}

	static Map<String,List<String>> getNetworkAndMacAddressAssociation(Nffg nffg, Vnf vnfServer, List<Vnf> vnfNetworks)
	{
		Map<String,List<String>> networkMacAddressAssociation = new HashMap<>();  // netName - listOfMacAddresses
		for(Vnf vnfNet: vnfNetworks)
		{
			for(Port port: vnfServer.getPorts())
			{
				if(NffgManager.areConnected(nffg, vnfServer.getId(), port.getId(), vnfNet.getId()))
				{
					List<String> macAddresses = networkMacAddressAssociation.get(vnfNet.getName().replaceAll(NETWORK_PREFIX, ""));
					if(macAddresses==null)
					{
						macAddresses = new ArrayList<>();
						networkMacAddressAssociation.put(vnfNet.getName().replaceAll(NETWORK_PREFIX, ""), macAddresses);
					}
					macAddresses.add(port.getMacAddress());
				}
			}
		}
		return networkMacAddressAssociation;
	}
}
