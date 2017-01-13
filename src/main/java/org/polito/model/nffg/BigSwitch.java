package org.polito.model.nffg;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BigSwitch {
	@JsonProperty("flow-rules")
	private List<FlowRule> flowRules = new ArrayList<>();

	public List<FlowRule> getFlowRules()
	{
		return flowRules;
	}

	public void setFlowRules(List<FlowRule> flowRules)
	{
		this.flowRules = flowRules;
	}

}
