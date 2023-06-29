package test.lsh.mongodb;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.core.core.Core;
import org.junit.Test;
import org.properties.Config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class TestMongoDatabase {

static {
	// disable loggers
	LogManager.getRootLogger().setLevel(Level.OFF);
	Config.setProperty("stream", "general.consumer.types", "null");
	Config.setProperty("stream", "general.producer.types", "null");
	Config.setProperty("stream", "local.stream.type", "mongo_db");
	
	// add testing database to system
	Bson filter = Filters.eq("element1", "e1");
	MongoClient client = new MongoClient(new MongoClientURI(Config.getProperty("stream", "mongodb.properties.uri")));
	MongoDatabase db = client.getDatabase("testing");
	MongoCollection<Document> collection = db.getCollection("test-mongo-database");
	collection.deleteMany(filter);
	collection.insertOne(new Document().append("_timestamp", System.nanoTime()).append("element1", "e1"));
}
	
	@Test
	public void TestINIT() {
		Config.setProperty("stream", "mongodb.database.main", "testing");
		new Core();
	}
	
	@Test
	public void TestSCAN() {
		Config.setProperty("stream", "mongodb.database.main", "testing");
		Core core = new Core();
		
		// test contains_collection
		assertEquals(200, core.send("LSH", "SCAN", "query", "contains_collection, test-mongo-database").code());
		assertEquals("true", core.send("LSH", "SCAN", "query", "contains_collection, test-mongo-database").data());	
		assertEquals("false", core.send("LSH", "SCAN", "query", "contains_collection, dne").data());
		assertEquals(445, core.send("LSH", "SCAN", "query", "contains_collection, test-mongo-database, invalid").code());
		
		// test contains_type
		assertEquals(200, core.send("LSH", "SCAN", "query", "contains_type, test-mongo-database, element1").code());
		assertEquals("true", core.send("LSH", "SCAN", "query", "contains_type, test-mongo-database, element1").data());
		assertEquals("false", core.send("LSH", "SCAN", "query", "contains_type, test-mongo-database, dne").data());
		assertEquals(445, core.send("LSH", "SCAN", "query", "contains_type, test-mongo-database, element1, dne").code());
		
		// test contains_item
		assertEquals(200, core.send("LSH", "SCAN", "query", "contains_item, test-mongo-database, element1, e1").code());
		assertEquals("true", core.send("LSH", "SCAN", "query", "contains_item, test-mongo-database, element1, e1").data());
		assertEquals("false", core.send("LSH", "SCAN", "query", "contains_item, test-mongo-database, element1, dne").data());
		assertEquals(445, core.send("LSH", "SCAN", "query", "contains_item, test-mongo-database, element1, e1, invalid").code());
	
		assertEquals(500, core.send("LSH", "SCAN", "query", "").code());
	}
	
	@Test
	public void TestRQST() {
		Config.setProperty("stream", "mongodb.database.main", "testing");
		Core core = new Core();
		
		// test get_all
		assertEquals(200, core.send("LSH", "RQST", "uuid", "external_template", "request", "test-mongo-database", "query", "get_all, test-mongo-database", "destination", "null").code());
		assertEquals(445, core.send("LSH", "RQST", "uuid", "external_template", "request", "test-mongo-database", "query", "get_all, test-mongo-database, invalid", "destination", "null").code());
		assertEquals(446, core.send("LSH", "RQST", "uuid", "external_template", "request", "test-mongo-database", "query", "get_all, dne", "destination", "null").code());
		
		// test get_item
		assertEquals(200, core.send("LSH", "RQST", "uuid", "external_template", "request", "test-mongo-database", "query", "get_item, test-mongo-database, element1, e1", "destination", "null").code());
		assertEquals(446, core.send("LSH", "RQST", "uuid", "external_template", "request", "test-mongo-database", "query", "get_item, test-mongo-database, element1, e2", "destination", "null").code());
		
		assertEquals(500, core.send("LSH", "RQST", "uuid", "").code());
	}
	
	@Test
	public void TestRQSTDated() {
		Config.setProperty("stream", "mongodb.database.main", "testing");
		Core core = new Core();

		assertEquals(200, core.send("SRC", "INIT", "source", "external_template", "key", "key").code());
		
		assertEquals(200, core.send("SRC", "RQST", "key", "key", "request", "test-mongo-database", "query", "get_all, test-mongo-database", "destination", "null",
									"start_date", "2020-09-01", "end_date", "2020-09-05").code());
	}
	
	@Test
	public void TestPUSH() {
		Config.setProperty("stream", "mongodb.database.main", "testing");
		Core core = new Core();

		assertEquals(200, core.send("LSH", "PUSH", "data", "element1, e1", "collection", "test-mongo-database").code());
		assertEquals(200, core.send("LSH", "PUSH", "data", "element1, e2", "collection", "test-mongo-database").code());
		assertEquals(449, core.send("LSH", "PUSH", "data", "element1, e2, invalid", "collection", "test-mongo-database").code());
		
		assertEquals(500, core.send("LSH", "PUSH", "data", "").code());
	}
}
