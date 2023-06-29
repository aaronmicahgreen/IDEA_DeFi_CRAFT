package org.stream.external.handler;
//package org.stream.external.handler;
//
//import java.lang.reflect.InvocationTargetException;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.Set;
//
//import org.core.logger.Logger;
//import org.framework.router.Response;
//import org.properties.Config;
//import org.reflections.Reflections;
//
///**
// * The {@link ExternalStreamManager} is a class which handles all
// * external stream connections and requests. This class contains
// * the functionality to reflect all {@link ExternalStreamConnection}
// * templates stored in {@code org.stream.external.connected.connections}
// * and create streams based on their parameters.
// * <br>
// * All subprocesses sent to the {@link ExternalStreamHandler} interact with this
// * class and are documented as such. All {@link org.framework.router.Response} objects are created
// * in the {@link ExternalStreamHandler}, as this class returns objects that can
// * be interpreted into responses. Please view {@link ExternalStreamHandler} for
// * more information regarding the processes.
// * 
// * @author Conor Flynn
// */
//public class ExternalStreamManager {
//
//	private final ExternalStreamHandler handler;
//	private final HashMap<String, Class<? extends ExternalStreamConnection>> templates;
//	private final HashMap<String, ExternalStreamConnection> streams;
//	
//	/**
//	 * Creates a new {@link ExternalStreamManager} object. Initializes the required
//	 * variables and reflects all classes stored in 
//	 * {@code org.stream.external.connected.connections}. This constructor is called from
//	 * the {@link ExternalStreamHandler} and should not be initialized from any other point.
//	 * 
//	 * @param handler {@link ExternalStreamHandler} object that the new {@link ExternalStreamManager}
//	 * is initialized by.
//	 */
//	protected ExternalStreamManager(ExternalStreamHandler handler) {
//		this.handler = handler;
//		
//		templates = new HashMap<String, Class<? extends ExternalStreamConnection>>();
//		streams = new HashMap<String, ExternalStreamConnection>();
//		
//		// initialize all available connections through reflections
//		try {
//			reflect();
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//	}
//	
//	/**
//	 * Private function which utilizes the {@code org.reflections} library to reflect
//	 * all classes stored in {@code org.stream.external.connected.connections}.
//	 * <br>
//	 * All thrown exceptions are from the reflections library. For more documentation please
//	 * go to the following link: https://github.com/ronmamo/reflections
//	 */
//	private void reflect() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
//		Reflections reflection = new Reflections("org.stream.external.connected.connections");
//		Set<Class<? extends ExternalStreamConnection>> types = reflection.getSubTypesOf(ExternalStreamConnection.class);
//		for(Class<? extends ExternalStreamConnection> c : types)
//			templates.put(c.getDeclaredConstructor(ExternalStreamManager.class, HashMap.class).newInstance(this, new HashMap<String, String>()).getUUID(), c);
//	}
//	
//	/**
//	 * Function used to determine the hash of given {@code data} using an existing template's
//	 * hashing function. Primarily used to predetermine if a stream with the given hash already
//	 * exists in the system so that streams with the same credentials do not get duplicated.
//	 * 
//	 * @param template Type referring to the UUID of the template found in {@link ExternalStreamConnection#getUUID()}.
//	 * @param data Data of the stream used for the authorization of the stream and for the random generation of 
//	 * {@link ExternalStreamConnection#getHash()}. Data should be formatted exactly as specified in the documentation otherwise the
//	 * stream will be unable to authorize.
//	 * @return String containing the hashed {@code data}.
//	 */
//	protected String getHash(String template, HashMap<String, String> data) {
//		if(!templates.containsKey(template))
//			return null;
//		
//		try {
//			return templates.get(template).getDeclaredConstructor(ExternalStreamManager.class, HashMap.class).newInstance(this, new HashMap<String, String>()).getHash(data);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		return null;
//	}
//	
//	/**
//	 * Determines if the given template was reflected on initialization. View
//	 * {@link ExternalStreamHandler} for more information on reflection.
//	 * 
//	 * @param type Type referring to the UUID of the template found in {@link ExternalStreamConnection#getUUID()}.
//	 * @return Boolean determining if the UUID exists within the manager.
//	 */
//	protected boolean containsTemplate(String type) {
//		return templates.containsKey(type);
//	}
//	
//	/**
//	 * Determines if a stream with the given hash exists in the manager. Hashes
//	 * are generated on initialization and should be stored for future stream references.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @return Boolean determining if a stream with the given hash exists within the manager.
//	 */
//	protected boolean containsStream(String hash) {
//		return streams.containsKey(hash);
//	}
//	
//	/**
//	 * Method for retrieving UUID of stream with the given key.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function. 
//	 * @return UUID of the stream with the given hash.
//	 */
//	protected String getStreamType(String hash) {
//		if(!containsStream(hash))
//			return null;
//		
//		return streams.get(hash).getUUID();
//	}
//	
//	/**
//	 * Determines if a stream with the given hash has been successfully authorized.
//	 * <br>
//	 * If a stream with the given hash does not exist, the function returns false.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @return Boolean determining if a stream with the given hash has been successfully authorized.
//	 */
//	protected boolean isStreamAuthorized(String hash) {
//		if(!streams.containsKey(hash))
//			return false;
//		
//		return streams.get(hash).isAuthorized();
//	}
//	
//	/**
//	 * Determines if a stream with the given hash is ready for deployment or a static request.
//	 * <br>
//	 * If a stream with the given hash does not exist, the function returns false.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @return Boolean determining if a stream with the given hash is ready for deployment or static requests.
//	 */
//	protected boolean isStreamReady(String hash) {
//		if(!streams.containsKey(hash))
//			return false;
//		
//		return streams.get(hash).isReady();
//	}
//	
//	/**
//	 * Determines if a stream with the given hash is currently active. Active streams have been successfully
//	 * executed through the {@link ExternalStreamConnection#start()} function.
//	 * <br>
//	 * If a stream with the given hash does not exist, the function returns false.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @return Boolean determining if a stream with the given hash is currently active.
//	 */
//	protected boolean isStreamActive(String hash) {
//		if(!streams.containsKey(hash))
//			return false;
//		
//		return streams.get(hash).isActive();
//	}
//	
//	/**
//	 * Adds a new stream of the given type to the manager. New streams types are generated from the
//	 * reflection of {@code org.stream.external.connected.connections} package. The {@code type} parameter
//	 * refers to the return of the {@link ExternalStreamConnection#getUUID()} function. 
//	 * <br>
//	 * After initialization, the manager will attempt to authorize the stream using the data passed in the
//	 * {@code data} parameter. If failed, the method will return {@code false}. 
//	 * 
//	 * @param type Type of the stream the user wants to initialize. See {@link ExternalStreamConnection#getUUID()} for more information.
//	 * @param data Data of the stream used for the authorization of the stream and for the random generation of 
//	 * {@link ExternalStreamConnection#getHash()}. Data should be formatted exactly as specified in the documentation otherwise the
//	 * stream will be unable to authorize.
//	 * @return The function returns an {@link Object} array containing 2 objects. The first is a {@link Boolean} that determines if
//	 * the action was successful. The second object will return a {@link String} which contains the generated hash of the new stream.
//	 * If the initialization is unsuccessful, the second object will be {@code null}.
//	 */
//	protected Object[] addStream(String type, HashMap<String, String> data) {
//		if(!templates.containsKey(type))
//			return new Object[] {false, null};
//		
//		ExternalStreamConnection stream = null;
//		try {
//			stream = templates.get(type).getDeclaredConstructor(ExternalStreamManager.class, HashMap.class).newInstance(this, data);
//			streams.put(stream.getHash(), stream);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//		
//		if(stream == null)
//			return new Object[] {false, null};
//		
//		return new Object[] {true, stream.getHash()};
//	}
//	
//	/**
//	 * Removes a stream with the given has from the manager. This function is currently deprecated and not used
//	 * however future implementations will include usage for it.
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @return Boolean determining if the removal was successful.
//	 */
//	protected boolean removeStream(String hash) {
//		if(!streams.containsKey(hash))
//			return false;
//		
//		streams.remove(hash);
//		return true;
//	}
//	
//	/**
//	 * Authorizes a stream for execution. Uses the {@code data} passed by the {@link ExternalStreamManager#addStream(String, String)}
//	 * function. That data is then processed in the {@link ExternalStreamConnection#authorize()} function to determine if authorization
//	 * is successful.
//	 * <br>
//	 * This function is explicitly called in {@link ExternalStreamManager#addStream(String, String)}. Failure to authorize successfully
//	 * as determined by {@link ExternalStreamConnection#isAuthorized()} will prevent the new stream from being added.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @return Boolean determining if the stream with the passed hash was successfully authorized.
//	 */
//	protected boolean authorizeStream(String hash) {
//		if(!streams.containsKey(hash))
//			return false;
//		
//		ExternalStreamConnection stream = streams.get(hash);
//		stream.authorize();
//		return stream.isAuthorized();
//	}
//	
//	/**
//	 * Determines if a stream with the given hash contains the given subscription type.
//	 * Utilizes the {@link ExternalStreamConnection#containsSubscriptionType(String)} for
//	 * determining if the subscription exists.
//	 * <br>
//	 * If a stream with the given hash does not exist, this function returns false.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @param type Type of subscription requested.
//	 * @return Boolean determining if the stream with the given hash contains the given subscription type.
//	 */
//	protected boolean containsSubscriptionType(String hash, String type) {
//		if(!streams.containsKey(hash))
//			return false;
//		
//		return streams.get(hash).containsSubscriptionType(type);
//	}
//	
//	/**
//	 * Subscribes the stream to the given subscription type passed in the {@code data}
//	 * parameter. Subscriptions are live data feeds that push to the output handler
//	 * class. From there they are distributed to the according external sources.
//	 * <br>
//	 * If a stream with the given hash does not exist, this function returns false.
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @param data Data required for processing the new subscription.
//	 * @return The function returns an {@link Object} array containing 2 objects. The first is a {@link Boolean} that determines if
//	 * the action was successful. The second item is a {@link String} containing any irregular message given when attempting to subscribe
//	 * to the new subscription. If the action is successful and the first item is true, the second object is null and is not used in the
//	 * given response.
//	 */
//	protected Object[] subscribe(String hash, String data) {
//		if(!streams.containsKey(hash))
//			return new Object[] {false, null};
//		
//		return streams.get(hash).subscribe(data);
//	}
//	
//	/**
//	 * Determines if a stream with the given hash contains the given request type.
//	 * Utilizes the {@link ExternalStreamConnection#containsRequestType(String)} for
//	 * determining if the subscription exists.
//	 * <br>
//	 * If a stream with the given hash does not exist, this function returns false.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @param type Type of request requested.
//	 * @return Boolean determining if the stream with the given hash contains the given request type.
//	 */
//	protected boolean containsRequestType(String hash, String type) {
//		if(!streams.containsKey(hash))
//			return false;
//		
//		return streams.get(hash).containsRequestType(type);
//	}
//	
//	/**
//	 * Sends a data request from the stream with the given hash. This request is in the form of a single
//	 * (typically REST API) request, which will then return a series of data presented.
//	 * <br>
//	 * If a stream with the given hash does not exist, this function returns false.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @param destination Destination of the request to be processed by the {@link ExternalStreamManager#process(String, String, String)} function.
//	 * @param request Request data used for processing the single request.
//	 * @return Returns a string object containing all data returned by the request.
//	 */
//	protected Object[] request(String hash, HashMap<String, String> request) {
//		if(!streams.containsKey(hash))
//			return new Object[] {false, null};
//		
//		ExternalStreamConnection stream = streams.get(hash);
//		if(!stream.isAuthorized() || !stream.isReady())
//			return new Object[] {false, null};
//		
//		return stream.request(request);
//	}
//	
//	/**
//	 * Executes a stream to start processing live data. Live data subscriptions must be called through
//	 * {@link ExternalStreamManager#subscribe(String, String)} which will then add a new data subscription
//	 * to the stream.
//	 * <br>
//	 * If a stream with the given hash does not exist, is not authorized, is not ready, or is already
//	 * executed, this function returns false.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @return Boolean determining if the stream execution is successful.
//	 */
//	protected boolean executeStream(String hash) {
//		if(!streams.containsKey(hash))
//			return false;
//		
//		ExternalStreamConnection stream = streams.get(hash);
//		if(!stream.isAuthorized() || !stream.isReady() || stream.isActive())
//			return false;
//		
//		return stream.start();
//	}
//	
//	/**
//	 * Kills a currently active stream. This function will terminate any connection to the external stream
//	 * and will immediately stop sending data to the output service. Even if the stream is unable to be killed,
//	 * the connection between the data and the output service will be severed.
//	 * <br>
//	 * If a stream with the given hash does not exist or is not active, this function returns false.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @return Boolean determining if the stream was successfully killed.
//	 */
//	protected boolean killStream(String hash) {
//		if(!streams.containsKey(hash))
//			return false;
//		
//		ExternalStreamConnection stream = streams.get(hash);
//		if(!stream.isActive())
//			return false;
//		
//		return stream.stop();
//	}
//	
//	/**
//	 * Function used for processing external data and sending it to the output handler.
//	 * Uses the protocol {@code EDAT} for processing external data.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @param subscription Subscription which the data was received by.
//	 * @param data Data sent by the given subscription.
//	 */
//	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Config.getProperty("app", "general.data.dateformat"));
//	protected void processSubscription(String hash, String subscription, String data) {
//		// define subscribed date
//		String date = LocalDate.now().format(formatter);
//		String collection = subscription + Config.getProperty("app", "general.collection.delim") + date;
//		Response lsh_response = handler.send("LSH", "PUSH", "data", data, "collection", collection);
//		if(lsh_response.code() != 200)
//			Logger.warn(lsh_response);
//	}
//	
//	/**
//	 * Function used for processing external data and sending it to the output handler.
//	 * Uses the protocol {@code EDAT} for processing external data.
//	 * 
//	 * @param hash Hash of the stream returned by the {@link ExternalStreamConnection#getHash(String)} function.
//	 * @param request Request which the data was received by.
//	 * @param data Data sent by the given subscription.
//	 */
//	protected void processRequest(String collection, String data) {
//		Response lsh_response = handler.send("LSH", "PUSH", "collection", collection, "data", data);
//		if(lsh_response.code() != 200)
//			Logger.warn(lsh_response);
//	}
//}
