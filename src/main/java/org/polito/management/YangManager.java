package org.polito.management;

import org.apache.commons.net.util.SubnetUtils;
import org.polito.model.yang.dhcp.DhcpYang;
import org.polito.model.yang.dhcp.GatewayIp;
import org.polito.model.yang.dhcp.GlobalIpPool;
import org.polito.model.yang.dhcp.IfEntry;
import org.polito.model.yang.dhcp.Section;

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

	public static void addInterface(DhcpYang yang, String name, String address, String configuration,
			String type, String defaultGw) {
		IfEntry iface = new IfEntry();
		iface.setName(name);
		iface.setAddress(address);
		iface.setConfigurationType(configuration);
		iface.setType(type);
		iface.setDefaultGw(defaultGw);
		yang.getConfigDhcpServerInterfaces().getIfEntry().add(iface);
	}
}
