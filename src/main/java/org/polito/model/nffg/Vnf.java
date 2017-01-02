package org.polito.model.nffg;

import java.util.ArrayList;
import java.util.List;

public class Vnf {
	private String id;
	private String name;
	private List<Port> ports  = new ArrayList<>();

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

}
