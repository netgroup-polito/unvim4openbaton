package org.polito.model.nffg;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Nffg {
	private String id;
	private String name;
	@JsonProperty("end-points")
	private List<EndpointWrapper> endpoints = new ArrayList<>();
	@JsonProperty("VNFs")
	private List<Vnf> vnfs = new ArrayList<>();
	@JsonProperty("big-switch")
	private BigSwitch bigSwitch;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<EndpointWrapper> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(List<EndpointWrapper> endpoints) {
		this.endpoints = endpoints;
	}

	public List<Vnf> getVnfs() {
		return vnfs;
	}

	public void setVnfs(List<Vnf> vnfs) {
		this.vnfs = vnfs;
	}

	public BigSwitch getBigSwitch() {
		return bigSwitch;
	}

	public void setBigSwitch(BigSwitch bigSwitch) {
		this.bigSwitch = bigSwitch;
	}

	public void addEndpoint(EndpointWrapper epw) {
		endpoints.add(epw);
	}

	public void addVnf(Vnf vnf) {
		vnfs.add(vnf);
	}

	public void addFlowRule(FlowRule fr) {
		bigSwitch.getFlowRules().add(fr);
	}

	@JsonIgnore
	public List<FlowRule> getFlowRules() {
		return bigSwitch.getFlowRules();
	}
}
