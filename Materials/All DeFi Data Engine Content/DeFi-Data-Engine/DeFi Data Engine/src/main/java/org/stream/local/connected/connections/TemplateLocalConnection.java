package org.stream.local.connected.connections;

import java.util.HashSet;
import java.util.Set;

import org.properties.Config;
import org.stream.local.handler.DataState;
import org.stream.local.handler.LocalStreamConnection;
import org.stream.local.handler.LocalStreamManager;

public class TemplateLocalConnection extends LocalStreamConnection {

	public TemplateLocalConnection(LocalStreamManager manager) {
		super(manager);
	}

	@Override
	public String getUUID() {
		return "local_template";
	}

	public boolean init() {
		return true;
	}
	
	@Override
	public boolean authorize() {
		Config.setProperty("testing", "lsh.authorized", "true");
		return true;
	}

	@Override
	public boolean isAuthorized() {
		return Config.getProperty("testing", "lsh.authorized").equals("true");
	}

	@Override
	public boolean isReady() {
		return Config.getProperty("testing", "lsh.ready").equals("true");
	}

	@Override
	public boolean validate(String... query) {
		return query[0].equals("valid") || 
				query[0].equals("dne") ||
				query[0].equals("irregular");
	}

	@Override
	public boolean contains(String... query) {
		return query[0].equals("valid")	|| query[0].equals("irregular");
	}

	@Override
	public DataState state(String... query) {
		if(query[0].equals("inv"))
			return DataState.INVALID;
		else if(query[0].equals("dne"))
			return DataState.DOES_NOT_EXIST;
		else if(query[0].equals("partial"))
			return DataState.PARTIAL;
		else if(query[0].equals("valid"))
			return DataState.EXISTS;
		else if(query[0].equals("modified"))
			return DataState.MODIFIED;
		else if(query[0].equals("corrupted"))
			return DataState.CORRUPTED;
		return DataState.INVALID;
	}

	@Override
	public Set<String> get(String... query) {
		if(query[0].equals("irregular"))
			return null;
		HashSet<String> out = new HashSet<String>();
		out.add("123");
		return out;
	}
	
	public boolean push(String data, String collection) {
		return collection.equals("valid");
	}

	@Override
	public boolean modify(String data, String... query) {
		return true;
	}
	
	public Integer getParameterTranslation(String protocol, String parameter) {
		return 1;
	}
}
