package org.polito.model.nffg;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Vnf {
	private String id;
	private String name;
	private List<Port> ports  = new ArrayList<>();
	@JsonProperty("vnf_template")
	private String template;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<Port> getPorts()
	{
		return ports;
	}

	public void setPorts(List<Port> ports)
	{
		this.ports = ports;
	}

	public void addPort(Port p) {
		ports.add(p);
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

}
