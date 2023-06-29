package org.stream.local.connected.mongodb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.properties.Config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class MongoDatabaseRequestHandler {

	private final static HashMap<String, HashMap<String, Integer>> translations;
	private final static HashMap<String, Integer> queries;
	private final static HashMap<String, Method> requests;
	
// define translation
// note translation maps all get, modify, and set queries to contains queries
// note <skip> alerts the engine to skip contains protocol
static {
	translations = new HashMap<String, HashMap<String, Integer>>();
	
	translations.put("contains_collection", new HashMap<String, Integer>());
	translations.get("contains_collection").put("collection", 1);
	
	translations.put("contains_type", new HashMap<String, Integer>());
	translations.get("contains_type").put("collection", 1);
	translations.get("contains_type").put("type", 2);
	
	translations.put("contains_item", new HashMap<String, Integer>());
	translations.get("contains_item").put("collection", 1);
	translations.get("contains_item").put("type", 2);
	translations.get("contains_item").put("id", 3);
	
	translations.put("get_all", new HashMap<String, Integer>());
	translations.get("get_all").put("collection", 1);
	
	translations.put("get_item", new HashMap<String, Integer>());
	translations.get("get_item").put("collection", 1);
	translations.get("get_item").put("type", 2);
	translations.get("get_item").put("id", 3);
}

// define queries
static {
	queries = new HashMap<String, Integer>();
	
	// contains queries:
	queries.put("contains_collection", 1);
	queries.put("contains_type", 2);
	queries.put("contains_item", 3);
	
	// get queries:
	queries.put("get_all", 1);
	queries.put("get_item", 3);
}

// define requests
static {
	requests = new HashMap<String, Method>();
	
	Class<MongoDatabaseRequestHandler> classobj = MongoDatabaseRequestHandler.class;
	
	try {
		// contains requests:
		requests.put("contains_collection", classobj.getMethod("containsCollection", MongoDatabase.class, String[].class));
		requests.put("contains_type", classobj.getMethod("containsType", MongoDatabase.class, String[].class));
		requests.put("contains_item", classobj.getMethod("containsItem", MongoDatabase.class, String[].class));
		
		// get requests:
		requests.put("get_all", classobj.getMethod("getAll", MongoDatabase.class, String[].class));
		requests.put("get_item", classobj.getMethod("getItem", MongoDatabase.class, String[].class));
	} catch(Exception e) {
		e.printStackTrace();
		System.exit(1);
	}
}
	
	public final static Integer getParameterTranslation(String protocol, String parameter) {
		if(!translations.containsKey(protocol) || !translations.get(protocol).containsKey(parameter))
			return -1;
		
		return translations.get(protocol).get(parameter);
	}

	public final static boolean validate(String... query) {
		// validate not empty
		if(query.length == 0)
			return false;
		
		// validate contains query name
		if(!queries.containsKey(query[0]) || !requests.containsKey(query[0]) || !translations.containsKey(query[0]))
			return false;
		
		// validate parameter length
		return query.length == queries.get(query[0]) + 1;
	}
	
	// contains functions:
	public final static boolean contains(MongoDatabase db, String... query) {
		if(!validate(query))
			return false;
		
		for(int i = 0; i < query.length; i++)
			query[i] = query[i].trim();
		
		if(!translations.containsKey(query[0]))
			return false;
		
		try {
			// determine contains query depth
			if(translations.get(query[0]).containsKey("id"))
				return (boolean)requests.get("contains_item").invoke(null, db, 
						new String[] {"contains_item",
								query[getParameterTranslation(query[0], "collection")],
								query[getParameterTranslation(query[0], "type")],
								query[getParameterTranslation(query[0], "id")]
						});
			
			else if(translations.get(query[0]).containsKey("type"))
				return (boolean)requests.get("contains_type").invoke(null, db, 
						new String[] {"contains_type",
								query[getParameterTranslation(query[0], "collection")],
								query[getParameterTranslation(query[0], "type")]
						});
			
			else if(translations.get(query[0]).containsKey("collection"))
				return (boolean)requests.get("contains_collection").invoke(null, db, 
						new String[] {"contains_collection",
								query[getParameterTranslation(query[0], "collection")]
						});
			
			// non-canceling contains parameter to not falsely trigger query failure
			else
				//TODO implement catching for failed contains translation
				return true;
								
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return false;
	}
	
	public final static boolean containsCollection(MongoDatabase db, String[] query) {
		if(!query[0].equals("contains_collection"))
			return false;
		
		MongoCursor<String> itr = db.listCollectionNames().iterator();
		while(itr.hasNext()) {
			if(itr.next().equals(query[1]))
				return true;
		}
		
		return false;
	}
	
	public final static boolean containsType(MongoDatabase db, String[] query) {
		if(!query[0].equals("contains_type"))
			return false;
		
		if(!containsCollection(db, new String[] {"contains_collection", query[1]}))
			return false;
		
		Document document = db.getCollection(query[1]).find().first();
		for(Entry<String, Object> type : document.entrySet())
			if(type.getKey().equals(query[2]))
				return true;
		
		return false;
	}
	
	public final static boolean containsItem(MongoDatabase db, String[] query) {
		if(!query[0].equals("contains_item"))
			return false;
		
		if(!containsType(db, new String[] {"contains_type", query[1], query[2]}))
			return false;
		
		return db.getCollection(query[1]).find(Filters.eq(query[2], query[3])).first() != null;
	}
	
	// get functions:
	@SuppressWarnings("unchecked")
	public final static Set<String> get(MongoDatabase db, String[] query) {
		if(!validate(query))
			return null;
		
		try {
			return (Set<String>)requests.get(query[0]).invoke(null, db, query);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return null;
	}
	
	public final static Set<String> getAll(MongoDatabase db, String[] query) {
		if(!query[0].equals("get_all"))
			return null;
		
		if(!containsCollection(db, new String[] {"contains_collection", query[1]}))
			return null;
		
		MongoCollection<Document> collection = db.getCollection(query[1]);
		Set<String> out = new HashSet<String>();
		MongoCursor<Document> itr = collection.find().iterator();
		while(itr.hasNext())
			out.add(itr.next().toJson());
		
		return out;
	}
	
	public final static Set<String> getItem(MongoDatabase db, String... query) {
		if(!query[0].equals("get_item"))
			return null;
		
		if(!containsItem(db, new String[] {"contains_item", query[1], query[2], query[3]}))
			return null;
		
		for(int i = 0; i < query.length; i++)
			query[i] = query[i].trim();
		
		MongoCollection<Document> collection = db.getCollection(query[1]);
		Set<String> out = new HashSet<String>();
		MongoCursor<Document> itr = collection.find(Filters.eq(query[2], query[3])).iterator();
		while(itr.hasNext())
			out.add(itr.next().toJson());
		
		return out;
	}
	
	public final static boolean push(MongoDatabase db, String data, String collection_name) {
		// retrieve created collection
		MongoCollection<Document> collection = db.getCollection(collection_name);
		
		String[] split_data = data.split(Config.getProperty("app", "general.data.delim"));
		// validate that every type has an id associated.
		if(split_data.length % 2 != 0 && !data.equals("<<<empty>>>"))
			return false;
		
		Document document = new Document();
		
		// validate not empty
		if(!data.equals("<<<empty>>>")) {
			for(int i = 0; i < split_data.length; i+=2)
				document.append(split_data[i], split_data[i + 1]);
			document.append("_timestamp", System.nanoTime());
			collection.insertOne(document);
		} else {
			// create temporary object then remove
			document.append("_empty", "holder");
			collection.insertOne(document);
			Bson holder = Filters.eq("_empty", "holder");
			collection.deleteOne(holder);
		}
		return true;
	}
}
