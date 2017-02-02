package org.polito.model.template;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VnfTemplateWrapper {
	private String id;
	private VnfTemplate template;
	@JsonProperty("image-upload-status")
	private String imageUploadStatus;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public VnfTemplate getTemplate() {
		return template;
	}

	public void setTemplate(VnfTemplate template) {
		this.template = template;
	}

	public String getImageUploadStatus() {
		return imageUploadStatus;
	}

	public void setImageUploadStatus(String imageUploadStatus) {
		this.imageUploadStatus = imageUploadStatus;
	}

}
