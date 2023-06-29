package org.out.handler;

import org.framework.router.Packet;
import org.framework.router.Response;
import org.framework.router.ResponseFactory;
import org.framework.router.Router;

public class OutputHandler extends Router {
	
	private final OutputManager manager;
	
	public OutputHandler() {
		super("output_handler", "OUT");
		
		manager = new OutputManager(this);
	}
	
	public Response processSTRT(Packet packet) {
		// activate all output connections
		try {
			Object[] consumer_response = manager.consumerListen();
			if(!(boolean)consumer_response[0])
				return ResponseFactory.response460(consumer_response[1].toString());
			
			Object[] producer_response = manager.producerListen();
			if(!(boolean)producer_response[0])
				return ResponseFactory.response470(producer_response[1].toString());
	
			return ResponseFactory.response200();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return ResponseFactory.response501();
	}
	
	public Response processEDAT(Packet packet) {
		String destination = packet.getData("destination");
		if(destination == null)
			return ResponseFactory.response500("OutputHandler", "destination");
		
		if(destination.equals("null"))
			return ResponseFactory.response200();
			
		if(!manager.containsDestination(destination))
			return ResponseFactory.response471(destination);
		
		if(!manager.send(destination, packet))
			return ResponseFactory.response472(destination);
		
		return ResponseFactory.response200();
	}
}