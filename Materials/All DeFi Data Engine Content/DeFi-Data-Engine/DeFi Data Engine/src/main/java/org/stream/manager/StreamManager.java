package org.stream.manager;

import org.framework.router.Router;
import org.stream.external.handler.ExternalStreamHandler;
import org.stream.local.handler.LocalStreamHandler;
import org.stream.registry.StreamRegistryController;

public class StreamManager extends Router {

	public StreamManager() {
		super("stream_manager", "STR");
		
		StreamRegistryController src = new StreamRegistryController();
		ExternalStreamHandler esh = new ExternalStreamHandler();
		LocalStreamHandler hsh = new LocalStreamHandler();
		
		connect(src, esh, hsh);
	}
}
