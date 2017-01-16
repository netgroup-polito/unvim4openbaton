package org.polito.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openbaton.exceptions.VimDriverException;
import org.polito.model.template.VnfTemplate;
import org.polito.model.template.VnfTemplateList;
import org.polito.model.template.VnfTemplateWrapper;
import org.polito.model.yang.dhcp.DhcpYang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatastoreProxy {
	private static Logger log = LoggerFactory.getLogger(DatastoreProxy.class);

	public static List<VnfTemplate> getTemplates(String datastoreEndpoint) throws VimDriverException
	{
		VnfTemplateList obj;
		try
		{
	        URL url = new URL(datastoreEndpoint + "/v2/nf_template/");
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("Accept", "application/json");
	        int responseCode = connection.getResponseCode();
			//TODO: Deal with responseCode different from 200
	
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			ObjectMapper mapper = new ObjectMapper();
			obj = mapper.readValue(response.toString(), VnfTemplateList.class);

		}
		catch(IOException e)
		{
			log.error(e.getMessage(), e);
			throw new VimDriverException(e.getMessage());
		}
		List<VnfTemplate> templates = new ArrayList<>();
		for(VnfTemplateWrapper vnfTemplateWrapper: obj.getList())
		{
			vnfTemplateWrapper.getTemplate().setId(vnfTemplateWrapper.getId());
			templates.add(vnfTemplateWrapper.getTemplate());
		}
		return templates;
	}

	public static void sendDhcpYang(String datastoreEndpoint, DhcpYang yang, String tenant, String graphId, String vnfId) throws VimDriverException
	{
		try
		{
	        URL url = new URL(datastoreEndpoint + "/config/vnf/" + vnfId + "/" + graphId + "/" + tenant);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
	        connection.setRequestMethod("PUT");
	        connection.setRequestProperty("Content-Type", "application/json");

	        ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			String jsonInString = mapper.writeValueAsString(yang);
			System.out.println(jsonInString);

			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(jsonInString);
			out.close();

	        int responseCode = connection.getResponseCode();
			//TODO: Deal with responseCode different from 200

		}
		catch(IOException e)
		{
			log.error(e.getMessage(), e);
			throw new VimDriverException(e.getMessage());
		}
		return;
	}
}
