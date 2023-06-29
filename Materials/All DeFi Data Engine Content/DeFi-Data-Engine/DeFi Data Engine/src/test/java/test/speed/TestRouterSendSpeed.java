package test.speed;

import java.lang.reflect.Method;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.core.core.Core;
import org.framework.router.Packet;
import org.framework.router.Response;
import org.framework.router.Router;

class RouterTemplate extends Router {

static {
	// disable loggers
	LogManager.getRootLogger().setLevel(Level.OFF);
}
	
	private int num = 0;
	
	public RouterTemplate(String uuid, String tag) {
		super(uuid, tag);
	}
	
	public RouterTemplate(int num, String uuid, String tag) {
		super(uuid, tag);
		this.num = num;
	}
	
	public void defineProcesses() throws NoSuchMethodException, SecurityException {
		Method p1 = getClass().getMethod("process1", Packet.class);
		p1.setAccessible(true);
		addProcess("", p1);
	}
}

public class TestRouterSendSpeed {

	public static void testSpeed1() {
//		long s1 = System.nanoTime();
//		boolean b = 165 == 196;
//		long e1 = System.nanoTime();
//		System.out.println(e1 - s1);
//		
//		long s2 = System.nanoTime();
//		boolean c = "a".equals("a");
//		long e2 = System.nanoTime();
//		System.out.println(e2 - s2);
		
		final RouterTemplate r1 = new RouterTemplate(1, "Router1", "RT1");
		RouterTemplate r2 = new RouterTemplate(2, "Router2", "RT2");
		RouterTemplate r3 = new RouterTemplate(3, "Router3", "RT3");
		RouterTemplate r4 = new RouterTemplate(4, "Router4", "RT4");
		RouterTemplate r5 = new RouterTemplate(5, "Router5", "RT5");
		RouterTemplate r6 = new RouterTemplate(6, "Router6", "RT6");
		RouterTemplate r7 = new RouterTemplate(6, "Router6", "RT7");
		
		r1.connect(r2);
		r2.connect(r3, r5);
		r3.connect(r4);
		r5.connect(r6);
		r6.connect(r7);
		
		// existing connection
		r3.connect(r6);
		
		// speed test
		double avg = 0;
		int runs = 10;
		for(int i = 0; i < runs; i++) {
			long start = System.currentTimeMillis();
			long count = 0;
			while(System.currentTimeMillis() < start + 1000) {
				r1.send("RT7", "", "");
				count++;
			}
			
			avg += count;
		}
		
		avg /= runs;
		System.out.println("Average packets sent: " + avg);
		System.out.println("Nano seconds per packet: " + (1000000000.0 / avg));
	}
	
	public static void testSpeed2() {
		Core core = new Core();
		

		core.send("SRC", "INIT", "external_template, key");
		
		// speed test
		double avg = 0;
		int runs = 10;
		for(int i = 0; i < runs; i++) {
			long start = System.currentTimeMillis();
			long count = 0;
			while(System.currentTimeMillis() < start + 1000) {
				core.send("SRC", "RQST", "key, correct");
				count++;
			}
			
			avg += count;
		}
		
		avg /= runs;
		System.out.println("Average packets sent: " + avg);
		System.out.println("Nano seconds per packet: " + (1000000000.0 / avg));
	}
	
	public static void main(String[] args) {
		testSpeed1();
		//testSpeed2();
	}
}
