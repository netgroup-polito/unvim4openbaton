package org.polito.model.template;

import java.util.List;

public class CpuRequirements {
	private String  platformType;
	private List<Socket> socket;

	public String getPlatformType() {
		return platformType;
	}
	public void setPlatformType(String platformType) {
		this.platformType = platformType;
	}
	public List<Socket> getSocket() {
		return socket;
	}
	public void setSocket(List<Socket> socket) {
		this.socket = socket;
	}

}
