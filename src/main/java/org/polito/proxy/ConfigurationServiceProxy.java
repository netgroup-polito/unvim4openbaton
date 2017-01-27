package org.polito.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.openbaton.exceptions.VimDriverException;
import org.polito.model.yang.dhcp.DhcpYang;
import org.polito.model.yang.nat.NatYang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationServiceProxy {
	private static Logger log = LoggerFactory.getLogger(ConfigurationServiceProxy.class);

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

	public static void sendNatYang(String datastoreEndpoint, NatYang yang, String tenant, String graphId, String vnfId) throws VimDriverException
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

	public static DhcpYang getDhcpYang(String datastoreEndpoint, String tenant, String graphId, String vnfId) throws VimDriverException
	{
		DhcpYang dhcpYang;
		try
		{
	        URL url = new URL(datastoreEndpoint + "/config/status/" + vnfId + "/" + graphId + "/" + tenant);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("Accept", "application/json");
	        int responseCode = connection.getResponseCode();
	        if(responseCode==404)
	        	return null;
			//TODO: Deal with responseCode different from 200

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			ObjectMapper mapper = new ObjectMapper();
			dhcpYang = mapper.readValue(response.toString(), DhcpYang.class);

		}
		catch(IOException e)
		{
			log.error(e.getMessage(), e);
			throw new VimDriverException(e.getMessage());
		}
		return dhcpYang;
	}
}
