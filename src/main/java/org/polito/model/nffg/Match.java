package org.polito.model.nffg;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Match {
	@JsonProperty("port_in")
	private String input;

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

}
