package org.polito.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openbaton.exceptions.VimDriverException;
import org.polito.model.message.UnConfiguration;
import org.polito.model.template.VnfTemplate;
import org.polito.model.template.VnfTemplateList;
import org.polito.model.template.VnfTemplateWrapper;
import org.polito.unvim.UnClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

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
}
