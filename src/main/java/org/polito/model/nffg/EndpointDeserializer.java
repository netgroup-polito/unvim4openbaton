package org.polito.model.nffg;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class EndpointDeserializer extends StdDeserializer<EndpointWrapper>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EndpointDeserializer()
	{
		this(null);
	}
	
	public EndpointDeserializer(Class<EndpointWrapper> t)
	{
		super(t);
	}

	@Override
	public EndpointWrapper deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		EndpointWrapper ep = new EndpointWrapper();
		JsonNode node = p.getCodec().readTree(p);
		ep.setId(node.get("id").asText());
        ep.setName(node.get("name").asText());
        String type = node.get("type").asText();
        switch (type) {
		case "interface":
			InterfaceEndPoint iep = new InterfaceEndPoint();
			JsonNode internalNode = node.get("interface");
			iep.setIfName(internalNode.get("ifName").asText());
			ep.setEndpoint(iep);
			break;

		default:
			break;
		}
		return ep;
	}


}
