package org.framework.router;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The Router is a super class that every process inherits. It is used to route data in a
 * standardized manner throughout the engine. Each process that inherits the Router super class
 * will be required to supply several types of information. See documentation for further
 * details.
 * 
 * @author Conor Flynn
 *
 */
public abstract class Router {

	private final String uuid;
	private final String tag;
	private Manager manager;
	private final HashMap<String, Method> processes;
	
	/**
	 * Initializes the Router object to handle processing packets. Router
	 * does not have any contained tags other than identifying tag.
	 *  
	 * @param uuid Unique identifier of the inheriting process.
	 * @param tag Unique tag of the inheriting process.
	 */
	public Router(String uuid, String tag) {
		this(uuid, tag, null);
	}
	
	/**
	 * Initializes the Router object to handle processing {@link Packet} objects.
	 *  
	 * @param uuid Unique identifier of the inheriting process.
	 * @param tag Unique tag of the inheriting process.
	 * @param manager {@link Manager} object which determines the network of processes the router is connected to.
	 * 
	 * @throws IllegalArgumentException Thrown if {@link Router} object's tag already exists within the
	 * contained_tags list passed in the constructor.
	 */
	public Router(String uuid, String tag, Manager manager) {
		this.uuid = uuid;
		this.tag = tag;
		this.manager = manager;
		this.processes = new HashMap<String, Method>();
		
		try {
			defineProcesses();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Unique identifier of the inheriting process.
	 * 
	 * @return String representation of UUID.
	 */
	public final String getUUID() {
		return uuid;
	}
	
	/**
	 * Unique tag of the inheriting process.
	 * 
	 * @return Unique 3 character string representing tag.
	 */
	public final String getTag() {
		return tag;
	}
	
	/**
	 * {@link Manager} that this {@link Router} is connected to.
	 * 
	 * @return {@link Manager} object of the {@link Router}.
	 */
	public final Manager getManager() {
		return manager;
	}
	
	/**
	 * Updates the {@link Manager} this {@link Router} is connected to.
	 * Disconnects from the old {@link Manager} as well if it is not
	 * {@code null}.
	 * 
	 * @param manager {@link Manager} object to connect this {@link Router} to.
	 */
	protected final void setManager(Manager manager) {
		// disconnect from previous manager if non-null
		if(this.manager != null)
			this.manager.disconnect(this);
		
		// update new manager and connect
		this.manager = manager;
		this.manager.connect(this);
	}
	
	/**
	 * Connects all passed {@link Router} objects to this {@link Router} object's {@link Manager}.
	 * Merges all {@link Router} objects within both networks such that they can all communicate with
	 * each other.
	 * <br>
	 * Creates a new {@link Manager} object for this {@link Router} if it is {@code null}.
	 * 
	 * @param routers {@link Router} objects to connect to this {@link Router} object's network.
	 */
	public final void connect(Router... routers) {
		// connect all routers in list to current manager
		// if manager is null then create new manager
		if(manager == null)
			manager = Manager.create(this);
		
		HashSet<Router> network = new HashSet<Router>();
		
		for(Router router : routers) {
			if(router.manager == null)
				network.add(router);
			else
				for(Router managed : router.manager.connected())
					network.add(managed);
		}
		
		for(Router router : network)
			router.setManager(manager);
	}
	
	/**
	 * Determines if a {@link Router} object with the passed tag
	 * exists on the network.
	 * 
	 * @param tag Tag of the {@link Router} object to search for.
	 * @return Boolean determining if {@link Router} object with passed tag exists.
	 */
	public final boolean isConnected(String tag) {
		return manager.isConnected(tag);
	}
	
	/**
	 * Determines if a {@link Router} object exists on the network. Uses
	 * the passed {@link Router} object's tag to determine existence and references
	 * {@link Router#isConnected(String)}.
	 * 
	 * @param router {@link Router} object to search for.
	 * @return Boolean determining if {@link Router} object exists.
	 */
	public final boolean isConnected(Router router) {
		return isConnected(router.tag);
	}
	
	/**
	 * Collection of all {@link Router} object's tags that are connected to the network.
	 * See {@link Router#getTag()} for more information.
	 * 
	 * @return Collection of all connected {@link Router} object's tags.
	 */
	protected final Collection<String> connectedTags() {
		return manager.tags();
	}
	
	/**
	 * Function used to send a {@link Packet} object to the desired destination.
	 * 
	 * @param tag Tag Tag of the destination's {@link Router} object.
	 * @param sub_tag Sub tag describing the action performed at the destination.
	 * @param data Data transmitted through the {@link Packet} for processing at the destination.
	 * @return Integer representing the return code of the sent {@link Packet}.
	 */
	public final Response send(String tag, String sub_tag, HashMap<String, String> data) {
		if(manager == null)
			return ResponseFactory.response400(this.getTag());
		
		Packet packet = Packet.packet(this, tag, sub_tag, data);
		if(packet == null)
			return ResponseFactory.response407(this.tag, tag, sub_tag, data.toString());
		
		return manager.send(packet);
	}
	
	/**
	 * Function used to send a {@link Packet} object to the desired destination.
	 * 
	 * @param tag Tag Tag of the destination's {@link Router} object.
	 * @param sub_tag Sub tag describing the action performed at the destination.
	 * @param data Data transmitted through the {@link Packet} for processing at the destination.
	 * @param sub_data Supporting data used for processing and transmitting from {@code data}. This parameter is optional.
	 * @return Integer representing the return code of the sent {@link Packet}.
	 */
	public final Response send(String tag, String sub_tag, String... data) {
		if(manager == null)
			return ResponseFactory.response400(this.getTag());
		
		// create packet and push to receive method
		Packet packet = Packet.packet(this, tag, sub_tag, data);
		if(packet == null)
			return ResponseFactory.response407(this.tag, tag, sub_tag, data);
			
		return manager.send(packet);
	}
	
	/**
	 * Function used for receiving {@link Packet} objects and determining whether
	 * to route them to a connected {@link Router} or to process them through the
	 * {@link Router#process(Packet)} function.
	 * 
	 * @param packet {@link Packet} object received by the {@link Router}.
	 * @return Integer representing the return code of the sent {@link Packet}.
	 */
	public final Response receive(Packet packet) {
		return process(packet);
	}
	
	/**
	 * Adds a process to the {@link Router} object under the given {@code subtag}.
	 * When the {@link Router} object receives a {@link Packet} with the given
	 * {@code subtag}, it will auto route the {@link Packet} to the stored method.
	 * <br>
	 * All {@link Method} objects must contain a single parameter, a {@link Packet} object,
	 * and return a {@link Response} object.
	 * 
	 * @param sub_tag Subtag of the process to handle the incoming {@link Packet} object.
	 * @param method {@link Method} to pass the {@link Packet} object to.
	 */
	public final void addProcess(String sub_tag, Method method) {
		processes.put(sub_tag, method);
	}
	
	/**
	 * Function used to handle incoming {@link Packet} objects.
	 * <br>
	 * Returns a 405 response code should the {@link Router} not support the
	 * given {@link Packet#getSubTag()} process. 
	 * 
	 * @param packet {@link} Packet object to be processed.
	 * @return Integer representing the return code of the sent {@link Packet}
	 */
	private final Response process(Packet packet) {
		if(!processes.containsKey(packet.getSubTag()))
			return ResponseFactory.response405(this.getTag(), packet.getSubTag());
		
		try {
			return (Response)processes.get(packet.getSubTag()).invoke(this, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ResponseFactory.response410(this.getTag(), packet.getSubTag());
	}
	
	/**
	 * Defines all processes used within the {@link Router}. All added processes must contain
	 * the explicit subtag they are listed under and the associated method to handle the subtag from.
	 */
	private final void defineProcesses() throws NoSuchMethodException, SecurityException {
		Method[] methods = getClass().getMethods();
		for(Method method : methods) {
			if(!method.getName().contains("process"))
				continue;
			
			method.setAccessible(true);
			addProcess(method.getName().replace("process", ""), method);
		}
	}
}
