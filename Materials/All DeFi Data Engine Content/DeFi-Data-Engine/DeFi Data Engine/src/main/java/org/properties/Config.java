package org.properties;

import java.util.HashMap;
import java.util.Properties;

public class Config {

	private static final HashMap<String, Properties> properties;
	
static {
	properties = new HashMap<String, Properties>();
	
	Properties app_properties = new Properties();
	app_properties.put("general.internal.delim", ":::");
	app_properties.put("general.data.delim", ",");
	app_properties.put("general.collection.delim", "=");
	app_properties.put("general.transfer.delim", "&&&");
	app_properties.put("general.data.dateformat", "yyyy-MM-dd");
	app_properties.put("general.logging.packets", "false");
	app_properties.put("general.logging.responses", "false");
	properties.put("app", app_properties);
	
	Properties stream_properties = new Properties();
	stream_properties.put("general.consumer.types", "null");
	stream_properties.put("general.producer.types", "socket_producer");
	stream_properties.put("rest.socket.address", "DataEngine");
	//stream_properties.put("rest.socket.address", "localhost");
	stream_properties.put("rest.socket.port", "61100");
	stream_properties.put("rest.socket.key", "rest-key-reserved");
	stream_properties.put("output.socket.address", "RestApp");
	// stream_properties.put("output.socket.address", "localhost");
	stream_properties.put("output.socket.port", "61200");
	stream_properties.put("local.stream.type", "mongo_db");
	//stream_properties.put("local.stream.type", "null");
	stream_properties.put("mongodb.properties.uri", "mongodb://MONGO:27017");
	//stream_properties.put("mongodb.properties.uri", "mongodb://localhost:27017");
	stream_properties.put("mongodb.database.state", "main-state-db");
	stream_properties.put("mongodb.database.main", "main-db");
	stream_properties.put("mongodb.auth.collection", "auth-collection");
	stream_properties.put("mongodb.query.delim", ",");
	stream_properties.put("polygon.request.delim", "-");
	properties.put("stream", stream_properties);
	
	Properties testing_properties = new Properties();
	testing_properties.put("lsh.authorized", "true");
	testing_properties.put("lsh.ready", "true");
	properties.put("testing", testing_properties);
}

	public static final Properties getProperties(String name) {
		validate(name);
		return properties.get(name);
	}
	
	public static final String getProperty(String name, String property) {
		validate(name, property);
		return properties.get(name).getProperty(property);
	}
	
	public static final void setProperty(String name, String property, String value) {
		validate(name, property);
		properties.get(name).setProperty(property, value);
	}
	
	public static final void validate(String name, String... keys) {
		if(!properties.containsKey(name)) {
			new IllegalArgumentException(String.format("Property file <%s> does not exist. Program terminating.", name)).printStackTrace();
			System.exit(1);
		}
		
		for(String key : keys)
			if(!properties.get(name).containsKey(key)) {
				new IllegalArgumentException(String.format("Missing property <%s> in file <%s>. Program terminating.", key, name)).printStackTrace();
				System.exit(1);
			}
	}
}
