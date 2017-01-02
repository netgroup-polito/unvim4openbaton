package org.polito.model.nffg;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NffgWrapper {
	@JsonProperty("forwarding-graph")
	Nffg nffg;

	public NffgWrapper() {
	}

	public NffgWrapper(Nffg nffg) {
		this.nffg=nffg;
	}

	public Nffg getNffg() {
		return nffg;
	}

	public void setNffg(Nffg nffg) {
		this.nffg = nffg;
	}

}
