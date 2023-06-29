package org.stream.external.handler;

import org.framework.router.Packet;
import org.framework.router.Response;
import org.framework.router.ResponseFactory;
import org.framework.router.Router;

public final class ExternalStreamHandler extends Router {

	private final ExternalStreamManager manager;
	
	public ExternalStreamHandler() {
		super("external_stream_handler", "ESH");
		
		manager = new ExternalStreamManager(this);
	}
	
	// type: type of data
	public Response processEXSR(Packet packet) {
		String validate;
		if((validate = packet.validate("type")) != null)
			return ResponseFactory.response500("ExternalStreamHandler", validate);
		
		return ResponseFactory.response200(String.format("%s", manager.containsType(packet.getData("type"))));
	}
	
	// type: 			type of data
	// url_path (opt): 	path of the url
	// properties (opt):properties required for call
	// headers (opt):	headers required for call
	public Response processRQST(Packet packet) {
		String validate;
		if((validate = packet.validate("type")) != null)
			return ResponseFactory.response500("ExternalStreamHandler", validate);
		
		// validate type exists
		if(!manager.containsType(packet.getData("type")))
			return ResponseFactory.response420(packet.getData("type"));
		
		Object[] response;
		
		// check to see if dated
		// if not
		if((validate = packet.validate("start_date", "end_date")) != null)
			response = manager.request(packet.getData("type"), packet.getData());
		
		// if dated
		else
			response = manager.request(packet.getData("type"), packet.getData(), packet.getData("start_date"), packet.getData("end_date"));
		
		// check to see if valid
		if(!((boolean)response[0])) {
			return ResponseFactory.response427(packet.getData("type"), response[1].toString());
		}
		
		// return valid
		return ResponseFactory.response200();
	}
}