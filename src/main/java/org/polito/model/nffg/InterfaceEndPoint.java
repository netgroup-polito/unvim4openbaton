package org.polito.model.nffg;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InterfaceEndPoint extends AbstractEP{
	@JsonProperty("if-name")
	private String ifName;

	public String getIfName()
	{
		return ifName;
	}

	public void setIfName(String ifName)
	{
		this.ifName = ifName;
	}

}
