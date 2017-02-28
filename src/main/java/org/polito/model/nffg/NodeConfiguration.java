package org.polito.model.nffg;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeConfiguration {
	String id;
	String type;
	@JsonProperty("default-gateway")
	DefaultGatewayConfig defaultGateway;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public DefaultGatewayConfig getDefaultGateway() {
		return defaultGateway;
	}

	public void setDefaultGateway(DefaultGatewayConfig defaultGateway) {
		this.defaultGateway = defaultGateway;
	}

}
