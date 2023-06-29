package org.stream.external.request.types;

import java.util.HashMap;

import org.application.apihandler.ApiHandlerApplication;
import org.json.JSONArray;
import org.json.JSONObject;

public class RequestParameterized extends RequestFramework {

	public RequestParameterized() {
		super();
	}
	
	public RequestParameterized(String name, String url, String[] url_path, HashMap<String, String> properties, HashMap<String, String> headers,
			HashMap<String, String> tags, String[] recursive_location, String recursive_replacement, String[] path,
			boolean is_dated, String date_location, String date_start_var, String date_end_var, String date_format) {
		super(name, url, url_path, properties, headers, tags, recursive_location, recursive_replacement, path,
				is_dated, date_location, date_start_var, date_end_var, date_format);
	}
	
	@Override
	public String getType() {
		return "parameterized"; 
	}
	
	public String handle(String json, HashMap<String, String> properties, HashMap<String, String> headers) {
		// parse json formatting
		JSONObject obj = new JSONObject(json);
		
		// validate all required parameters are present:
		// check for required tag -l
		if(!hasTag("-l")) {
			System.err.println(String.format("Missing required recursive parameter <-l>"));
			return "Missing required recursive parameter <-l>";
		}
		
		// check for required tag -t
		if(!hasTag("-t")) {
			System.err.println(String.format("Missing required recursive parameter <-t>"));
			return "Missing required recursive parameter <-t>";
		}
		
		// check for -l being an integer
		int limit = 0;
		try {
			limit = Integer.parseInt(getTag("-l"));
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println(String.format("Value following <-l> must be an integer."));
			return "Value following <-l> must be an integer.";
		}
		
		// validate that recursion location is valid
		// should exist within json if url or static
		// should exist within properties if type incremental
		String recursive_parameter = null;
		if(getTag("-t").equals("url") || getTag("-t").equals("static")) {
			JSONObject base = obj;
			String[] location = getRecursiveLocation();
			for(int i = 0; i < location.length - 1; i++) {
				if(base.has(location[i])) {
					base = base.getJSONObject(location[i]);
				} else {
					System.err.println("Response does not contain proper recursive parameter location at: " + location[i]);
					return "Response does not contain proper recursive parameter location at: " + location[i];
				}
			}
			
			recursive_parameter = base.get(location[location.length - 1]).toString();
		} 
		 
		else if(getTag("-t").equals("incremental")) {
			if(!properties.containsKey(getRecursiveLocation()[0])) {
				System.err.println("Properties do not contain incremental parameter listed in: " + getRecursiveLocation()[0]);
				return "Properties do not contain incremental parameter listed in: " + getRecursiveLocation()[0];
			}
			
			recursive_parameter = properties.get(getRecursiveLocation()[0]);
		}
		
		// validate that recursive parameter has been set
		if(recursive_parameter == null) {
			System.err.println("Fatal error. Recursive parameter is null after successful initialization.");
			return "Fatal error. Recursive parameter is null after successful initialization.";
		}
		
		
		// validate that the base has the proper obj path
		String[] path = getPath();
		JSONArray data = null;
		for(int i = 0; i < path.length; i++) {
			if(i == path.length - 1) {
				if(obj.has(path[i])) {
					try {
						data = obj.getJSONArray(path[i]);
					} catch(Exception e) {
						System.err.println("obj path type is not of type <JSONArray>. Cannot parse.");
						return "obj path type is not of type <JSONArray>. Cannot parse.";
					}
				}
			}
			
			else if(obj.has(path[i])) {
				try {
					obj = obj.getJSONObject(path[i]);
				} catch(Exception e) {
					System.err.println("obj path type step is not of type <JSONObject>. Cannot parse.");
					return "obj path type step is not of type <JSONObject>. Cannot parse.";
				}
			}
		}
		
		// validate that data is non-empty
		if(data == null) {
			System.err.println("Data array retrieval had fatal error, killing process.");
			return "Data array retrieval had fatal error, killing process.";
		}
		
		// extract and print data
		for(int i = 0; i < data.length(); i++) {
			HashMap<String, String> point = parse(data.getJSONObject(i));
			ApiHandlerApplication.output(this.getName(), point);
		}
		
		// initiate recursive call
		// if under limit requested, terminate call
		if(data.length() < limit)
			return null;
		
		// extract recursive parameter and apply to next call
		String rec_type = getTag("-t");
		String url = getUrl();
		switch(rec_type) {
		
		// type url:
		// - clear properties and set base to extracted url
		// - execute
		case "url":
			properties.clear();
			url = recursive_parameter;
			return processUrl(url, headers);
			
		// type incremental:
		// - assert that parameter is an integer
		// - increment parameter and update
		// - execute
		case "incremental":
			int param = -1;
			try {
				param = Integer.parseInt(recursive_parameter);
			} catch(Exception e) {
				e.printStackTrace();
				System.err.println(String.format("Recursive parameter <%s> is not of type integer.", recursive_parameter));
				return String.format("Recursive parameter <%s> is not of type integer.", recursive_parameter);
			}
			
			if(param == -1) {
				System.err.println("Fatal parsing error occured.");
				return "Fatal parsing error occured.";
			}
			
			param += 1;
			properties.put(getRecursiveLocation()[0], "" + param);
			break;
			
		// type static:
		// - check to see if replacement location exists
		// - if so update replacement
		// - execute
		case "static":
			if(getRecursiveReplacement() != null) {
				properties.put(getRecursiveReplacement(), recursive_parameter);
			} else {
				properties.put(getRecursiveLocation()[0], recursive_parameter);
			}
			break;
		}
		
		// increment param, replace, and execute
		return process(url, properties, headers);
	}
}