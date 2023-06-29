package test.framework.router;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.framework.router.Packet;
import org.framework.router.Response;
import org.framework.router.Router;
import org.junit.Test;

class Router1 extends Router {
	
	public Router1() {
		super("Router1", "RT1");
	}
	
	public void defineProcesses() throws NoSuchMethodException, SecurityException {
		Method p1 = getClass().getMethod("process1", Packet.class);
		p1.setAccessible(true);
		addProcess("", p1);
	}
}

class Router2 extends Router {
	
	public Router2() {
		super("Router2", "RT2");
	}
}

class RouterTemplate extends Router {

	private int num = 3;
	
	public RouterTemplate(String uuid, String tag) {
		super(uuid, tag);
	}
	
	public RouterTemplate(int num, String uuid, String tag) {
		super(uuid, tag);
		this.num = num;
	}
}

public class TestRouter {

	@Test
	// router1 <-> router2
	public void TestSimpleRouterConnection() {
		Router router1 = new Router1();
		Router router2 = new Router2();
		router1.connect(router2);

		assertEquals("Router1", router1.getUUID());
		assertEquals("RT1", router1.getTag());
		assertEquals(true, router1.isConnected(router2));
		assertEquals(true, router2.isConnected(router1));
	}
	
	@Test
	// router1 <-> router2
	public void TestSimpleRouterSendPacket() {
		Router router1 = new Router1();
		Router router2 = new Router2();
		router1.connect(router2);
		
		assertEquals(2, router1.send("RT2", "", "").code());
		assertEquals(1, router2.send("RT1", "", "").code());
	}
	
	@Test
	// router1 <-> RouterTemp <-> router2
	public void TestCentralRouterConnection() {
		Router1 r1 = new Router1();
		Router2 r2 = new Router2();
		RouterTemplate r3 = new RouterTemplate("Router3", "RT3");
		r3.connect(r1, r2);
		
		assertEquals(true, r1.isConnected(r2));
		assertEquals(true, r1.isConnected(r3));
		
		assertEquals(true, r3.isConnected(r1));
		assertEquals(true, r3.isConnected(r2));
		assertEquals(false, r3.isConnected("RT4"));

		assertEquals(2, r1.send("RT2", "", "").code());
		assertEquals(3, r1.send("RT3", "", "").code());
		
		assertEquals(1, r2.send("RT1", "", "").code());
		assertEquals(3, r2.send("RT3", "", "").code());
		
		assertEquals(1, r3.send("RT1", "", "").code());
		assertEquals(2, r3.send("RT2", "", "").code());
	}
	
	@Test
	// RouterTemp(1) <-> RouterTemp(2) <-> RouterTemp(3) <-> RouterTemp(4)
	// 						  |
	//				     RouterTemp(5) <-> RouterTemp(6)
	public void TestComplexRouterConnection() {
		RouterTemplate r1 = new RouterTemplate(1, "Router1", "RT1");
		RouterTemplate r2 = new RouterTemplate(2, "Router2", "RT2");
		RouterTemplate r3 = new RouterTemplate(3, "Router3", "RT3");
		RouterTemplate r4 = new RouterTemplate(4, "Router4", "RT4");
		RouterTemplate r5 = new RouterTemplate(5, "Router5", "RT5");
		RouterTemplate r6 = new RouterTemplate(6, "Router6", "RT6");
		
		r1.connect(r2, r3);
		r4.connect(r5, r6);
		r1.connect(r4);

		assertEquals(true, r1.isConnected(r2));
		assertEquals(true, r1.isConnected(r3));
		assertEquals(true, r1.isConnected(r4));
		assertEquals(true, r1.isConnected(r5));
		assertEquals(true, r1.isConnected(r6));

		assertEquals(true, r6.isConnected(r1));
		assertEquals(true, r6.isConnected(r2));
		assertEquals(true, r6.isConnected(r3));
		assertEquals(true, r6.isConnected(r4));
		assertEquals(true, r6.isConnected(r5));
		
	}
	
