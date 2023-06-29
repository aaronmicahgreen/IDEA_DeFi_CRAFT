package org.core.engine;

import org.framework.router.Packet;
import org.framework.router.Response;
import org.framework.router.ResponseFactory;
import org.framework.router.Router;
import org.properties.Config;

public class Engine extends Router {

	public Engine() {
		super("engine", "ENG");
	}
	
	// source: source of the local stream to initialize
	public Response processSTRT(Packet packet) {
		// start output processes:
		Response out_response = send("OUT", "STRT");
		if(out_response.code() != 200)
			return out_response;
		
		// start local stream handler processes:
		String lsh_type = Config.getProperty("stream", "local.stream.type");
		if(!lsh_type.equals("null")) {
			Response lsh_response = send("LSH", "INIT", "source", lsh_type);
			if(lsh_response.code() != 200)
				return lsh_response;			
		}

		return ResponseFactory.response200();
	}
}
