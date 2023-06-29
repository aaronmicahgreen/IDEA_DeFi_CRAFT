package org.stream.local.handler;

import java.util.Set;
import org.framework.router.Packet;
import org.framework.router.Response;
import org.framework.router.ResponseFactory;
import org.framework.router.Router;
import org.properties.Config;

public class LocalStreamHandler extends Router {

	private final LocalStreamManager manager;
	
	public LocalStreamHandler() {
		super("local_stream_handler", "LSH");
		this.manager = new LocalStreamManager(this);
	}
	
	public Response processINIT(Packet packet) {
		String validate;
		if((validate = packet.validate("source")) != null)
			return ResponseFactory.response500("LocalStreamHandler", validate);
		
		String source = packet.getData("source");

		if(!manager.containsTemplate(source))
			return ResponseFactory.response440(source);
		
		if(manager.isStreamDefined())
			return ResponseFactory.response443(source);
		
		if(!manager.setStream(source))
			return ResponseFactory.response442(source);
		
		if(!manager.authorize() || !manager.isAuthorized())
			return ResponseFactory.response444(source);
		
		if(!manager.isReady())
			return ResponseFactory.response441(source);
		
		return ResponseFactory.response200();
	}
	
	public Response processSCAN(Packet packet) {
		String validate;
		if((validate = packet.validate("query")) != null)
			return ResponseFactory.response500("LocalStreamHandler", validate);
		
		if(!manager.isReady())
			return ResponseFactory.response441(manager.streamType());
		
		String[] query = packet.getData("query").split(Config.getProperty("stream", "mongodb.query.delim"));
		
		if(!manager.validate(query))
			return ResponseFactory.response445(manager.streamType(), packet.getData("query"));
		
		if(manager.scan(query))
			return ResponseFactory.response200("true");
		
		return ResponseFactory.response200("false");
	}
	
	//public Response process
	public Response processRQST(Packet packet) {
		String validate;
		if((validate = packet.validate("type", "destination")) != null)
			return ResponseFactory.response500("LocalStreamHandler", validate);
		
		String request = packet.getData("query");
		
		String[] query = new String[] {"get_all", request};
		
		if(request == null || request.isEmpty())
			return ResponseFactory.response501("Request was null when attempting to process.");
		
		if(!manager.validate(query))
			return ResponseFactory.response445(manager.streamType(), packet.getData("query"));

		if(!manager.scan(query))
			return ResponseFactory.response446(manager.streamType(), packet.getData("query"));
		
		Set<String> output = manager.get(query);
		
		if(output == null)
			return ResponseFactory.response447(manager.streamType(), packet.getData("query"));
		
		Response response;
		for(String line : output) {
			response = send("SRC", "EDAT", "data", line, "destination", packet.getData("destination"));
			if(response.code() != 200)
				return response;
		}
		
		return ResponseFactory.response200();
	}
	
	public Response processSTAT(Packet packet) {
		String validate;
		if((validate = packet.validate("query")) != null)
			return ResponseFactory.response500("LocalStreamHandler", validate);
		
		if(!manager.isReady())
			return ResponseFactory.response441(manager.streamType());
		
		String[] query = packet.getData("query").split(Config.getProperty("stream", "mongodb.query.delim"));
		
		if(!manager.validate(query))
			return ResponseFactory.response445(manager.streamType(), packet.getData("query"));
		
		if(!manager.scan(query))
			return ResponseFactory.response446(manager.streamType(), packet.getData("query"));
		
		DataState state = manager.state(query);
		if(state == DataState.INVALID)
			return ResponseFactory.response448(manager.streamType(), packet.getData("query"));
		
		return ResponseFactory.response200(state.toString());
	}
	
	public Response processPUSH(Packet packet) {
		// data format: data, location...
		String validate;
		if((validate = packet.validate("data", "collection")) != null)
			return ResponseFactory.response500("LocalStreamHandler", validate);
		
		if(!manager.isReady())
			return ResponseFactory.response441(manager.streamType());
		
		if(!manager.push(packet.getData("data"), packet.getData("collection")))
			return ResponseFactory.response449(manager.streamType(), packet.getData("data"), packet.getData("collection"));
		
		return ResponseFactory.response200();
	}
}
