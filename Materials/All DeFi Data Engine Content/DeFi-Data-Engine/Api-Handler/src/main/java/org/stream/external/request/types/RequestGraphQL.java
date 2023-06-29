package org.stream.external.request.types;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;

import org.application.apihandler.ApiHandlerApplication;
import org.json.JSONArray;
import org.json.JSONObject;

public class RequestGraphQL extends RequestFramework {

	public RequestGraphQL() {
		super();
	}
	
	public RequestGraphQL(String name, String url, String[] url_path, HashMap<String, String> properties, HashMap<String, String> headers,
			HashMap<String, String> tags, String[] recursive_location, String recursive_replacement, String[] path,
			boolean is_dated, String date_location, String date_start_var, String date_end_var, String date_format) {
		super(name, url, url_path, properties, headers, tags, recursive_location, recursive_replacement, path,
				is_dated, date_location, date_start_var, date_end_var, date_format);
	}

	@Override
	public String getType() {
		return "graphql";
	}
	
	@Override 
	protected String processRequest(String url, HashMap<String, String> properties, HashMap<String, String> headers) {
		// validate properties and url and not empty
		if(url.isEmpty() || properties.isEmpty())
			return "Key parameter is empty";
		
		// check for required tag -l
		if(!hasTag("-l")) {
			System.err.println(String.format("Missing required recursive parameter <-l>"));
			return "Missing required recursive parameter <-l>";
		}
		
		// check for -l being an integer
		try {
			Integer.parseInt(getTag("-l"));
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println(String.format("Value following <-l> must be an integer."));
			return "Value following <-l> must be an integer.";
		}
		
		// validate all required properties exist
		String[] req_properties = {"values", "method"};
		for(String key : req_properties) {
			if(!properties.containsKey(key))
				return String.format("Required property <%s> not found.", key);
		}
		
		// build query from properties:
		
		// generate values
		String[] values_arr = properties.get("values").split(":");
		StringBuilder values = new StringBuilder();
		for(int i = 0; i < values_arr.length; i++) {
			values.append(values_arr[i]);
			if(i != values_arr.length - 1)
				values.append(",");
		}
		
		// generate timestamp/recursive values
		String recursive_location = getRecursiveLocation()[0];
		StringBuilder where = new StringBuilder();
		if(properties.containsKey("gt")) {
			where.append(String.format("{%s_gt:%s", recursive_location, properties.get("gt")));
			if(properties.containsKey("lt"))
				where.append(String.format(" %s_lt:%s", recursive_location, properties.get("lt")));
		}
		
		// if no gt or lt detected, check if dated
		else {
			// if dated
			if(properties.containsKey(date_start_var) && properties.containsKey(date_end_var)) {
				// define timestamp definition based on recursive parameter for gt and lt
				LocalDate start_date = LocalDate.parse(properties.get(date_start_var), formatter);
				LocalDate end_date = LocalDate.parse(properties.get(date_end_var), formatter);
				long start_epoch = start_date.toEpochDay() * 86400L;
				long end_epoch = end_date.toEpochDay() * 86400L;
				
				// append
				where.append(String.format("{%s_gt:%s %s_lt:%s",
						recursive_location, start_epoch,
						recursive_location, end_epoch));
				
				// push gt and lt properties
				properties.put("gt", "" + start_epoch);
				properties.put("lt", "" + end_epoch);
			} 
			
			// if not dated then apply basic lt
			else {
				where.append(String.format("{%s_gt:0", recursive_location));
				properties.put("gt", "0");
			}
		}
		
		// close recursion
		where.append("}");
		
		// append
		String query = String.format("query {"
				+ "%s(first:%s orderBy:%s where:%s){%s}}",
				properties.get("method"),
				getTag("-l"),
				recursive_location,
				where,
				values);
	
		System.out.println(query);
		// make request to server:
		
		// create client
		HttpClient client = HttpClient.newHttpClient();
		
		// create http request with POST method
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(getUrl()))
				.POST(HttpRequest.BodyPublishers.ofString(new JSONObject().put("query", query).toString()));
		
		// add all headers
		for(String key : headers.keySet()) {
			builder = builder.header(key, headers.get(key));
		}
		
		// generate request
		HttpRequest request = builder.build();
		
		// submit request
		HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch(Exception e) {
			e.printStackTrace();
			return "Invalid client request to server";
		}
			
		if(response.statusCode() != 200) {
			return "GraphQL request failed with status code: " + response.statusCode();
		}
		
		// extract json body
		String body = response.body();
		JSONObject json = new JSONObject(body);
		if(json.has("errors")) {
			return json.toString();
		}
		
		// process in handler
		return handle(body, properties, headers);
	}
	
	@Override
	protected String handle(String json, HashMap<String, String> properties, HashMap<String, String> headers) {
		// parse json formatting
		JSONObject obj = new JSONObject(json);
		
		// validate all required parameters are present:
		// check for required tag -l
		if(!hasTag("-l")) {
			System.err.println(String.format("Missing required recursive parameter <-l>"));
			return "Missing required recursive parameter <-l>";
		}
		
		// check for -l being an integer
		int limit;
		try {
			limit = Integer.parseInt(getTag("-l"));
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println(String.format("Value following <-l> must be an integer."));
			return "Value following <-l> must be an integer.";
		}
		
		// validate that the base has the proper data path
		String[] path = getPath();
		JSONArray data = null;
		for(int i = 0; i < path.length; i++) {
			if(i == path.length - 1) {
				if(obj.has(path[i])) {
					try {
						data = obj.getJSONArray(path[i]);
					} catch(Exception e) {
						System.err.println("obj path type is not of type <JSONArray>. Cannot parse");
						return "obj path type is not of type <JSONArray>. Cannot parse";
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
			
			else {
				return "Data path is invalid. Please revise configuration.";
			}
		}
		
		// validate that data is non-empty
		if(data == null) {
			System.err.println("Data array retrieval had fatal error, killing process.");
			return "Data array retrieval had fatal error, killing process.";
		}
		
		// define recursive location
		String recursive_location = getRecursiveLocation()[0];
		
		// extract and print data
		// validate recursive parameter with first data point and store value from last
		// note that if dated then use date restrictive query
		for(int i = 0; i < data.length(); i++) {
			// retrieve values
			HashMap<String, String> point = parse(data.getJSONObject(i));
			 
			// if i == 0 then parse recursive parameter
			if(i == 0 && !point.containsKey(recursive_location)) { 
				System.err.println("Point does not contain recursive location.");
				return "Point does not contain recursive location.";
			}
			
			// push data
			ApiHandlerApplication.output(this.getName(), point);
			
			// if last data point then retrieve recursive parameter
			if(i == data.length() - 1) {
				if(!point.containsKey(recursive_location)) {
					System.err.println("Final point does not contain recursive location. Data collection may not be complete.");
					return "Final point does not contain recursive location. Data collection may not be complete.";
				}
				
				// update gt property with new value
				properties.put("gt", String.format("\"%s\"", point.get(recursive_location)));
			}
		}
		
		// if data is less than provided limit then return
		if(data.length() < limit)
			return null;
		
		return process(getUrl(), properties, headers);
	}
}
