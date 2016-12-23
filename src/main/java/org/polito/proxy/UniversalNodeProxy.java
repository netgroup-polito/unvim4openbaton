package org.polito.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.polito.model.message.UnConfiguration;
import org.polito.model.template.VnfTemplate;
import org.polito.model.template.VnfTemplateList;
import org.polito.model.template.VnfTemplateWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UniversalNodeProxy {
	
	public static List<VnfTemplate> getTemplates(String universalNodeEndpoint)
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
			e.printStackTrace();
			return null;
		}

		return DatastoreProxy.getTemplates(unConf.getDatastoreEndpoint());
	}
}
