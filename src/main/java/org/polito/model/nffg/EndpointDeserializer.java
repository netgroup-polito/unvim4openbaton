package org.polito.model.nffg;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

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
			iep.setIfName(internalNode.get("if-name").asText());
			ep.setEndpoint(iep);
			break;
		case "host-stack":
			HoststackEndPoint hep = new HoststackEndPoint();
			JsonNode internalHsNode = node.get("host-stack");
			hep.setConfiguration(internalHsNode.get("configuration").asText());
			if(hep.getConfiguration().equals("static"))
				hep.setIp(internalHsNode.get("ipv4").asText());
			ep.setEndpoint(hep);
			break;
		default:
			break;
		}
		return ep;
	}


}
