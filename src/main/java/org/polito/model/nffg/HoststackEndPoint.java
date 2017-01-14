package org.polito.model.nffg;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HoststackEndPoint extends AbstractEP{
	private String configuration;
	@JsonProperty("ipv4")
	private String ip;

	public HoststackEndPoint()
	{
		type=Type.HOSTSTACK;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
