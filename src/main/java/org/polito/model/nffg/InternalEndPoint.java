package org.polito.model.nffg;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InternalEndPoint extends AbstractEP{
	@JsonProperty("internal-group")
	private String internalGroup;

	public InternalEndPoint()
	{
		type=Type.INTERNAL;
	}

	public String getInternalGroup()
	{
		return internalGroup;
	}

	public void setInternalGroup(String internalGroup)
	{
		this.internalGroup = internalGroup;
	}

}
