package org.polito.model.nffg;

import java.util.ArrayList;
import java.util.List;

import org.polito.model.template.VnfTemplate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Vnf implements IdAware{
	private String id;
	private String name;
	private List<Port> ports  = new ArrayList<>();
	@JsonProperty("vnf_template")
	private String template;
	@JsonProperty("functional_capability")
	private String functionalCapability;
	private String description;
	@JsonProperty("user_data")
	private String userData;
	@JsonIgnore
	private VnfTemplate templateObject;

	@Override
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

	public String getFunctionalCapability() {
		return functionalCapability;
	}

	public void setFunctionalCapability(String functionalCapability) {
		this.functionalCapability = functionalCapability;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public void setTemplateObject(VnfTemplate vnfTemplate) {
		templateObject = vnfTemplate;
	}

	public VnfTemplate getTemplateObject() {
		return templateObject;
	}
}
