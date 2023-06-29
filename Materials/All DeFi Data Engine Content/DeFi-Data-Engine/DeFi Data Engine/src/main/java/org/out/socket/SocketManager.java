package org.out.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.core.logger.Logger;
import org.framework.router.Response;
import org.json.JSONObject;
import org.out.producers.SocketProducer;
import org.properties.Config;

public class SocketManager {

	private static final HashMap<Integer, ServerSocket> servers = new HashMap<Integer, ServerSocket>();
	private static final HashMap<String, Socket> connections = new HashMap<String, Socket>();
	private static final HashMap<String, HashSet<String>> registry = new HashMap<String, HashSet<String>>();
	private static final HashMap<String, DataInputStream> inflow = new HashMap<String, DataInputStream>();
	private static final HashMap<String, DataOutputStream> outflow = new HashMap<String, DataOutputStream>();
	
	private static final long HEARTBEAT_OFFSET = 5000L;
	
	public synchronized static boolean createServer(int port) {
		if(servers.containsKey(port))
			return true;
		
		try {
			ServerSocket server = new ServerSocket(port);
			servers.put(port, server);
			registry.put(server.getInetAddress().toString(), new HashSet<String>());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public synchronized static boolean exists(String key) {
		return connections.containsKey(key);
	}
	
	public synchronized static void createThread(String key, SocketProducer producer) {
		Thread thread = new Thread() {
			public void run() {
				// perform logger verifications
				Logger.log(String.format("Starting thread for Socket with key <%s>", key));
				if(!inflow.containsKey(key)) {
					Logger.terminate(String.format("Key <%s> not found within inflow thread configuration, manual review recommended.", key));
					return;
				}
				
				if(!outflow.containsKey(key)) {
					Logger.terminate(String.format("Key <%s> not found within outflow thread configuration, manual review recommended.", key));
					return;
				}
				
				// retrieve inflow stream and listen
				DataInputStream in = inflow.get(key);
				DataOutputStream out = outflow.get(key);
				String str;
				while(true) {
					try {
						str = readLine(in);
						
						// parse input
						String[] input = str.split(Config.getProperty("app", "general.transfer.delim"));
						
						// validate input
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
						
						// retrieve destination
						String temp_destination = "";
						for(int i = 0; i < data.length; i++) {
							if(data[i].equals("destination") && data.length - 1 != i)
								temp_destination = data[i + 1];
						}
						
						// if no destination found then continue
						if(temp_destination.equals(""))
							continue;
						
						final String destination = temp_destination;
						
						// create heartbeat connection
						Thread heartbeat = new Thread() {
							public void run() {
								while(true) {
									try {
										Thread.sleep(HEARTBEAT_OFFSET);
										producer.send("OUT", "EDAT", 
												"data", "<<<heartbeat>>>",
												"destination", destination);
									} catch(Exception e) {
										Logger.log(String.format("Heartbeat connection terminated for Socket with key <%s>", key));
										break;
									}
								}
							}
						};
						
						// start heartbeat
						heartbeat.start();
						
						// execute valid response to engine
						Response response = producer.send(tag, sub_tag, data);
	
						// send response signifier
						producer.send("OUT", "EDAT", 
								"data", "<<<response>>>",
								"destination", destination);
						// send response details
						producer.send("OUT", "EDAT", 
								"data", new JSONObject()
								.put("response", "200")
								.put("code", response.code())
								.put("message", response.message())
								.put("data", response.data())
								.toString(), 
								"destination", destination);
						
						// terminate heartbeat
						try {
							heartbeat.interrupt();
						} catch(Exception e) {}
						
					} catch(Exception e) {
						break;
					}
				}
				
				Logger.log(String.format("Terminating thread for Socket with key <%s>", key));
			}
		};
		
		thread.start();
	}
	
	// used for generic channel accepting
	public synchronized static String accept(int port, SocketProducer producer) {
		if(!servers.containsKey(port))
			if(!createServer(port))
				return null;
		
		try {
			// accept new connection
			Socket connection = servers.get(port).accept();
			
			// validate new connection:
			DataInputStream in = new DataInputStream(connection.getInputStream());
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			String key = UUID.randomUUID().toString();
			out.write(key.getBytes());
			out.write(10);
			
			Logger.log("Successfully connected to external socket. Key <" + key + ">");
			
			if(connections.containsKey(key))
				connections.get(key).close();
			
			connections.put(key, connection);
			inflow.put(key, in);
			outflow.put(key, new DataOutputStream(connection.getOutputStream()));
			registry.get(servers.get(port).getInetAddress().toString()).add(key);
			
			if(!synced(key))
				throw new Exception("Connection inflow and outflow not synchronized");
			
			// start internal thread for socket information parsing
			createThread(key, producer);
			
			return key;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// used for reserved channel accepting
	public synchronized static boolean accept(int port, String required_key) {
		if(!servers.containsKey(port))
			if(!createServer(port))
				return false;
		
		try {
			// accept new connection
			Socket connection = servers.get(port).accept();
			// validate new connection:
			DataInputStream in = new DataInputStream(connection.getInputStream());
			
			String key = in.readUTF();
			
			if(!required_key.equals(key)) {
				connection.close();
				return false;
			}
			
			if(connections.containsKey(key))
				return false;
			
			connections.put(key, connection);
			inflow.put(key, in);
			outflow.put(key, new DataOutputStream(connection.getOutputStream()));
			registry.get(servers.get(port).getInetAddress().toString()).add(key);
			
			if(!synced(key))
				throw new Exception("Connection inflow and outflow not synchronized.");

			Logger.log("Successfully connected to reserved socket. Key <" + key + ">");
			
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static DataOutputStream write(String key) {
		if(!exists(key))
			return null;
		
		return outflow.get(key);
	}
	
	public static DataInputStream read(String key) {
		if(!exists(key))
			return null;
		
		return inflow.get(key);
	}
	
	private static boolean synced(String key) {
		return connections.containsKey(key) && inflow.containsKey(key) && outflow.containsKey(key);
	}
	
	private static final String readLine(DataInputStream in) throws IOException {
		StringBuilder out = new StringBuilder();
		char c = 0;
		while((c = (char)in.read()) != 10) {
			if(out.length() > 200_000)
				break;
			out.append(c);
		}
		return out.toString();
	}
}
