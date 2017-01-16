package org.polito.model.template;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VnfTemplate {
	@JsonIgnore
	private String id;
	private String name;
	private boolean expandable;
	@JsonProperty("uri-image")
	private String uri;
	@JsonProperty("uri-image-type")
	private String uriType;
	@JsonProperty("functional-capability")
	private String functionalCapability;
	@JsonProperty("vnf-type")
	private String vnfType;
	@JsonProperty("memory-size")
	private int  memorySize;
	@JsonProperty("root-file-system-size")
	private int rootFileSystemSize;
	@JsonProperty("ephemeral-file-system-size")
	private int  ephemeralFileSystemSize;
	@JsonProperty("swap-disk-size")
	private int swapDiskSize;
	@JsonProperty("CPUrequirements")
	private CpuRequirements cpuRequirements;
	private List<Port> ports;

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

	public boolean isExpandable() {
		return expandable;
	}

	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUriType() {
		return uriType;
	}

	public void setUriType(String uriType) {
		this.uriType = uriType;
	}

	public String getFunctionalCapability() {
		return functionalCapability;
	}

	public void setFunctionalCapability(String functionalCapability) {
		this.functionalCapability = functionalCapability;
	}

	public String getVnfType() {
		return vnfType;
	}

	public void setVnfType(String vnfType) {
		this.vnfType = vnfType;
	}

	public int getMemorySize() {
		return memorySize;
	}

	public void setMemorySize(int memorySize) {
		this.memorySize = memorySize;
	}

	public int getRootFileSystemSize() {
		return rootFileSystemSize;
	}

	public void setRootFileSystemSize(int rootFileSystemSize) {
		this.rootFileSystemSize = rootFileSystemSize;
	}

	public int getEphemeralFileSystemSize() {
		return ephemeralFileSystemSize;
	}

	public void setEphemeralFileSystemSize(int ephemeralFileSystemSize) {
		this.ephemeralFileSystemSize = ephemeralFileSystemSize;
	}

	public int getSwapDiskSize() {
		return swapDiskSize;
	}

	public void setSwapDiskSize(int swapDiskSize) {
		this.swapDiskSize = swapDiskSize;
	}

	public CpuRequirements getCpuRequirements() {
		return cpuRequirements;
	}

	public void setCpuRequirements(CpuRequirements cpuRequirements) {
		this.cpuRequirements = cpuRequirements;
	}

	public List<Port> getPorts() {
		return ports;
	}

	public void setPorts(List<Port> ports) {
		this.ports = ports;
	}

}
