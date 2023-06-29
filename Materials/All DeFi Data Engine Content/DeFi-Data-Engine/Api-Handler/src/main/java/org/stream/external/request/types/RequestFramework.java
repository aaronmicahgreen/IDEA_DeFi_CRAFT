package org.stream.external.request.types;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

public abstract class RequestFramework {
	
	public final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	private final String name;
	private String url;
	private final String[] url_path;
	private final HashMap<String, String> properties;
	private final HashMap<String, String> headers;
	private final HashMap<String, String> tags;
	private final String[] recursive_location;
	private final String recursive_replacement;
	private final String[] path;
	private final boolean is_dated;
	private final String date_location;
	protected final String date_start_var;
	protected final String date_end_var;
	protected final DateTimeFormatter date_format;

	// default constructor used for templating
	public RequestFramework() {
		this.name = null;
		this.url = null;
		this.url_path = null;
		this.properties = null;
		this.headers = null;
		this.tags = null;
		this.recursive_location = null;
		this.recursive_replacement = null;
		this.path = null;
		this.is_dated = false;
		this.date_location = null;
		this.date_start_var = null;
		this.date_end_var = null;
		this.date_format = null;
	}
	
	public RequestFramework(String name, String url, String[] url_path, HashMap<String, String> properties, HashMap<String, String> headers, 
							HashMap<String, String> tags, String[] recursive_location, String recursive_replacement, String[] path,
							boolean is_dated, String date_location, String date_start_var, String date_end_var, String date_format) {
		this.name = name;
		this.url = url;
		this.url_path = url_path;
		this.properties = properties;
		this.headers = headers;
		this.tags = tags;
		this.recursive_location = recursive_location;
		this.recursive_replacement = recursive_replacement;
		this.path = path;
		this.is_dated = is_dated;
		this.date_location = date_location;
		this.date_start_var = date_start_var;
		this.date_end_var = date_end_var;
		this.date_format = DateTimeFormatter.ofPattern(date_format);
	}

	public final boolean hasTag(String tag) {
		return this.tags.containsKey(tag);
	}
	
	public final String getTag(String tag) {
		return this.tags.get(tag);
	}
	
	public final HashMap<String, String> getTags() {
		return this.tags;
	}
	
	public final String getName() {
		return name;
	}
	
	public final String getUrl() {
		return url;
	}
	
	public final String[] getRecursiveLocation() {
		return recursive_location;
	}
	
	public final String getRecursiveReplacement() {
		return recursive_replacement;
	}
	
	public final String[] getPath() {
		return path;
	}
	
	protected final Request getRequest(HashMap<String, String> properties, HashMap<String, String> headers) {
		return this.getRequest(this.url, properties, headers);
	}
	
	protected final Request getRequest(String url, HashMap<String, String> properties, HashMap<String, String> headers) {
		// define builder
		Builder builder = new Builder();
		StringBuilder url_builder = new StringBuilder(url);
		
		// add properties delim if there are properties
		if(!properties.isEmpty())
			url_builder.append("?");
		
		// add all required/optional properties
		for(String property : properties.keySet()) {
			String value = properties.get(property);
			
			// check if empty
			if(value == null || value.equals("")) {
				System.err.println(String.format("Property cannot be empty <%s>", property));
				return null;
			}
			
			// check if required
			if(value.equals(".") && this.properties.containsKey(property)) {
				System.err.println(String.format("Required property <%s> not defined.", property));
				return null;
			}
			
			// add to url
			url_builder.append(String.format("%s=%s&", property, value));
		}
		
		// remove final & and update builder
		if(!properties.isEmpty())
			url_builder.deleteCharAt(url_builder.length() - 1);
		builder = builder.url(url_builder.toString());
		
		// add all headers
		for(String header : headers.keySet()) {
			String value = headers.get(header);
			
			// check if empty
			if(value == null || value.equals("")) {
				System.err.println(String.format("Header cannot be empty <%s>", header));
				return null;
			}
			
			// check if required
			if(value.equals(".") && this.properties.containsKey(header)) {
				System.err.println(String.format("Required header <%s> not defined.", header));
				return null;
			}
			
			builder = builder.addHeader(header, value);
		}

		return builder.build();
	}


	public final synchronized String request(String[] url_path, HashMap<String, String> properties, HashMap<String, String> headers) {
		// validate that url_path is correctly formatted
		if(url_path.length % 2 != 0) {
			System.err.println("Url path is not formatted properly and must be in <key, value> pairs.");
			return "Url path is not formatted properly and must be in <key, value> pairs.";
		}
		
		// validate that url_path parameter is valid
		if(this.url_path.length != url_path.length) {
			System.err.println("Url path does not match defined url path.");
			return "Url path does not match defined url path.";
		}
		
		// update path if there are url path extensions
		StringBuilder url_builder = new StringBuilder(url);
		if(url_path.length > 0) {
			for(int i = 0; i < url_path.length; i+=2) {
				if(!url_path[i].equals(this.url_path[i])) {
					System.err.println(String.format("Url path key <%s> does not match defined key <%s>", url_path[i], this.url_path[i]));
					return String.format("Url path key <%s> does not match defined key <%s>", url_path[i], this.url_path[i]);
				}
				
				url_builder.append("/").append(url_path[i+1]);
			}
		}
		
		return process(url_builder.toString(), properties, headers);
	}
	
