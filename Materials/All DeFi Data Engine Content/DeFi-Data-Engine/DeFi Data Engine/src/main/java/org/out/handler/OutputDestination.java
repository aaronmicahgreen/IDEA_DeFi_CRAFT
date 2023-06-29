package org.out.handler;

import org.framework.router.Packet;

public abstract class OutputDestination {

	private final String key;
	
	public OutputDestination(String key) {
		this.key = key;
	}
	
	public final String getKey() {
		return key;
	}
	
	public abstract boolean send(Packet packet);
}
