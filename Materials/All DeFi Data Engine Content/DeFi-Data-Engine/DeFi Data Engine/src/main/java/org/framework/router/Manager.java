package org.framework.router;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

/**
 * The {@link Manager} class is used to handle all {@link Router} connections.
 * {@link Manager} objects have the ability to merge and combine networks of
 * {@link Router} objects efficiently and effectively.
 * 
 * @author Conor Flynn
 *
 */
public class Manager {

	private final String uuid;
	private final HashMap<String, Router> routers;
	
	/**
	 * Private constructor used to create a new {@link Manager} object.
	 * Used by {@link Manager#create(Router)} to connect the newly created
	 * object to a {@link Router}.
	 */
	private Manager() {
		uuid = UUID.randomUUID().toString();
		routers = new HashMap<String, Router>();
	}
	
	/**
	 * Uniquely generated UUID created on object initialization.
	 * 
	 * @return String representing the UUID of the object.
	 */
	public String getUUID() {
		return uuid;
	}
	
	/**
	 * String representation of the {@link Manager} object.
	 * 
	 * @return UUID of the object. See {@link Manager#getUUID()} for more information.
	 */
	public String toString() {
		return getUUID();
	}
	
	/**
	 * Connects a {@link Router} object to the {@link Manager} object. Allows it to send
	 * {@link Packet} object's to any {@link Router} on the network through {@link Manager#send(Packet)}.
	 * 
	 * @param router {@link Router} object to connect to the {@link Manager} object.
	 */
	protected void connect(Router router) {
		if(isConnected(router.getTag())) 
			return;
		
		routers.put(router.getTag(), router);
	}
	
	/**
	 * Disconnects a {@link Router} object from the {@link Manager} object. Removes access from
	 * sending any {@link Packet} object's to any {@link Router} connected to the network.
	 * 
	 * @param router {@link Router} object to disconnect from the {@link Manager} object.
	 */
	protected void disconnect(Router router) {
		if(!isConnected(router.getTag()))
			return;
		
		routers.remove(router.getTag());
	}
	
	/**
	 * Checks to see if a {@link Router} object with the specified tag is connected
	 * to the network.
	 * 
	 * @param tag Tag of the {@link Router} object to determine if it is connected to the network.
	 * @return Boolean determining if a {@link Router} with the given tag exists on the network.
	 */
	public boolean isConnected(String tag) {
		return routers.containsKey(tag);
	}
	
	/**
	 * Collection of all {@link Router} objects connected to the {@link Manager} object.
	 * 
	 * @return Collection of all {@link Router} objects stored within the {@link Manager}.
	 */
	public Collection<Router> connected() {
		return routers.values();
	}
	
	/**
	 * Collection of all {@link Router} object tags connected to the {@link Manager} object.
	 * 
	 * @return Collection of all {@link Router} object tags stored within the {@link Manager}.
	 */
	protected Collection<String> tags() {
		return routers.keySet();
	}
	
	/**
	 * Sends a {@link Packet} object between two {@link Router} objects stored in the network. Sent
	 * packets are required to return a {@link Response} to the sender that determines the result
	 * of the sent {@link Packet}.
	 * 
	 * @param packet {@link Packet} object to send to the {@link Router}.
	 * @return {@link Response} object returned from the receiver determining the state of the action performed
	 * by the sent packet.
	 */
	public Response send(Packet packet) {
		if(!routers.containsKey(packet.getTag()))
			return ResponseFactory.response404(packet.getSender(), packet.getTag());
		
		return routers.get(packet.getTag()).receive(packet);
	}
	
	/**
	 * Static function used to create a new {@link Manager} object. Called by a {@link Router}
	 * object when necessary. Automatically connects the passed {@link Router} to the {@link Manager}
	 * upon initialization.
	 * 
	 * @param router {@link Router} object that creates the {@link Manager} and then automatically connects to it.
	 * @return New {@link Manager} object with the parameterized {@code router} object connected.
	 */
	protected static Manager create(Router router) {
		Manager manager = new Manager();
		manager.connect(router);
		return manager;
	}
}