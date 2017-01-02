package org.polito.model.nffg;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class EndpointSerializer extends StdSerializer<EndpointWrapper>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EndpointSerializer()
	{
		this(null);
	}
	
	public EndpointSerializer(Class<EndpointWrapper> t)
	{
		super(t);
	}

	@Override
	public void serialize(EndpointWrapper value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("id", value.getId());
		gen.writeStringField("name", value.getName());
		if(value.getEndpoint().getClass()==InterfaceEndPoint.class)
		{
			gen.writeStringField("type", "interface");
			gen.writeObjectField("interface", (InterfaceEndPoint)value.getEndpoint());
		}
		gen.writeEndObject();
	}
}
