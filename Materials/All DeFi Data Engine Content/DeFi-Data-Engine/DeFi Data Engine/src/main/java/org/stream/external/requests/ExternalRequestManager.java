package org.stream.external.requests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import org.core.logger.Logger;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.stream.external.handler.ExternalStreamManager;

public class ExternalRequestManager {
	
	private static final HashMap<String, Class<? extends ExternalRequestFramework>> templates = new HashMap<String, Class<? extends ExternalRequestFramework>>();
	private static final HashMap<String, ExternalRequestFramework> requests = new HashMap<String, ExternalRequestFramework>();
	
	public void initialize(ExternalStreamManager manager) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, URISyntaxException, IOException {
		// load all templates
		Reflections reflection = new Reflections(new ConfigurationBuilder().forPackages(ExternalRequestManager.class.getPackageName()));
		Set<Class<? extends ExternalRequestFramework>> types = reflection.getSubTypesOf(ExternalRequestFramework.class);
		for(Class<? extends ExternalRequestFramework> c : types) {
			templates.put(c.getDeclaredConstructor().newInstance().getType(), c);
		}
		
		// load all property files
		File directory = new File("target/classes/requests/");
		if(!directory.isDirectory()) {
			System.err.println("Missing /target/classes/requests/ directory.");
			System.exit(1);
		}
		
		String[] requests = directory.list();
		for(String r : requests) {
			// create file
			Properties p = new Properties();
			p.load(new FileInputStream(directory.getPath() + "/" + r));
			
			// extract all properties and parse
			String name = null;
			String url = null;
			String[] url_path = {};
			HashMap<String, String> properties = new HashMap<String, String>();
			HashMap<String, String> headers = new HashMap<String, String>();
			String recursive_type = null;
			HashMap<String, String> tags = new HashMap<String, String>();
			String[] recursive_location = {};
			String recursive_replacement = null;
			String[] path = {};
			boolean is_dated = false;
			String date_location = null;
			String date_start_var = null;
			String date_end_var = null;
			String date_format = null;
			
			// parse name
			if(p.containsKey("request.name")) {
				name = p.getProperty("request.name");
			} else {
				System.err.println(String.format("Missing required property <%s>.", "request.name"));
				System.exit(1);
			}
			
			// parse url base
			if(p.containsKey("url.base")) {
				url = p.getProperty("url.base");
			} else {
				System.err.println(String.format("Missing required property <%s>.", "url.base"));
				System.exit(1);
			}
			
			// parse url base properties
			if(p.containsKey("url.base.path")) {
				url_path = p.getProperty("url.base.path").split(",");
				if(url_path.length % 2 != 0) {
					System.err.println("Url path must be formatted with <key, value> pairs.");
					System.exit(1);
				}
			}
			
			// parse url properties
			if(p.containsKey("url.properties")) {
				String[] arr = p.getProperty("url.properties").split(",");
				if(arr.length % 2 != 0) {
					System.err.println("Each property must be a <key, value> pair.");
					System.exit(1);
				}
				
				for(int i = 0; i < arr.length; i+=2)
					properties.put(arr[i], arr[i + 1]);
			} else {
				System.err.println(String.format("Missing required property <%s>.", "url.properties"));
				System.exit(1);
			}
			
			// parse url headers
			if(p.containsKey("url.headers")) {
				String[] arr = p.getProperty("url.headers").split(",");
				if(arr.length % 2 != 0) {
					System.err.println("Each property must be a <key, value> pair.");
					System.exit(1);
				}
				
				for(int i = 0; i < arr.length; i+=2)
					headers.put(arr[i], arr[i + 1]);
			} else {
				System.err.println(String.format("Missing required property <%s>.", "url.headers"));
				System.exit(1);
			}
			
			// parse data path
			if(p.containsKey("data.path")) {
				path = p.getProperty("data.path").split(",");
			} else {
				System.err.println(String.format("Missing required property <%s>.", "data.path"));
				System.exit(1);
			}
			
			// parse recursion type
			if(p.containsKey("recursion.type")) {
				recursive_type = p.getProperty("recursion.type");
			} else {
				System.err.println(String.format("Missing required property <%s>.", "url.recursive.type"));
				System.exit(1);
			}
			
			// parse recursion tags
			if(p.containsKey("recursion.tags")) {
				String[] arr = p.getProperty("recursion.tags").split(",");
				if(arr.length % 2 != 0) {
					System.err.println("Each property must be a <key, value> pair.");
					System.exit(1);
				}
				
				for(int i = 0; i < arr.length; i+=2)
					tags.put(arr[i], arr[i + 1]);
			}
			
			// parse recursive location
			if(p.containsKey("recursion.location")) {
				recursive_location = p.getProperty("recursion.location").split(",");
			}
			
			// parse recursive replacement location
			if(p.containsKey("recursion.replacement")) {
				recursive_replacement = p.getProperty("recursive.replacement");
			}
			
			// parse date valid
			if(p.containsKey("date.valid")) {
				try {
					is_dated = Boolean.parseBoolean(p.getProperty("date.valid"));
				} catch(Exception e) {
					System.err.println("Property date.valid is not of type boolean.");
					System.exit(1);
				}
			} else {
				System.err.println(String.format("Missing required property <%s>.", "date.valid"));
				System.exit(1);
			}
			
			// parse date location
			if(p.containsKey("date.location")) {
				date_location = p.getProperty("date.location");
			} else if(!p.containsKey("date.location") && is_dated) {
				System.err.println("Missing required property date.location if date.valid=true.");
				System.exit(1);
			}
			
			// parse date start
			if(p.containsKey("date.start")) {
				date_start_var = p.getProperty("date.start");
			} else if(!p.containsKey("date.start") && is_dated) {
				System.err.println("Missing required property date.start if date.valid=true.");
				System.exit(1);
			}
			
			// parse date end
			if(p.containsKey("date.end")) {
				date_end_var = p.getProperty("date.end");
			} else if(!p.containsKey("date.end") && is_dated) {
				System.err.println("Missing required property date.end if date.valid=true.");
				System.exit(1);
			}
			
			// parse date format
			if(p.containsKey("date.format")) {
				date_format = p.getProperty("date.format");
			} else if(!p.containsKey("date.format") && is_dated) {
				System.err.println("Missing required property date.format if date.valid=true.");
				System.exit(1);
			}
			
			// validate templates contains proper key
			if(!templates.containsKey(recursive_type)) {
				System.err.println(String.format("Template not found for <%s>.", recursive_type));
				System.exit(1);
			}
			
			// validate that duplicate request doesn't exist
			if(ExternalRequestManager.requests.containsKey(name)) {
				System.err.println(String.format("Duplicate name found <%s>. Please modify one of the names to be unique.", name));
				System.exit(1);
			}
			
			ExternalRequestManager.requests.put(name, templates.get(recursive_type).getDeclaredConstructor(
					ExternalStreamManager.class, String.class, String.class, String[].class, HashMap.class, HashMap.class,
					HashMap.class, String[].class, String.class, String[].class,
					boolean.class, String.class, String.class, String.class, String.class).newInstance(
					manager, name, url, url_path, properties, headers, tags, recursive_location, recursive_replacement, path,
					is_dated, date_location, date_start_var, date_end_var, date_format));
		
			Logger.log(String.format("[ExternalRequestManager] Successfully added Request Framework type [%s]", name));
		}
	}
	
	public final boolean hasRequestFormat(String name) {
		return requests.containsKey(name);
	}
	
	public final ExternalRequestFramework getRequestFormat(String name) {
		return requests.get(name);
	}
}
