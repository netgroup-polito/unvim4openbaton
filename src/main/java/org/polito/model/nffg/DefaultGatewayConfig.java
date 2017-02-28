package org.polito.model.nffg;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DefaultGatewayConfig {
	@JsonProperty("ip-address")
	String ipAddress;

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
}
