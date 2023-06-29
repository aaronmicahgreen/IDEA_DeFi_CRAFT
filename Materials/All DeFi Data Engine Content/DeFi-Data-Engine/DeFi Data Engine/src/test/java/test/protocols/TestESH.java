package test.protocols;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.core.core.Core;
import org.framework.router.Response;
import org.junit.Test;
import org.properties.Config;

public class TestESH {

static {
	// disable loggers
	LogManager.getRootLogger().setLevel(Level.OFF);
	Config.setProperty("stream", "general.consumer.types", "null");
	Config.setProperty("stream", "general.producer.types", "null");
	Config.setProperty("stream", "local.stream.type", "null");
}
	
	@Test
	public void TestEXSR() {
		Core core = new Core();
	
		assertEquals(200, core.send("SRC", "EXSR", "external_template").code());
		assertEquals("true", core.send("SRC", "EXSR", "external_template").data());
		assertEquals("false", core.send("SRC", "EXSR", "template").data());
		
		assertEquals(500, core.send("SRC", "EXSR", "").code());
	}
	
	@Test
	public void TestEXST() {
		Core core = new Core();
		
		assertEquals(200, core.send("SRC", "INIT", "external_template, key").code());
		
		assertEquals("true", core.send("SRC", "EXST", "key").data());
		assertEquals("false", core.send("SRC", "EXST", "key1").data());
	
		assertEquals(500, core.send("SRC", "EXST", "").code());
	}
	
	@Test
	public void TestINIT() {
		Core core = new Core();
		
		Response valid = core.send("SRC", "INIT", "external_template, key");
		assertEquals(200, valid.code());
		assertEquals("key", valid.data());
		assertEquals("true", core.send("SRC", "EXST", "key").data());
		
		assertEquals(220, core.send("SRC", "INIT", "external_template, key").code());
		
		assertEquals(420, core.send("SRC", "INIT", "does_not_exist").code());
		assertEquals(422, core.send("SRC", "INIT", "external_template, wrong").code());
		assertEquals(422, core.send("SRC", "INIT", "external_template").code());
		assertEquals(500, core.send("SRC", "INIT", "").code());
	}
	
	@Test
	public void TestIATH() {
		Core core = new Core();
		
		assertEquals(200, core.send("SRC", "INIT", "external_template, key").code());
		
		assertEquals(200, core.send("SRC", "IATH", "key").code());
		assertEquals("true", core.send("SRC", "IATH", "key").data());
		
		assertEquals(421, core.send("SRC", "IATH", "does_not_exist").code());
		assertEquals(500, core.send("SRC", "IATH", "").code());
	}
	
	@Test
	public void TestIATV() {
		Core core = new Core();
	
		assertEquals(200, core.send("SRC", "INIT", "external_template, key").code());
		assertEquals("false", core.send("SRC", "IATV", "key").data());
		assertEquals(200, core.send("SRC", "EXEC", "key").code());
		assertEquals("true", core.send("SRC", "IATV", "key").data());
	
		assertEquals(421, core.send("SRC", "IATV", "does_not_exist").code());
		assertEquals(500, core.send("SRC", "IATV", "").code());
	}
	
	@Test
	public void TestEXEC() {
		Core core = new Core();
	
		assertEquals(200, core.send("SRC", "INIT", "external_template, key").code());
		assertEquals(200, core.send("SRC", "INIT", "external_template, not_ready, true").code());
		
		assertEquals(200, core.send("SRC", "EXEC", "key").code());
		assertEquals("true", core.send("SRC", "IATH", "key").data());
		assertEquals("true", core.send("SRC", "IATV", "key").data());
		
		assertEquals(421, core.send("SRC", "EXEC", "does_not_exist").code());
		assertEquals(423, core.send("SRC", "EXEC", "not_ready").code());
		assertEquals(424, core.send("SRC", "EXEC", "key").code());
	}
	 
	@Test
	public void TestKILL() {
		Core core = new Core();
	
		assertEquals(200, core.send("SRC", "INIT", "external_template, key").code());
		assertEquals(200, core.send("SRC", "EXEC", "key").code());
	
		assertEquals("true", core.send("SRC", "IATV", "key").data());
		assertEquals("true", core.send("SRC", "KILL", "key").data());
		assertEquals("false", core.send("SRC", "IATV", "key").data());
	
		assertEquals(421, core.send("SRC", "KILL", "does_not_exist").code());
		assertEquals(425, core.send("SRC", "KILL", "key").code());
		assertEquals(500, core.send("SRC", "KILL", "").code());
	}
}
