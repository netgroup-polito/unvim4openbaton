package org.polito.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.polito.model.template.VnfTemplate;
import org.polito.model.template.VnfTemplateList;
import org.polito.model.template.VnfTemplateWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DatastoreProxy {
	
	public static List<VnfTemplate> getTemplates(String datastoreEndpoint)
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
			e.printStackTrace();
			return null;
		}
		List<VnfTemplate> templates = new ArrayList<>();
		for(VnfTemplateWrapper vnfTemplateWrapper: obj.getList())
		{
			vnfTemplateWrapper.getTemplate().setId(vnfTemplateWrapper.getId());
			templates.add(vnfTemplateWrapper.getTemplate());
		}
		return templates;
	}
}
