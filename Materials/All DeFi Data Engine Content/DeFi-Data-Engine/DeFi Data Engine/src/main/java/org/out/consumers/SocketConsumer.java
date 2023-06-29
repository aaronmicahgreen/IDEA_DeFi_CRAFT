package org.out.consumers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;

import org.framework.router.Response;
import org.json.JSONObject;
import org.out.handler.OutputConsumer;
import org.out.handler.OutputManager;
import org.out.socket.SocketManager;
import org.properties.Config;

public class SocketConsumer extends OutputConsumer {

	private Thread listener;
	
	public SocketConsumer(OutputManager manager) {
		super(manager);
	}
	
	public String getUUID() {
		return "socket_consumer";
	}
	
	@Override
	protected boolean init() {
		int port = Integer.parseInt(Config.getProperty("stream", "rest.socket.port"));
		
		// create server
		if(!SocketManager.createServer(port))
			return false;
		
		// accept inflow from REST
		final String key = Config.getProperty("stream", "rest.socket.key");
		
		if(!SocketManager.accept(port, key))
			return false;
		
		listener = new Thread() {
			public void run() {
				try {
					DataInputStream in = SocketManager.read(key);
					DataOutputStream out = SocketManager.write(key);
					
					// listen for data packets from rest socket
					while(true) {
						String[] input = ((String)in.readUTF()).split(Config.getProperty("app", "general.transfer.delim"));
						
						Thread thread = new Thread() {
							public void run() {
								try {
									// validate length is greater than 2
									if(input.length <= 2) {
										out.writeUTF(new JSONObject()
												.put("response", "502")
												.put("message", "Packet processed from REST API does not contain a TAG or SUB_TAG. Review REST API endpoint code.")
												.toString());
									}
									
									// extract non-essential data
									String[] data = Arrays.copyOfRange(input, 2, input.length);
									String tag = input[0];
									String sub_tag = input[1];
									
									// execute valid response to engine
									Response response = send(tag, sub_tag, data);
									out.writeUTF(new JSONObject()
											.put("response", "200")
											.put("code", response.code())
											.put("message", response.message())
											.put("data", response.data())
											.toString());
									out.flush();
									
								} catch(SocketException e) {
									System.err.println("Rest Application has unexpectedly closed.");
									System.exit(1);
								} catch (IOException e) {
									e.printStackTrace();
									System.exit(1);
								}
							}
						};
						
						thread.start();
					}
				} catch(Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		};
		
		return true;
	}
	
	@Override
	protected boolean listen() {
		// server not initialized
		if(listener == null)
			return false;
		
		listener.start();
		return true;
	}
	
	@Override
	protected boolean kill() {
		listener.interrupt();
		return true;
	}
}
