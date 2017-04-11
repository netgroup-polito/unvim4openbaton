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
import org.polito.model.message.FloatingIpPool;
import org.polito.model.nffg.AbstractEP.Type;
import org.polito.model.yang.IfEntry;
import org.polito.model.yang.dhcp.DhcpYang;
import org.polito.model.yang.nat.FloatingIp;
import org.polito.model.yang.nat.NatYang;
import org.polito.proxy.ConfigurationServiceProxy;
import org.polito.proxy.DatastoreProxy;
import org.polito.unvim.UnClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NetworkManager {
	private static final String NETWORK_PREFIX = "SW_";
	private static final String SUBNET_PREFIX = "DHCP_";
	private static final String NETWORK = "network";
	private static final String SUBNET = "subnet";
	private static final String MANAGEMENT_SWITCH = "manag_SWITCH";
	private static final String MANAGEMENT_DHCP = "manag_DHCP";
	private static final String MANAGEMENT_ROUTER = "manag_ROUTER";
	private static final String MANAGEMENT_SWITCH_TEMPLATE = "managementSwitch";
	private static final String MANAGEMENT_DHCP_TEMPLATE = "managementDhcp";
	private static final String MANAGEMENT_ROUTER_TEMPLATE = "managementRouter";
	private static final String OPERATOR_ROUTER = "operator_ROUTER";
	private static final String OPERATOR_ROUTER_TEMPLATE = "operatorRouter";
	private static final String EXTERNAL_NET_ENDPOINT = "wanEp";
	private static Logger log = LoggerFactory.getLogger(UnClient.class);


	public static void createNetwork(Nffg operatorNffg, Nffg tenantNffg, Network network) {
		String vnfNetId = NffgManager.getNewId(tenantNffg.getVnfs());
		NffgManager.createVnf(tenantNffg,vnfNetId,NETWORK_PREFIX + network.getName(),NETWORK,"switch",null);
		network.setExtId(vnfNetId);
		// attach the new switch (in the tenant graph) to the router (in the management graph)
		String moperatorRouterId = NffgManager.getVnfsByDescription(operatorNffg, OPERATOR_ROUTER).get(0).getId();
		NffgManager.connectGraphToGraph(tenantNffg,vnfNetId,operatorNffg,moperatorRouterId);
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
		String managementNetId = NffgManager.getVnfsByDescription(managementNffg,MANAGEMENT_SWITCH).get(0).getId();
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
		for(Vnf vnfSub: NffgManager.getVnfsByDescription(nffg, SUBNET))
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
		DhcpYang dhcpYang = ConfigurationServiceProxy.getDhcpYang(configurationService, nffg.getId(), nffg.getId(), NffgManager.getMacControlPort(nffg, vnfSubnet.getId()));
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

	public static Map<String,Nffg> createBootGraphs()
	{
		// Creating management Graph
		Nffg managementNffg = NffgManager.createEmptyNffg("management_graph");
		String managementSwId = NffgManager.getNewId(managementNffg.getVnfs());
		String managementDhcpId = NffgManager.getNewId(managementNffg.getVnfs());
		String managementRouterId = NffgManager.getNewId(managementNffg.getVnfs());
		String manWanMergePointId = NffgManager.getNewId(managementNffg.getEndpoints());
		NffgManager.createVnf(managementNffg, managementSwId, MANAGEMENT_SWITCH, MANAGEMENT_SWITCH, null, MANAGEMENT_SWITCH_TEMPLATE);
		NffgManager.createVnf(managementNffg, managementDhcpId, MANAGEMENT_DHCP, MANAGEMENT_DHCP, null, MANAGEMENT_DHCP_TEMPLATE);
		NffgManager.createVnf(managementNffg, managementRouterId, MANAGEMENT_ROUTER, MANAGEMENT_ROUTER, null, MANAGEMENT_ROUTER_TEMPLATE);
		NffgManager.createEndpoint(managementNffg, manWanMergePointId, EXTERNAL_NET_ENDPOINT, Type.INTERNAL, EXTERNAL_NET_ENDPOINT);
		NffgManager.connectVnfToVnf(managementNffg,managementSwId,managementDhcpId,false);
		NffgManager.connectVnfToVnf(managementNffg,managementRouterId,managementSwId,true);
		NffgManager.connectEndpointToVnf(managementNffg,manWanMergePointId,managementRouterId);

		// Creating operator Graph
		Nffg operatorNffg = NffgManager.createEmptyNffg("operator_graph");
		String opWanMergePointId = NffgManager.getNewId(operatorNffg.getEndpoints());
		String operatorRouterId = NffgManager.getNewId(operatorNffg.getVnfs());
		NffgManager.createEndpoint(operatorNffg, opWanMergePointId, EXTERNAL_NET_ENDPOINT, Type.INTERNAL, EXTERNAL_NET_ENDPOINT);
		NffgManager.createVnf(operatorNffg, operatorRouterId, OPERATOR_ROUTER, OPERATOR_ROUTER, null, OPERATOR_ROUTER_TEMPLATE);

		// Connecting the two graphs
		Map<String,Nffg> graphs = new HashMap<String, Nffg>();
		NffgManager.connectGraphToGraph(operatorNffg, operatorRouterId, managementNffg, managementSwId, true);
		NffgManager.connectEndpointToVnf(operatorNffg,opWanMergePointId,operatorRouterId);
		graphs.put(managementNffg.getId(), managementNffg);
		graphs.put(operatorNffg.getId(), operatorNffg);
		return graphs;
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

	public static void configureSubnet(Nffg managementNffg, Nffg operatorNffg, Nffg tenantNffg, Network createdNetwork, Subnet subnet, Properties properties, String configurationService) throws VimDriverException {
		boolean firstConfiguration=false;
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
		Vnf router = NffgManager.getVnfsByDescription(operatorNffg, OPERATOR_ROUTER).get(0);
		String routerMacControlPort = NffgManager.getMacControlPort(operatorNffg,router.getId());
		NatYang natYang = ConfigurationServiceProxy.getNatYang(configurationService, operatorNffg.getId(), operatorNffg.getId(), routerMacControlPort);
		if(natYang==null)
		{
			natYang = new NatYang();
			firstConfiguration=true;
		}
		for(Port port: router.getPorts())
			if(firstConfiguration && port.isTrusted())
				YangManager.addInterface(natYang, port.getName(), null, "dhcp", "config", null);
			else if(NffgManager.areConnected(operatorNffg, router.getId(), port.getId(), tenantNffg, createdNetwork.getExtId()))
				YangManager.addInterface(natYang, port.getName(), defaultGateway, "static", "lan", null);
			else if(firstConfiguration && NffgManager.areConnected(operatorNffg, router.getId(), port.getId(), NffgManager.getEndpointByName(operatorNffg, EXTERNAL_NET_ENDPOINT).get(0).getId()))
				YangManager.addInterface(natYang, port.getName(), null, "dhcp", "wan", null);

		// Send the yang
		ConfigurationServiceProxy.sendNatYang(configurationService, natYang, operatorNffg.getId(), operatorNffg.getId(), routerMacControlPort);

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
				YangManager.addInterface(dhcpYang, port.getName(), null, "dhcp", "config", null);
			else
				YangManager.addInterface(dhcpYang, port.getName(), dhcpUserPortIp, "static", "dhcp", null);

		// Send the yang
		String dhcpMacControlPort = NffgManager.getMacControlPort(tenantNffg,subnet.getExtId());
//		try {
//			Thread.sleep(new Long(10000));
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		ConfigurationServiceProxy.sendDhcpYang(configurationService, dhcpYang, tenantNffg.getId(), tenantNffg.getId(), dhcpMacControlPort);

		subnet.setGatewayIp(defaultGateway);
	}

	public static void deleteNetwork(Nffg tenantNffg, Nffg operatorNffg, String id) {
		String operatorRouterId = NffgManager.getVnfsByDescription(operatorNffg, OPERATOR_ROUTER).get(0).getId();
		NffgManager.disconnectGraphs(tenantNffg,id,operatorNffg,operatorRouterId);
		NffgManager.destroyVnf(tenantNffg,id);
	}

	public static void deleteSubnet(Nffg tenantNffg, Nffg managementNffg, String id) {
		String managementNetId = NffgManager.getVnfsByDescription(managementNffg,MANAGEMENT_SWITCH).get(0).getId();
		NffgManager.disconnectGraphs(tenantNffg,id,managementNffg,managementNetId);
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
		int secondsDhcpGet=0;
		int secondsIpAddress=0;
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
					DhcpYang dhcpYang = ConfigurationServiceProxy.getDhcpYang(configurationService, nffg.getId(), nffg.getId(), NffgManager.getMacControlPort(nffg, vnfSubnet.getId()));
					if(dhcpYang==null)
					{
						if(secondsDhcpGet>44)
							throw new VimDriverException("The Dhcp with id " + vnfSubnet.getId() + " has not be able to register itself to the configuration service in 45 seconds!");
						secondsDhcpGet+=3;
						log.debug("The Dhcp with id " + vnfSubnet.getId() + " is not registered to the configuration service yet. Sleeping 3 seconds... ["+secondsDhcpGet+"]");
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
							if(secondsIpAddress>119)
								throw new VimDriverException("the interface with mac address: '"+ mac + " has not be able to get an Ip Address in 120 seconds!");
							secondsIpAddress+=3;
							log.debug("The Dhcp with id " + vnfSubnet.getId() + " has not assigned an Ip address to the interface with mac address: '"+ mac + "' yet. Sleeping 3 seconds... ["+secondsIpAddress+"]");
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

	public static Map<String, String> getFloatingIps(Nffg operatorNffg, Nffg nffg, Vnf vnfServer, String configurationService) throws VimDriverException {
		Map<String, String> floatingips = new HashMap<>();
		Map<String, List<String>> networkIpAddressAssociation = getNetworkIpAddressAssociation(nffg, vnfServer, configurationService);
		String routerMacControlPort = NffgManager.getMacControlPort(operatorNffg,NffgManager.getVnfsByDescription(operatorNffg, OPERATOR_ROUTER).get(0).getId());
		NatYang natYang = ConfigurationServiceProxy.getNatYang(configurationService, operatorNffg.getId(), operatorNffg.getId(), routerMacControlPort);
		for(String network: networkIpAddressAssociation.keySet())
			for(String ipInNet: networkIpAddressAssociation.get(network))
				for(FloatingIp floatingIp: natYang.getConfigNatStaticBindings().getFloatingIp())
					if(floatingIp.getPrivateAddress().equals(ipInNet))
					{
						floatingips.put(network, floatingIp.getPublicAddress());
						break;
					}
		return floatingips;
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

	public static Map<String,String> implementFloatingIps(Nffg operatorNffg, Map<String, List<String>> ipsOnNetwork, Map<String, String> floatingIps,
			String configurationService, String externalNetwork, FloatingIpPool floatingIpPool) throws VimDriverException {
		Map<String,String> randomFloatingIps = new HashMap<>();
		String routerMacControlPort = NffgManager.getMacControlPort(operatorNffg,NffgManager.getVnfsByDescription(operatorNffg, OPERATOR_ROUTER).get(0).getId());
		NatYang natYang = ConfigurationServiceProxy.getNatYang(configurationService, operatorNffg.getId(), operatorNffg.getId(), routerMacControlPort);
		for(String network: ipsOnNetwork.keySet())
		{
			String floatigIp = floatingIps.get(network);
			if(floatigIp!=null)
			{
				String privateIp = ipsOnNetwork.get(network).get(0);
				if(floatigIp.equals("random"))
				{
					floatigIp = generateRandomFloatingIp(natYang, externalNetwork, floatingIpPool);
					randomFloatingIps.put(network, floatigIp);
				}
				YangManager.addFloatingIp(natYang, privateIp, floatigIp);
			}
		}
		ConfigurationServiceProxy.sendNatYang(configurationService, natYang, operatorNffg.getId(), operatorNffg.getId(), routerMacControlPort);	
		return randomFloatingIps;
	}

	private static String generateRandomFloatingIp(NatYang natYang, String externalNetwork, FloatingIpPool floatingIpPool) throws VimDriverException {
		List<String> usedPublicIp = new ArrayList<>();
		for(IfEntry ifEntry: natYang.getConfigNatInterfaces().getIfEntry())
			if(ifEntry.getType().equals("wan"))
			{
				if(ifEntry.getAddress()==null)
					throw new VimDriverException("The Router haven't got an External Ip Address");
				usedPublicIp.add(ifEntry.getAddress());
				break;
			}
		for(FloatingIp floatingIp: natYang.getConfigNatStaticBindings().getFloatingIp())
			usedPublicIp.add(floatingIp.getPublicAddress());
		String generatedFloatingIp = null;
		String startAddress = floatingIpPool.getStart();
		String endAddress = floatingIpPool.getEnd();
		SubnetInfo extNetInfo = new SubnetUtils(externalNetwork).getInfo();
		if(!extNetInfo.isInRange(startAddress) || !extNetInfo.isInRange(endAddress))
			throw new VimDriverException("The floating Ip pool is not part of the external network");
		String candidateAddress = startAddress;
		while(generatedFloatingIp==null)
		{
			if(!usedPublicIp.contains(candidateAddress))
				generatedFloatingIp = candidateAddress;
			else
			{
				if(candidateAddress.equals(endAddress))
					throw new VimDriverException("There are not available floating Ips");
				candidateAddress=nextIpAddress(candidateAddress);
			}
		}
		return generatedFloatingIp;
	}

	public static void deleteFloatingIps(Nffg operatorNffg, Nffg nffg, Vnf vnfServer,
			Map<String, List<String>> networkIpAddressAssociation, String configurationService) throws VimDriverException {
		String routerMacControlPort = NffgManager.getMacControlPort(operatorNffg,NffgManager.getVnfsByDescription(operatorNffg, OPERATOR_ROUTER).get(0).getId());
		NatYang natYang = ConfigurationServiceProxy.getNatYang(configurationService, operatorNffg.getId(), operatorNffg.getId(), routerMacControlPort);
		List<FloatingIp> floatIps = natYang.getConfigNatStaticBindings().getFloatingIp();
		List<FloatingIp> toDelete = new ArrayList<>();
		for(FloatingIp floatIp: floatIps)
		{
			for(List<String> ips: networkIpAddressAssociation.values())
				if(ips.contains(floatIp.getPrivateAddress()))
				{
					toDelete.add(floatIp);
					break;
				}
		}
		for(FloatingIp floatIp: toDelete)
			floatIps.remove(floatIp);
		ConfigurationServiceProxy.sendNatYang(configurationService, natYang, operatorNffg.getId(), operatorNffg.getId(), routerMacControlPort);
	}

	public static void connectToManagementNetwork(Nffg managementNffg, Nffg tenantNffg, String vnfId) {
		String managementNetId = NffgManager.getVnfsByDescription(managementNffg,MANAGEMENT_SWITCH).get(0).getId();
		NffgManager.connectGraphToGraph(tenantNffg,vnfId,managementNffg,managementNetId);
	}

	public static void disconnectToManagementNetwork(Nffg managementNffg, Nffg tenantNffg, String id) {
		String managementNetId = NffgManager.getVnfsByDescription(managementNffg,MANAGEMENT_SWITCH).get(0).getId();
		NffgManager.disconnectGraphs(managementNffg, managementNetId, tenantNffg, id);
	}

	public static String getNetworkIdByName(Nffg nffg, String name) {
		String full_network_name = NETWORK_PREFIX + name;
		for(Vnf vnf: nffg.getVnfs()) {
			if (vnf.getName().equals(full_network_name))
				return vnf.getId();
		}
		return null;
	}
}
