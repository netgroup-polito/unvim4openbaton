package org.polito.model.nffg;

import java.util.AbstractMap.SimpleEntry;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Action {
	@JsonProperty("output_to_port")
	private String output;

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

}