	public final synchronized String request(String[] url_path, HashMap<String, String> properties, HashMap<String, String> headers,
			String startDate, String endDate) {
		
		// validate that the request can be dated
		if(!is_dated) {
			System.err.println("Request cannot be dated.");
			return "Request cannot be dated.";
		}
		
		// parse passed dates
		LocalDate start, end;
		try {
			start = LocalDate.parse(startDate, formatter);
			end = LocalDate.parse(endDate, formatter);
		} catch (DateTimeParseException e) {
			return "Unable to parse dates.";
		}
		
		if(start == null || end == null) {
			System.err.println("Fatal error parsing dates.");
			return "Fatal error parsing dates.";
		}
		
		// retrieve all dates in-between
		Stream<LocalDate> dates = null;
		
		try {
			dates = start.datesUntil(end);
		} catch(Exception e) {
			System.err.println("End date must be after start date.");
			return "End date must be after start date.";
		}
		
		// submit requests for each date
		Iterator<LocalDate> itr = dates.iterator();
		while(itr.hasNext()) {
			// retrieve date
			LocalDate date = itr.next();
						
			// retrieve start date var and update to location
			switch(date_location) {
			case "properties":
				properties.put(date_start_var, date.format(date_format));
				break;
			case "headers":
				headers.put(date_start_var, date.format(date_format));
				break;
			default:
				System.err.println("Invalid date.location parameter value.");
				System.exit(1);
			}
			
			// if end date is required add one day and push to properties
			if(!date_end_var.equals(".")) {
				LocalDate tmr = date.plusDays(1);
				switch(date_location) {
				case "properties":
					properties.put(date_end_var, tmr.format(date_format));
					break;
				case "headers":
					headers.put(date_end_var, tmr.format(date_format));
					break;
				default:
					System.err.println("Invalid date.location parameter value.");
					System.exit(1);
				}
			}
			
			// submit request with updated properties
			System.out.println(String.format("Requesting: name=[%s] date=[%s]", getName(), date.toString()));
			String request = request(url_path, properties, headers);
			if(request != null)
				return request;
		}
		
		return null;
	}
	
	protected String processUrl(String url, HashMap<String, String> headers) {
		HashMap<String, String> all_headers = new HashMap<String, String>();
		
		for(String header : this.headers.keySet())
			all_headers.put(header, this.headers.get(header));
		
		for(String header : headers.keySet())
			all_headers.put(header, headers.get(header));
		
		return processRequest(url, new HashMap<String, String>(), all_headers);
	}
	
	protected String process(String url, HashMap<String, String> properties, HashMap<String, String> headers) {
		HashMap<String, String> all_properties = new HashMap<String, String>();
		HashMap<String, String> all_headers = new HashMap<String, String>();
		
		// add all properties and headers
		for(String property : this.properties.keySet())
			all_properties.put(property, this.properties.get(property));
		
		for(String property : properties.keySet())
			all_properties.put(property, properties.get(property));
		
		for(String header : this.headers.keySet())
			all_headers.put(header, this.headers.get(header));
		
		for(String header : headers.keySet())
			all_headers.put(header, headers.get(header));
		
		return processRequest(url, all_properties, all_headers);
	}
	
	protected String processRequest(String url, HashMap<String, String> properties, HashMap<String, String> headers) {
		OkHttpClient client = new OkHttpClient();
		Request request = getRequest(url, properties, headers);
		if(request == null) {
			System.err.println("Malformed request, killing process.");
			return "Malformed request, killing process.";
		}
		
		Response response = null;
		String body = null;
		try {
			response = client.newCall(request).execute();
			body = response.body().string().toString();
			if(response.code() != 200) {
				return String.format("Request Failure code=<%d> url=<%s> body=<%s>", response.code(), request.url().toString(), body);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(body == null) {
			System.err.println("Response had fatal issue, killing process.");
			return "Response had fatal issue, killing process.";
		}
		
		// send to specific request handler
		try {
			handle(body, properties, headers);
		} catch(Exception e) {
			e.printStackTrace();
			return e.toString();
		}
		
		return null;
	}
	
	protected abstract String handle(String json, HashMap<String, String> properties, HashMap<String, String> headers);

	public abstract String getType();
	
	protected final HashMap<String, String> parse(Object input) throws JSONException {

		HashMap<String, String> out = new HashMap<String, String>();
		
	    if (input instanceof JSONObject) {

	        Iterator<?> keys = ((JSONObject) input).keys();

	        while (keys.hasNext()) {

	            String key = (String) keys.next();

	            if (!(((JSONObject) input).get(key) instanceof JSONArray)) {
	                if (((JSONObject) input).get(key) instanceof JSONObject) {
	                	out.putAll(parse(((JSONObject) input).get(key)));
	                } else {
	                    out.put(key, ((JSONObject) input).get(key).toString());
	                }
	            } else {
	            	out.putAll(parse(new JSONArray(((JSONObject) input).get(key).toString())));
	            }
	        }
	    }

	    if (input instanceof JSONArray) {
	        for (int i = 0; i < ((JSONArray) input).length(); i++) {
	            JSONObject a = ((JSONArray) input).getJSONObject(i);
	            out.putAll(parse(a));
	        }
	    }
	    
	    return out;
	}
}