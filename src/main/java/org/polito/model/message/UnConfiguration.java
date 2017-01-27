package org.polito.model.message;

import java.util.List;

public class UnConfiguration {
	private String datastoreEndpoint;
	private String configurationServiceEndpoint;
	private List<String> unPhisicalPorts;

	public String getDatastoreEndpoint() {
		return datastoreEndpoint;
	}

	public void setDatastoreEndpoint(String datastoreEndpoint) {
		this.datastoreEndpoint = datastoreEndpoint;
	}

	public List<String> getUnPhisicalPorts() {
		return unPhisicalPorts;
	}

	public void setUnPhisicalPorts(List<String> unPhisicalPorts) {
		this.unPhisicalPorts = unPhisicalPorts;
	}

	public String getConfigurationServiceEndpoint() {
		return configurationServiceEndpoint;
	}

	public void setConfigurationServiceEndpoint(String configurationServiceEndpoint) {
		this.configurationServiceEndpoint = configurationServiceEndpoint;
	}

}
