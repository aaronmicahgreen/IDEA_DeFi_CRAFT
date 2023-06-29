package org.core.core;

import org.core.engine.Engine;
import org.core.logger.Logger;
import org.framework.router.Response;
import org.framework.router.Router;
import org.out.controller.Controller;
import org.out.handler.OutputHandler;
import org.stream.manager.StreamManager;

public class Core extends Router {

	public Core() {
		super("core", "COR");
		
		OutputHandler out = new OutputHandler();
		Controller crl = new Controller();
		Engine eng = new Engine();
		StreamManager str = new StreamManager();
		
		this.connect(out, crl, eng, str);
		
		Response response = this.send("ENG", "STRT");
		if(response.code() != 200)
			Logger.terminate(response);
	}
}