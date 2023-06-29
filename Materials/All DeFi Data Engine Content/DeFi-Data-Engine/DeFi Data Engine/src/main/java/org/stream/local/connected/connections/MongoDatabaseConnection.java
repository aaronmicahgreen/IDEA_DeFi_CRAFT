package org.stream.local.connected.connections;

import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.properties.Config;
import org.stream.local.connected.mongodb.MongoDatabaseRequestHandler;
import org.stream.local.handler.DataState;
import org.stream.local.handler.LocalStreamConnection;
import org.stream.local.handler.LocalStreamManager;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

// https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/builders/filters/
public class MongoDatabaseConnection extends LocalStreamConnection {

	private MongoClient client;
	private MongoDatabase state_db;
	private MongoDatabase main_db;
	
	private boolean authorized = false;
	
	public MongoDatabaseConnection(LocalStreamManager manager) {
		super(manager);
	}

	@Override
	public String getUUID() {
		return "mongo_db";
	}
	
	public boolean init() {
		client = new MongoClient(new MongoClientURI(Config.getProperty("stream", "mongodb.properties.uri")));
		state_db = client.getDatabase(Config.getProperty("stream", "mongodb.database.state"));
		main_db = client.getDatabase(Config.getProperty("stream", "mongodb.database.main"));
		return true;
	}

	@Override
	public boolean authorize() {
		if(client == null || state_db == null || main_db == null)
			return false;
		
		Bson filter = Filters.eq("title", "test");
		MongoCollection<Document> collection = main_db.getCollection(Config.getProperty("stream", "mongodb.auth.collection"));
		collection.deleteOne(filter);
		
		Document document = new Document("title", "test");
		collection.insertOne(document);
		
		Document resolved = collection.find().filter(filter).first();
		
		authorized = resolved != null && resolved.get("title").equals("test");
		
		return authorized;
	}

	@Override
	public boolean isAuthorized() {
		return authorized;
	}

	@Override
	public boolean isReady() {
		return authorized;
	}

	@Override
	public boolean validate(String... query) {
		// parse query
		return MongoDatabaseRequestHandler.validate(query);
	}

	@Override
	public boolean contains(String... query) {
		if(!validate(query))
			return false;
		
		return MongoDatabaseRequestHandler.contains(main_db, query);
	}

	@Override
	public DataState state(String... query) {
		// TODO Integrate state handler
		return null;
	}

	@Override
	public Set<String> get(String... query) {
		if(!validate(query))
			return null;
		
		return MongoDatabaseRequestHandler.get(main_db, query);
	}

	@Override
	public boolean push(String data, String collection) {
		return MongoDatabaseRequestHandler.push(main_db, data, collection);
	}

	@Override
	public boolean modify(String data, String... query) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Integer getParameterTranslation(String protocol, String parameter) {
		return MongoDatabaseRequestHandler.getParameterTranslation(protocol, parameter);
	}
}
