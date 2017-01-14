package org.polito.model.nffg;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonSerialize(using = EndpointSerializer.class)
@JsonDeserialize(using = EndpointDeserializer.class)
public class EndpointWrapper implements IdAware{
	private String id;
	private String name;
	private AbstractEP endpoint;

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

	public AbstractEP getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(AbstractEP endpoint) {
		this.endpoint = endpoint;
	}

}
