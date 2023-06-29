package test.framework.router;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.framework.router.Packet;
import org.framework.router.Response;
import org.framework.router.ResponseFactory;
import org.framework.router.Router;
import org.junit.Test;

class RouterTemp extends Router {

	public RouterTemp(String uuid, String tag) {
		super(uuid, tag);
	}
	
	public void defineProcesses() throws NoSuchMethodException, SecurityException {
		Method p1 = getClass().getMethod("process1", Packet.class);
		p1.setAccessible(true);
		addProcess("", p1);
	}
	
	public Response process1(Packet packet) {
		return ResponseFactory.response0();
	}
}

public class TestManager {

	@Test
	public void TestConnection() {
		RouterTemp r1 = new RouterTemp("r1", "r1");
		RouterTemp r2 = new RouterTemp("r2", "r2");
		RouterTemp r3 = new RouterTemp("r3", "r3");
		
		r1.connect(r2);
		
		assertEquals(true, r1.isConnected(r2));
		assertEquals(false, r1.isConnected(r3));
		assertEquals(false, r2.isConnected(r3));
	}
	
	@Test
	public void TestExistingConnection() {
		RouterTemp r1 = new RouterTemp("r1", "r1");
		RouterTemp r2 = new RouterTemp("r2", "r2");
		RouterTemp r3 = new RouterTemp("r3", "r3");
		
		r1.connect(r2);
		r1.connect(r2);
		r2.connect(r1);
		
		assertEquals(true, r1.isConnected(r2));
		assertEquals(false, r1.isConnected(r3));
		assertEquals(false, r2.isConnected(r3));
	}
	
	@Test
	public void TestSends() {
		RouterTemp r1 = new RouterTemp("r1", "r1");
		RouterTemp r2 = new RouterTemp("r2", "r2");
		RouterTemp r3 = new RouterTemp("r3", "r3");
		RouterTemp r4 = new RouterTemp("r4", "r4");
		
		r1.connect(r2);
		
		assertEquals(0, r1.send("r2", "", "").code());
		assertEquals(0, r2.send("r1", "", "").code());
		assertEquals(404, r1.send("r3", "", "").code());
		assertEquals(400, r3.send("r1", "", "").code());
		
		r3.connect(r4);
		
		assertEquals(404, r3.send("r2", "", "").code());
	}
}
