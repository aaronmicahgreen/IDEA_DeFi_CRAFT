package org.framework.router;

import java.util.HashMap;

import org.core.logger.Logger;
import org.properties.Config;

/**
 * The {@link Packet} class represents a standardized data transfer object
 * used throughout the engine. It contains a series of values which help to
 * route it to different processes. This class interacts heavily with the
 * {@link Router} class.
 * 
 * @author Conor Flynn
 *
 */
public class Packet {
	
	private final static boolean log = Config.getProperty("app", "general.logging.packets").equals("true");

	private final String sender;
	private final String tag;
	private final String sub_tag;
	private final HashMap<String, String> data;
	
	/**
	 * Initializes a new {@link Packet} object.
	 * 
	 * @param router {@link Router} object the {@link Packet} was sent from.
	 * @param tag Tag of the destination the {@link Packet} will be sent to.
	 * @param sub_tag Sub tag describing the action performed at the destination.
	 * @param data Data transmitted through the {@link Packet} for processing at the destination.
	 */
	private Packet(Router router, String tag, String sub_tag, HashMap<String, String> data) {
		this.sender = router.getTag();
		this.tag = tag;
		this.sub_tag = sub_tag;
		this.data = data;
	}
	
	/**
	 * Tag of the {@link Router} object that sent the {@link Packet}.
	 * 
	 * @return Tag of the sending {@link Router} object.
	 */
	public final String getSender() {
		return sender;
	}
	
	/**
	 * Tag of the destination the {@link Packet} will be sent to.
	 * 
	 * @return Tag of the {@link Router} the {@link Packet} is being sent to.
	 */
	public final String getTag() {
		return tag;
	}
	
	/**
	 * Sub tag determining the action of the {@link Packet} at the destination.
	 * 
	 * @return Sub tag of the {@link Packet} object.
	 */
	public final String getSubTag() {
		return sub_tag;
	}
	
	public final boolean containsKey(String key) {
		return data.containsKey(key);
	}
	
	/**
	 * Data transmitted through the {@link Packet} for processing at the destination.
	 * 
	 * @return String containing all data sent.
	 */
	public final HashMap<String, String> getData() {
		return data;
	}
	
	/**
	 * Retrieves data point stored within {@link Packet}.
	 * 
	 * @param key Key that the data point is stored under.
	 * @return {@link String} containing data stored.
	 */
	public final String getData(String key) {
		if(data.containsKey(key))
			return data.get(key);
		
		return null;
	}
	
	public final String validate(String... keys) {
		if(keys == null)
			return null;
		
		for(String key : keys)
			if(!data.containsKey(key) || data.get(key).equals(""))
				return key;
		
		return null;
	}
	
	/**
	 * Factory method used to create a {@link Packet} object.
	 * 
	 * @param router {@link Router} object the {@link Packet} was sent from.
	 * @param tag Tag of the destination the {@link Packet} will be sent to.
	 * @param sub_tag Sub tag describing the action performed at the destination.
	 * @param data Data transmitted through the {@link Packet} for processing at the destination.
	 * @return New {@link Packet} object.
	 */
	public static Packet packet(Router router, String tag, String sub_tag, HashMap<String, String> data) {
		if(data == null)
			return null;
		
		Packet packet = new Packet(router, tag, sub_tag, data);
		if(log)
			Logger.log(packet);
		return packet;
	}
	
	/**
	 * Factory method used to create a {@link Packet} object.
	 * 
	 * @param router {@link Router} object the {@link Packet} was sent from.
	 * @param tag Tag of the destination the {@link Packet} will be sent to.
	 * @param sub_tag Sub tag describing the action performed at the destination.
	 * @param data Data transmitted through the {@link Packet} for processing at the destination.
	 * @param sub_data Supporting data used for processing and transmitting from {@code data}.
	 * @return New {@link Packet} object.
	 */
	public static Packet packet(Router router, String tag, String sub_tag, String... data) {
		if(data.length % 2 != 0)
			return null;
		
		HashMap<String, String> map = new HashMap<String, String>();
		for(int i = 0; i < data.length; i+=2)
			map.put(data[i], data[i + 1]);
		
		Packet packet = new Packet(router, tag, sub_tag, map);
		if(log)
			Logger.log(packet);
		return packet;
	}
}