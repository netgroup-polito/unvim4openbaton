package org.polito.model.template;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Port {
	private String position;
	private String label;
	private int min;
	@JsonProperty("ipv4-config")
	private String ipv4Config;
	@JsonProperty("ipv6-config")
	private String ipv6Config;
	private String name;

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public String getIpv4Config() {
		return ipv4Config;
	}

	public void setIpv4Config(String ipv4Config) {
		this.ipv4Config = ipv4Config;
	}

	public String getIpv6Config() {
		return ipv6Config;
	}

	public void setIpv6Config(String ipv6Config) {
		this.ipv6Config = ipv6Config;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
