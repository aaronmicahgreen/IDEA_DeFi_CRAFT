package org.out.handler;

import org.framework.interfaces.UUID;
import org.framework.router.Response;

public abstract class OutputProducer implements UUID {

	protected final OutputManager manager;
	
	public OutputProducer(OutputManager manager) {
		this.manager = manager;
	}
	
	protected final Response send(String tag, String sub_tag, String data) {
		return manager.send(tag, sub_tag, data);
	}
	
	protected abstract boolean init();
	protected abstract boolean listen();
	protected abstract boolean kill();
}