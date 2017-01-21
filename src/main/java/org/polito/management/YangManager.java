package org.polito.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.polito.model.yang.IfEntry;
import org.polito.model.yang.dhcp.Client;
import org.polito.model.yang.dhcp.DhcpYang;
import org.polito.model.yang.dhcp.GatewayIp;
import org.polito.model.yang.dhcp.GlobalIpPool;
import org.polito.model.yang.dhcp.Section;
import org.polito.model.yang.nat.NatYang;

public class YangManager {

	public static void setServerSection(DhcpYang yang, String sectionStartIp, String sectionStopIp) {

		Section section = new Section();
		section.setSectionStartIp(sectionStartIp);
		section.setSectionEndIp(sectionStopIp);
		yang.getConfigDhcpServerServer().getGlobalIpPool().getSections().getSection().add(section);

	}

	public static void setServerIpPoolParameters(DhcpYang yang, String defaultLeaseTime, String maxLeaseTime,
				String domainNameServer, String domainName) {
		GlobalIpPool globalIpPool = yang.getConfigDhcpServerServer().getGlobalIpPool();
		globalIpPool.setDefaultLeaseTime(defaultLeaseTime);
		globalIpPool.setMaxLeaseTime(maxLeaseTime);
		globalIpPool.setDomainNameServer(domainNameServer);
		globalIpPool.setDomainName(domainName);
	}

	public static void setServerDefaultGateway(DhcpYang yang, String defaultGateway, String netmask) {
		GatewayIp gatewayIp = yang.getConfigDhcpServerServer().getGlobalIpPool().getGatewayIp();
		gatewayIp.setGatewayIp(defaultGateway);
		gatewayIp.setGatewayMask(netmask);
	}

	public static String getServerDefaultGatewayIp(DhcpYang dhcpYang) {
		return dhcpYang.getConfigDhcpServerServer().getGlobalIpPool().getGatewayIp().getGatewayIp();
	}

	public static String getServerDefaultGatewayMask(DhcpYang dhcpYang) {
		return dhcpYang.getConfigDhcpServerServer().getGlobalIpPool().getGatewayIp().getGatewayMask();
	}

	public static void addInterface(DhcpYang yang, String name, String address, String configuration,
			String type, String defaultGw) {
		IfEntry iface = createIfEntry(name, address, configuration, type, defaultGw);
		yang.getConfigDhcpServerInterfaces().getIfEntry().add(iface);
	}

	private static IfEntry createIfEntry(String name, String address, String configuration, String type,
			String defaultGw) {
		IfEntry iface = new IfEntry();
		iface.setName(name);
		iface.setAddress(address);
		iface.setConfigurationType(configuration);
		iface.setType(type);
		iface.setDefaultGw(defaultGw);
		return iface;
	}

	public static void addInterface(NatYang yang, String name, String address, String configuration,
			String type, String defaultGw) {
		IfEntry iface = createIfEntry(name, address, configuration, type, defaultGw);
		yang.getConfigNatInterfaces().getIfEntry().add(iface);
	}

	public static Map<String, List<String>> readClientAddresses(DhcpYang dhcpYang) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for(Client client: dhcpYang.getConfigDhcpServerServer().getClients())
		{
			List<String> ip = new ArrayList<>();
			ip.add(client.getIpAddress());
			map.put(client.getMacAddress(), ip);
		}
		return map;
	}
}
