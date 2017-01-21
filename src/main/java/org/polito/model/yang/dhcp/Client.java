package org.polito.model.yang.dhcp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Client {
	@JsonProperty("ip")
	private String ipAddress;
	@JsonProperty("mac_address")
	private String macAddress;

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

}
