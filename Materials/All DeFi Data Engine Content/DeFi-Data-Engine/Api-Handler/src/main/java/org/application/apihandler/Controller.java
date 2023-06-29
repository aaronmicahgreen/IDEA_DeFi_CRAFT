package org.application.apihandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.stream.external.request.types.RequestFramework;
import org.stream.external.request.types.RequestManager;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping(path = {"/api/v1"})
public class Controller {

	@PostConstruct
	public void initialize() {
		try {
			RequestManager.initialize();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	}
	
	@PostMapping
	@CrossOrigin
	@RequestMapping(path = {"/request"})
	public ResponseEntity<String> handleRequest(
			@RequestParam(name="name", required=true) String name,
			@RequestParam(name="properties", required=false) String properties,
			@RequestParam(name="headers", required=false) String headers,
			@RequestParam(name="path",required=false) String path) {
		
		// assert that request exists
		if(!RequestManager.hasRequestFormat(name))
			return new ResponseEntity<String>(String.format("Request name <%s> does not exist.", name), HttpStatus.UNPROCESSABLE_ENTITY);
		
		// retrieve request framework
		RequestFramework request = RequestManager.getRequestFormat(name);
		
		// parse properties and headers
		HashMap<String, String> properties_map = new HashMap<String, String>();
		HashMap<String, String> headers_map = new HashMap<String, String>();
		String[] path_map = new String[] {};
		
		if(properties != null) {
			String[] arr = properties.split(",");
			if(arr.length % 2 != 0)
				return new ResponseEntity<String>("Properties must be in the format of <key, value> pairs.", HttpStatus.PRECONDITION_FAILED);
			
			for(int i = 0; i < arr.length; i+=2)
				properties_map.put(arr[i], arr[i + 1]);
		}
		
		if(headers != null) {
			String[] arr = headers.split(",");
			if(arr.length % 2 != 0)
				return new ResponseEntity<String>("Headers must be in the format of <key, value> pairs.", HttpStatus.PRECONDITION_FAILED);
			
			for(int i = 0; i < arr.length; i+=2)
				headers_map.put(arr[i], arr[i + 1]);
		}
		
		if(path != null) {
			path_map = path.split(",");
			if(path_map.length % 2 != 0)
				return new ResponseEntity<String>("Path must be in the format of <key, value> pairs.", HttpStatus.PRECONDITION_FAILED);
		}
		
		// submit request
		ApiHandlerApplication.lock(name);
		String response = request.request(path_map, properties_map, headers_map);
		ApiHandlerApplication.unlock(name);
		if(response != null)
			return new ResponseEntity<String>(response, HttpStatus.SERVICE_UNAVAILABLE);
		
		return new ResponseEntity<String>("", HttpStatus.OK);
	}
	
	@PostMapping
	@CrossOrigin
	@RequestMapping(path = {"/request-dated"})
	public ResponseEntity<String> handleRequestDated(
			@RequestParam(name="name", required=true) String name,
			@RequestParam(name="properties", required=false) String properties,
			@RequestParam(name="headers", required=false) String headers,
			@RequestParam(name="path",required=false) String path,
			@RequestParam(name="startDate",required=true) String start_date,
			@RequestParam(name="endDate",required=true) String end_date) {
		
		// assert that request exists
		if(!RequestManager.hasRequestFormat(name))
			return new ResponseEntity<String>(String.format("Request name <%s> does not exist.", name), HttpStatus.UNPROCESSABLE_ENTITY);
		
		// retrieve request framework
		RequestFramework request = RequestManager.getRequestFormat(name);
		
		// parse properties and headers
		HashMap<String, String> properties_map = new HashMap<String, String>();
		HashMap<String, String> headers_map = new HashMap<String, String>();
		String[] path_map = new String[] {};
		
		if(properties != null) {
			String[] arr = properties.split(",");
			if(arr.length % 2 != 0)
				return new ResponseEntity<String>("Properties must be in the format of <key, value> pairs.", HttpStatus.PRECONDITION_FAILED);
			
			for(int i = 0; i < arr.length; i+=2)
				properties_map.put(arr[i], arr[i + 1]);
		}
		
		if(headers != null) {
			String[] arr = headers.split(",");
			if(arr.length % 2 != 0)
				return new ResponseEntity<String>("Headers must be in the format of <key, value> pairs.", HttpStatus.PRECONDITION_FAILED);
			
			for(int i = 0; i < arr.length; i+=2)
				headers_map.put(arr[i], arr[i + 1]);
		}
		
		if(path != null) {
			path_map = path.split(",");
			if(path_map.length % 2 != 0)
				return new ResponseEntity<String>("Path must be in the format of <key, value> pairs.", HttpStatus.PRECONDITION_FAILED);
		}
		
		// submit request
		ApiHandlerApplication.lock(name);
		String response = request.request(path_map, properties_map, headers_map, start_date, end_date);
		ApiHandlerApplication.unlock(name);
		if(response != null)
			return new ResponseEntity<String>(response, HttpStatus.SERVICE_UNAVAILABLE);
		
		return new ResponseEntity<String>("", HttpStatus.OK);
	}
}
