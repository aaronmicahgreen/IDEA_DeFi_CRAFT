package org.out.producers;

import org.framework.router.Response;
import org.out.destinations.SocketDestination;
import org.out.handler.OutputManager;
import org.out.handler.OutputProducer;
import org.out.socket.SocketManager;
import org.properties.Config;

public class SocketProducer extends OutputProducer {

	private Thread listener;
	public final SocketProducer producer = this;
	
	public SocketProducer(OutputManager manager) {
		super(manager);
	}

	@Override
	public String getUUID() {
		return "socket_producer";
	}

	@Override
	protected boolean init() {
		listener = new Thread() {
			public void run() {
				while(true) {
					String key = SocketManager.accept(Integer.parseInt(Config.getProperty("stream", "output.socket.port")), producer);
					if(key == null) {
						System.err.println("SocketProducer: Could not create connection to socket port.");
						System.exit(1);
					}
					manager.add(new SocketDestination(key, SocketManager.write(key)));
				}
			}
		};
		
		return true;
	}

	@Override
	protected boolean listen() {
		if(listener == null)
			return false;
		
		listener.start();
		return true;
	}

	@Override
	protected boolean kill() {
		try {
			listener.interrupt();
		} catch(Exception e) {}
		
		return true;
	}
	
	public Response send(String tag, String sub_tag, String... data) {
		return manager.send(tag, sub_tag, data);
	}
}
