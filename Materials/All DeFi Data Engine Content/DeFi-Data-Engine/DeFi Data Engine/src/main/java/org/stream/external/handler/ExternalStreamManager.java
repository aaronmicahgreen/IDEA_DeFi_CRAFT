package org.stream.external.handler;

import java.util.HashMap;
import java.util.TreeMap;

import org.stream.external.requests.ExternalRequestFramework;
import org.stream.external.requests.ExternalRequestManager;

public class ExternalStreamManager {

	private final ExternalStreamHandler handler;
	private final ExternalRequestManager manager;
	
	protected ExternalStreamManager(ExternalStreamHandler handler) {
		this.handler = handler;
		this.manager = new ExternalRequestManager();
		
		// initialize all available connections
		try {
			this.manager.initialize(this);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public boolean containsType(String type) {
		return manager.hasRequestFormat(type);
	}
	
	protected Object[] request(String type, HashMap<String, String> data) {
		return request(type, data, null, null);
	}
	
	protected Object[] request(String type, HashMap<String, String> data, String startDate, String endDate) {
		// validate that type exists
		if(!manager.hasRequestFormat(type))
			return new Object[]{false, String.format("Missing request type <%s>", type)};
		
		// parse data into required formatting
		// retrieve url path
		String[] url_path;
		if(!data.containsKey("url_path")) {
			url_path = new String[] {};
		} else {
			url_path = data.get("url_path").split(",");
		}
		
		// retrieve properties
		HashMap<String, String> properties = new HashMap<String, String>();
		TreeMap<String, String> user_properties = new TreeMap<String, String>();
		if(data.containsKey("properties")) {
			String[] raw_properties = data.get("properties").split(",");
			if(raw_properties.length % 2 != 0)
				return new Object[] {false, String.format("Properties must be in <key, value> pairs.")};
				
			for(int i = 0; i < raw_properties.length; i+=2) {
				properties.put(raw_properties[i], raw_properties[i + 1]);
				user_properties.put(raw_properties[i], raw_properties[i + 1]);
			}
		}
		
		// retrieve headers
		HashMap<String, String> headers = new HashMap<String, String>();
		if(data.containsKey("headers")) {
			String[] raw_headers = data.get("headers").split(",");
			if(raw_headers.length % 2 != 0)
				return new Object[] {false, String.format("Headers must be in <key, value> pairs.")};
			
			for(int i = 0; i < raw_headers.length; i+=2)
				headers.put(raw_headers[i], raw_headers[i + 1]);
		}
		
		// retrieve request framework
		ExternalRequestFramework request = manager.getRequestFormat(type);
		
		// submit response
		String response;
		if(startDate == null || endDate == null)
			response = request.request(url_path, properties, headers, user_properties);
		else
			response = request.request(url_path, properties, headers, startDate, endDate, user_properties);
		
		if(response != null)
			return new Object[] {false, response};
		
		return new Object[] {true, ""};
	}
	
	public void processRequest(String collection, HashMap<String, String> data) {
		// invalid hashmap
		if(data == null)
			return;
		
		// if empty then submit blank push
		if(data.isEmpty()) {
			handler.send("LSH", "PUSH", "collection", collection, "data", "<<<empty>>>");
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		for(String key : data.keySet())
			sb.append(key.replaceAll(",", ".") + "," + data.get(key).replaceAll(",", ".") + ",");
		sb.delete(sb.length() - 1, sb.length());
		handler.send("LSH", "PUSH", "collection", collection, "data", sb.toString());
	}
}
