package org.polito.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.openbaton.exceptions.VimDriverException;
import org.polito.model.message.UnConfiguration;
import org.polito.model.nffg.Nffg;
import org.polito.model.nffg.NffgWrapper;
import org.polito.model.template.VnfTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class UniversalNodeProxy {
	private static Logger log = LoggerFactory.getLogger(UniversalNodeProxy.class);

	public static List<VnfTemplate> getTemplates(String universalNodeEndpoint) throws VimDriverException
	{
		UnConfiguration unConf;
		try
		{
	        URL url = new URL(universalNodeEndpoint + "/conf");
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
			unConf = mapper.readValue(response.toString(), UnConfiguration.class);
		}
		catch(IOException e)
		{
			log.error(e.getMessage(), e);
			throw new VimDriverException(e.getMessage());
		}

		return DatastoreProxy.getTemplates(unConf.getDatastoreEndpoint());
	}

	public static Nffg getNFFG(String universalNodeEndpoint, String NffgId) throws VimDriverException
	{
		Nffg nffg;
		try
		{
	        URL url = new URL(universalNodeEndpoint + "/NF-FG/" + NffgId);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("Accept", "application/json");
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

	public static void sendNFFG(String universalNodeEndpoint, Nffg nffg) throws VimDriverException
	{
		try
		{
	        URL url = new URL(universalNodeEndpoint + "/NF-FG/" + nffg.getId());
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
	        connection.setRequestMethod("PUT");
	        connection.setRequestProperty("Content-Type", "application/json");

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
}
