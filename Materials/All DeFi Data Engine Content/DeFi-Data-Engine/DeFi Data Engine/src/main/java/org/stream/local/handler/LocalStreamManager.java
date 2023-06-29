package org.stream.local.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

import org.reflections.Reflections;

public class LocalStreamManager {

	@SuppressWarnings("unused")
	private final LocalStreamHandler handler;
	private final HashMap<String, Class<? extends LocalStreamConnection>> templates;
	private LocalStreamConnection stream;
	
	protected LocalStreamManager(LocalStreamHandler handler) {
		this.handler = handler;
		
		templates = new HashMap<String, Class<? extends LocalStreamConnection>>();
		
		try {
			reflect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void reflect() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Reflections reflection = new Reflections("org.stream.local.connected.connections");
		Set<Class<? extends LocalStreamConnection>> types = reflection.getSubTypesOf(LocalStreamConnection.class);
		for(Class<? extends LocalStreamConnection> c : types) {
			String uuid = c.getDeclaredConstructor(LocalStreamManager.class).newInstance(this).getUUID();
			if(templates.containsKey(uuid)) {
				System.err.println(String.format("Local stream UUID <%s> is not unique.", uuid));
				System.exit(1);
			}
			templates.put(uuid, c);
		}
	}
	
	protected boolean containsTemplate(String type) {
		return templates.containsKey(type);
	}
	
	protected boolean isStreamDefined() {
		return stream != null;
	}
	
	protected boolean setStream(String type) {
		if(!templates.containsKey(type))
			return false;
		
		try {
			stream = templates.get(type).getDeclaredConstructor(LocalStreamManager.class).newInstance(this);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return stream.init();
	}
	
	protected String streamType() {
		if(stream == null)
			return null;
		
		return stream.getUUID();
	}
	
	protected boolean authorize() {
		return stream.authorize();
	}
	
	protected boolean isAuthorized() {
		return stream.isAuthorized();
	}
	
	protected boolean isReady() {
		return stream.isReady();
	}
	
	protected boolean validate(String... query) {
		return stream.validate(query);
	}
	
	protected boolean scan(String... query) {
		if(!stream.validate(query))
			return false;
		
		return stream.contains(query);
	}
	
	protected boolean contains(String... query) {
		if(!validate(query))
			return false;
		
		return stream.contains(query);
	}
	
	public DataState state(String... query) {
		if(!validate(query))
			return DataState.INVALID;
		
		return stream.state(query);
	}
	
	protected Set<String> get(String... query) {
		if(!isReady() || !validate(query))
			return null;
		
		return stream.get(query);
	}
	
	protected boolean push(String data, String collection) {
		if(!isReady())
			return false;
		
		return stream.push(data, collection);
	}
	
	protected boolean modify(String data, String... query) {
		if(!validate(query))
			return false;
		
		return stream.modify(data, query);
	}
	
	protected Integer getParameterTranslation(String protocol, String parameter) {
		return stream.getParameterTranslation(protocol, parameter);
	}
}