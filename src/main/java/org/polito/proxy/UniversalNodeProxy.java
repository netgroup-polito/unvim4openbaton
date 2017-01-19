package org.polito.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.VimDriverException;
import org.polito.model.message.Authentication;
import org.polito.model.message.UnConfiguration;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.NffgWrapper;
import org.polito.model.template.VnfTemplate;
import org.polito.model.yang.dhcp.DhcpYang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UniversalNodeProxy {
	private static Logger log = LoggerFactory.getLogger(UniversalNodeProxy.class);

	public static List<VnfTemplate> getTemplates(VimInstance unInstance) throws VimDriverException
	{
		String datastoreEndpoint = getDatastoreEndpoint(unInstance);
		return DatastoreProxy.getTemplates(datastoreEndpoint);
	}

	public static String getDatastoreEndpoint(VimInstance unInstance) throws VimDriverException
	{
		UnConfiguration unConf;
		try
		{
			String token = Authenticate(unInstance);
	        URL url = new URL(unInstance.getAuthUrl() + "/conf");
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("Accept", "application/json");
			if(token!=null)
				connection.setRequestProperty("X-Auth-Token", token);
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
			unConf = mapper.readValue(response.toString(), UnConfiguration.class);
		}
		catch(IOException e)
		{
			log.error(e.getMessage(), e);
			throw new VimDriverException(e.getMessage());
		}
		return unConf.getDatastoreEndpoint();
	}

	public static Nffg getNFFG(VimInstance unInstance, String NffgId) throws VimDriverException
	{
		Nffg nffg;
		try
		{
			String token = Authenticate(unInstance);
	        URL url = new URL(unInstance.getAuthUrl() + "/NF-FG/" + NffgId);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("Accept", "application/json");
	        if(token!=null)
	        	connection.setRequestProperty("X-Auth-Token", token);
	        int responseCode = connection.getResponseCode();
			//TODO: Deal with responseCode different from 200 & 404
	        if(responseCode==404)
		        return null;

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			ObjectMapper mapper = new ObjectMapper();

			nffg = mapper.readValue(response.toString(),NffgWrapper.class).getNffg();
		}
		catch(IOException e)
		{
			log.error(e.getMessage(), e);
			throw new VimDriverException(e.getMessage());
		}
		return nffg;
	}

	public static void sendNFFG(VimInstance unInstance, Nffg nffg) throws VimDriverException
	{
		try
		{
			String token = Authenticate(unInstance);
	        URL url = new URL(unInstance.getAuthUrl() + "/NF-FG/" + nffg.getId());
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
	        connection.setRequestMethod("PUT");
	        connection.setRequestProperty("Content-Type", "application/json");
	        if(token!=null)
	        	connection.setRequestProperty("X-Auth-Token", token);

	        ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			String jsonInString = mapper.writeValueAsString(new NffgWrapper(nffg));
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

	private static String Authenticate(VimInstance unInstance) throws IOException, VimDriverException {

		URL url = new URL(unInstance.getAuthUrl() + "/login");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");

		Authentication auth = new Authentication();
		auth.setUsername(unInstance.getUsername());
		auth.setPassword(unInstance.getPassword());

		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(auth);
		System.out.println(jsonInString);

		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
		out.write(jsonInString);
		out.close();

		int responseCode = connection.getResponseCode();

		if(responseCode==401) // Anauthorized
			throw new VimDriverException("Bad credentials");
		if(responseCode==501) // Not implemented - Authentication is not required
			return null;
		if(responseCode!=200)
			throw new VimDriverException("Unknown error. Response code: " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	public static void sendDhcpYang(VimInstance unInstance, DhcpYang yang, String tenant, String graphId, String vnfId) throws VimDriverException
	{
		String datastoreEndpoint = getDatastoreEndpoint(unInstance);
		DatastoreProxy.sendDhcpYang(datastoreEndpoint, yang, tenant, graphId, vnfId);
	}
}
