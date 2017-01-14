package org.polito.model.nffg;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Port {
	private String id;
	private String name;
	@JsonProperty("mac")
	private String macAddress;
	private boolean trusted;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public boolean isTrusted() {
		return trusted;
	}

	public void setTrusted(boolean trusted) {
		this.trusted = trusted;
	}

}