	@Test
	// RouterTemp(1) <-> RouterTemp(2) <-> RouterTemp(3) <-> RouterTemp(4)
	// 						  |
	//				     RouterTemp(5) <-> RouterTemp(6)
	public void TestComplexRouterSend() {
		RouterTemplate r1 = new RouterTemplate(1, "Router1", "RT1");
		RouterTemplate r2 = new RouterTemplate(2, "Router2", "RT2");
		RouterTemplate r3 = new RouterTemplate(3, "Router3", "RT3");
		RouterTemplate r4 = new RouterTemplate(4, "Router4", "RT4");
		RouterTemplate r5 = new RouterTemplate(5, "Router5", "RT5");
		RouterTemplate r6 = new RouterTemplate(6, "Router6", "RT6");
		
		r1.connect(r2);
		r2.connect(r3, r5);
		r3.connect(r4);
		r5.connect(r6);
		
		assertEquals(2, r1.send("RT2", "", "").code());
		assertEquals(3, r1.send("RT3", "", "").code());
		assertEquals(4, r1.send("RT4", "", "").code());
		assertEquals(5, r1.send("RT5", "", "").code());
		assertEquals(6, r1.send("RT6", "", "").code());
		
		assertEquals(1, r2.send("RT1", "", "").code());
		assertEquals(3, r2.send("RT3", "", "").code());
		assertEquals(4, r2.send("RT4", "", "").code());
		assertEquals(5, r2.send("RT5", "", "").code());
		assertEquals(6, r2.send("RT6", "", "").code());
		
		assertEquals(1, r3.send("RT1", "", "").code());
		assertEquals(2, r3.send("RT2", "", "").code());
		assertEquals(4, r3.send("RT4", "", "").code());
		assertEquals(5, r3.send("RT5", "", "").code());
		assertEquals(6, r3.send("RT6", "", "").code());
		
		assertEquals(1, r4.send("RT1", "", "").code());
		assertEquals(2, r4.send("RT2", "", "").code());
		assertEquals(3, r4.send("RT3", "", "").code());
		assertEquals(5, r4.send("RT5", "", "").code());
		assertEquals(6, r4.send("RT6", "", "").code());
		
		assertEquals(1, r5.send("RT1", "", "").code());
		assertEquals(2, r5.send("RT2", "", "").code());
		assertEquals(3, r5.send("RT3", "", "").code());
		assertEquals(4, r5.send("RT4", "", "").code());
		assertEquals(6, r5.send("RT6", "", "").code());
		
		assertEquals(1, r6.send("RT1", "", "").code());
		assertEquals(2, r6.send("RT2", "", "").code());
		assertEquals(3, r6.send("RT3", "", "").code());
		assertEquals(4, r6.send("RT4", "", "").code());
		assertEquals(5, r6.send("RT5", "", "").code());
	}
	
	@Test
	// RouterTemp(1) <-> RouterTemp(2) <-> RouterTemp(3) <-> RouterTemp(4)
	// 						  |
	//				     RouterTemp(5) <-> RouterTemp(6)
	//
	// try pre-existing connection to check handle RouterTemp(3) <-> RouterTemp(6)
	public void TestExistingConnection() {
		RouterTemplate r1 = new RouterTemplate(1, "Router1", "RT1");
		RouterTemplate r2 = new RouterTemplate(2, "Router2", "RT2");
		RouterTemplate r3 = new RouterTemplate(3, "Router3", "RT3");
		RouterTemplate r4 = new RouterTemplate(4, "Router4", "RT4");
		RouterTemplate r5 = new RouterTemplate(5, "Router5", "RT5");
		RouterTemplate r6 = new RouterTemplate(6, "Router6", "RT6");
		
		r1.connect(r2);
		r2.connect(r3, r5);
		r3.connect(r4);
		r5.connect(r6);
		
		// existing connection
		r3.connect(r6);
		assertEquals(r3.getManager(), r6.getManager());
	}
}
