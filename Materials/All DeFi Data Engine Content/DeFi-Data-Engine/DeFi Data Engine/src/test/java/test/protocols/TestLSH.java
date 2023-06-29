package test.protocols;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.core.core.Core;
import org.junit.Test;
import org.properties.Config;

public class TestLSH {

static {
	// disable loggers
	LogManager.getRootLogger().setLevel(Level.OFF);
	Config.setProperty("stream", "general.consumer.types", "null");
	Config.setProperty("stream", "general.producer.types", "null");
	Config.setProperty("stream", "local.stream.type", "null");
}

	@Test
	public void TestINIT() {
		Config.setProperty("stream", "local.stream.type", "local_template");
		Config.setProperty("testing", "lsh.ready", "true");
		Core core = new Core();
		
		assertEquals(440, core.send("LSH", "INIT", "null").code());
		assertEquals(443, core.send("LSH", "INIT", "local_template").code());
		assertEquals(500, core.send("LSH", "INIT", "").code());
		
		// enable to check engine catch invalid property: local.stream.type
		// Config.setProperty("stream", "local.stream.type", "invalid");
		// new Core();
	}
	
	@Test
	public void TestSCAN() {
		Config.setProperty("stream", "local.stream.type", "local_template");
		Core core = new Core();
		
		assertEquals(200, core.send("LSH", "SCAN", "valid").code());

		Config.setProperty("testing", "lsh.ready", "false");
		assertEquals(441, core.send("LSH", "SCAN", "valid").code());
		Config.setProperty("testing", "lsh.ready", "true");
		
		assertEquals(445, core.send("LSH", "SCAN", "invalid").code());
		assertEquals("true", core.send("LSH", "SCAN", "valid").data());
		assertEquals("false", core.send("LSH", "SCAN", "dne").data());
		
		assertEquals(500, core.send("LSH", "SCAN", "").code());
	}
	
	@Test
	public void TestRQST() {
//		Config.setProperty("stream", "local.stream.type", "local_template");
//		Core core = new Core();
//		
//		assertEquals(200, core.send("LSH", "RQST", "valid", "null").code());
//		
//		Config.setProperty("testing", "lsh.ready", "false");
//		assertEquals(441, core.send("LSH", "RQST", "valid", "null").code());
//		Config.setProperty("testing", "lsh.ready", "true");
//		
//		assertEquals(445, core.send("LSH", "RQST", "invalid").code());
//		assertEquals(446, core.send("LSH", "RQST", "dne", "null").code());
//		assertEquals(447, core.send("LSH", "RQST", "irregular").code());
//		
//		assertEquals(500, core.send("LSH", "RQST", "").code());
	}
	
	@Test
	public void TestSTAT() {
		Config.setProperty("stream", "local.stream.type", "local_template");
		Core core = new Core();
		
		assertEquals(200, core.send("LSH", "STAT", "valid").code());
		assertEquals("EXISTS", core.send("LSH", "STAT", "valid").data());
		
		Config.setProperty("testing", "lsh.ready", "false");
		assertEquals(441, core.send("LSH", "STAT", "valid").code());
		Config.setProperty("testing", "lsh.ready", "true");
		
		assertEquals(445, core.send("LSH", "STAT", "invalid").code());
		assertEquals(446, core.send("LSH", "STAT", "dne").code());
		assertEquals(448, core.send("LSH", "STAT", "irregular").code());
		
		assertEquals(500, core.send("LSH", "STAT", "").code());
	}
	
	@Test
	public void TestPUSH() {
		Config.setProperty("stream", "local.stream.type", "local_template");
		Core core = new Core();
		
		assertEquals(200, core.send("LSH", "PUSH", format("1", "valid")).code());
		
		Config.setProperty("testing", "lsh.ready", "false");
		assertEquals(441, core.send("LSH", "PUSH", format("1", "valid")).code());
		Config.setProperty("testing", "lsh.ready", "true");
		
		assertEquals(449, core.send("LSH", "PUSH", format("1", "invalid")).code());
		
		assertEquals(500, core.send("LSH", "PUSH", "").code());
		assertEquals(500, core.send("LSH", "PUSH", "1").code());
	}
	
	private String format(String s1, String s2) {
		return s1 + Config.getProperty("app", "general.internal.delim") + s2;
	}
}
