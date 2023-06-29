package org.out.destinations;

import java.io.DataOutputStream;
import java.io.IOException;

import org.framework.router.Packet;
import org.json.JSONException;
import org.out.handler.OutputDestination;

public class SocketDestination extends OutputDestination {

	private final DataOutputStream out;
	
	public SocketDestination(String key, DataOutputStream out) {
		super(key);
		
		this.out = out;
	}
	
	public final synchronized boolean send(Packet packet) {
		try {
			out.write(packet.getData("data").getBytes());
			out.write(10);
			out.flush();
		} catch (JSONException | IOException e) {
			return false;
		}
		
		return true;
	}
}
