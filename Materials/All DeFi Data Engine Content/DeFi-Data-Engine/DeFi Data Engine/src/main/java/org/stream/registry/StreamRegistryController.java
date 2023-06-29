package org.stream.registry;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.framework.router.Packet;
import org.framework.router.Response;
import org.framework.router.ResponseFactory;
import org.framework.router.Router;
import org.properties.Config;

public class StreamRegistryController extends Router {
	
	public StreamRegistryController() {
		super("stream_registry_controller", "SRC");
	}
	
	public Response processEXSR(Packet packet) {
		return send("ESH", "EXSR", packet.getData());
	}
	
	public Response processEDAT(Packet packet) {
		return send("OUT", "EDAT", packet.getData());
	}
	
	public Response processSCAN(Packet packet) {
		return send("LSH", "SCAN", packet.getData());
	}
	
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Config.getProperty("app", "general.data.dateformat"));
	public Response processRQST(Packet packet) {
		// Define passed properties. Types:
		// Dated:   	data=key, start_date, end_date, request, query, destination
		// Not Dated: 	data=key, request, query, destination
		
		String validate;
		if((validate = packet.validate("type", "destination")) != null)
			return ResponseFactory.response500("ExternalStreamHandler", validate);
		
		// check to see if request is dated
		boolean dated = false;
		if(packet.containsKey("start_date") || packet.containsKey("end_date")) {
			if((validate = packet.validate("start_date", "end_date")) != null)
				return ResponseFactory.response500("StreamRegistryController", validate);
			
			dated = true;
		}
		
		// extract packet data
		HashMap<String, String> data = packet.getData();
		
		// not dated
		if(!dated) {
			// format collection name
			StringBuilder sb = new StringBuilder();
			sb.append(data.get("type"));
			if(packet.containsKey("properties")) {
				TreeMap<String, String> ordered_properties = new TreeMap<String, String>();
				String[] properties = data.get("properties").split(",");
				if(properties.length % 2 != 0)
					return ResponseFactory.response407("SRC", "RQST", "<null>", data.toString());
				for(int i = 0; i < properties.length; i+=2)
					ordered_properties.put(properties[i], properties[i + 1]);
				sb.append("-").append(ordered_properties.toString());
			}
			
			if(packet.containsKey("url_path")) {
				String[] url_path = data.get("url_path").split(",");
				sb.append("-").append(Arrays.toString(url_path));
			}
			data.put("query", String.format("%s", sb));
			
			Response lsh_response, esh_response;
			lsh_response = send("LSH", "RQST", data);
			if(lsh_response.code() == 200)
				return lsh_response;
			
			// if data does not exist send request to external stream handler
			if(lsh_response.code() == 446) {
				esh_response = send("ESH", "RQST", data);
				if(esh_response.code() != 200)
					return esh_response;
			}
			return send("LSH", "RQST", data);
		}
		
		// dated
		else if(dated) {
			try {
				LocalDate start = LocalDate.parse(packet.getData("start_date"), formatter);
				LocalDate end = LocalDate.parse(packet.getData("end_date"), formatter);
				List<LocalDate> dates = start.datesUntil(end).collect(Collectors.toList());
				// invalid date processing
				if(dates.isEmpty())
					throw new Exception();
				
				Response lsh_response, esh_response;
				for(LocalDate date : dates) {
					// initial request
					data.put("date", date.format(formatter));
					
					// format collection name
					StringBuilder sb = new StringBuilder();
					sb.append(data.get("type"));
					sb.append("-").append(data.get("date"));
					if(packet.containsKey("properties")) {
						TreeMap<String, String> ordered_properties = new TreeMap<String, String>();
						String[] properties = data.get("properties").split(",");
						if(properties.length % 2 != 0)
							return ResponseFactory.response407("SRC", "RQST", "<null>", data.toString());
						for(int i = 0; i < properties.length; i+=2)
							ordered_properties.put(properties[i], properties[i + 1]);
						sb.append("-").append(ordered_properties.toString());
					}
					
					if(packet.containsKey("url_path")) {
						String[] url_path = data.get("url_path").split(",");
						sb.append("-").append(Arrays.toString(url_path));
					}
					data.put("query", sb.toString());
					
					// initiate request
					lsh_response = send("LSH", "RQST", data);
					if(lsh_response.code() == 200)
						continue;
					
					// if data does not exist
					if(lsh_response.code() == 446) {
						esh_response = send("ESH", "RQST", data);
						if(esh_response.code() != 200)
							return esh_response;
					} 
					
					// invalid request
					else {
						return lsh_response;
					}
					
					// request again
					lsh_response = send("LSH", "RQST", data);
					if(lsh_response.code() != 200)
						return lsh_response;
				}
				
				// send end response
				return ResponseFactory.response200();
				
			} catch(Exception e) {
				return ResponseFactory.response503(Config.getProperty("app", "general.data.dateformat"), packet.getData("start_date"), packet.getData("end_date"));
			}
		}
		
		// invalid query
		else {
			return ResponseFactory.response501("Boolean <dated> set to non binary value in cache.");
		}
	}
}