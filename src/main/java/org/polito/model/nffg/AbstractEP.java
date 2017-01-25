package org.polito.model.nffg;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AbstractEP {
	public enum Type{
		UNDEFINED,
		INTERFACE,
		HOSTSTACK,
		INTERNAL
	}
	@JsonIgnore
	protected Type type= Type.UNDEFINED;

	public Type getType() {
		return type;
	}

}
