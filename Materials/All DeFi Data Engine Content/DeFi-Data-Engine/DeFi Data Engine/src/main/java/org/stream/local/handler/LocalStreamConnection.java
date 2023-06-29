package org.stream.local.handler;

import java.util.Set;

import org.framework.interfaces.UUID;

public abstract class LocalStreamConnection implements UUID {

	@SuppressWarnings("unused")
	private final LocalStreamManager manager;
	
	public LocalStreamConnection(LocalStreamManager manager) {
		this.manager = manager;
	}
	
	public abstract boolean init();
	public abstract boolean authorize();
	public abstract boolean isAuthorized();
	public abstract boolean isReady();
	public abstract boolean validate(String... query);
	public abstract boolean contains(String... query);
	public abstract DataState state(String... query);
	public abstract Set<String> get(String... query);
	public abstract boolean push(String data, String collection);
	public abstract boolean modify(String data, String... query);
	public abstract Integer getParameterTranslation(String protocol, String parameter);
}