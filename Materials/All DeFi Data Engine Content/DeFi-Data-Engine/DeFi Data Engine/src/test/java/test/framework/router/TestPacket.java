package test.framework.router;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.framework.router.Packet;
import org.framework.router.Response;
import org.framework.router.ResponseFactory;
import org.framework.router.Router;
import org.junit.Test;

class TestPacketRouter extends Router {
	
	public TestPacketRouter() {
		super("TestPacketRouter", "TEST");
	}
	
	public Response process1(Packet p) {
		return ResponseFactory.response0();
	}
}

public class TestPacket {
	
	@Test
	public void TestCreatePacket() {
		TestPacketRouter router = new TestPacketRouter();
		TestPacketRouter router2 = new TestPacketRouter();
		router.connect(router2);
		Packet packet = Packet.packet(router, "TAG", "SUBTAG", "DATA");
		
		assertEquals("TEST", packet.getSender());
		assertEquals("TAG", packet.getTag());
		assertEquals("SUBTAG", packet.getSubTag());
		assertEquals("DATA", packet.getData());
		assertEquals(0, router.process1(packet).code());
	}
}
