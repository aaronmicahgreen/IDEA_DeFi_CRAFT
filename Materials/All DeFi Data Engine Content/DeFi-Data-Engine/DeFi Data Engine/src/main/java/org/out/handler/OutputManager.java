package org.out.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.framework.router.Packet;
import org.framework.router.Response;
import org.properties.Config;
import org.reflections.Reflections;

public class OutputManager {
	
	private final OutputHandler handler;
	private final HashMap<String, Class<? extends OutputConsumer>> consumer_templates;
	private final HashMap<String, Class<? extends OutputProducer>> producer_templates;
	private final HashSet<OutputConsumer> consumers;
	private final HashSet<OutputProducer> producers;
	
	private final HashMap<String, OutputDestination> destinations;
	
	public OutputManager(OutputHandler handler) {
		this.handler = handler;
		this.consumer_templates = new HashMap<String, Class<? extends OutputConsumer>>();
		this.producer_templates = new HashMap<String, Class<? extends OutputProducer>>();
		this.consumers = new HashSet<OutputConsumer>();
		this.producers = new HashSet<OutputProducer>();
		this.destinations = new HashMap<String, OutputDestination>();
		
		// reflect all consumer/producer types
		try {
			reflect();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// load all output connections
		try {
			load();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
	}
	
	private void reflect() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Reflections consumer_reflection = new Reflections("org.out.consumers");
		Set<Class<? extends OutputConsumer>> consumer_types = consumer_reflection.getSubTypesOf(OutputConsumer.class);
		for(Class<? extends OutputConsumer> c : consumer_types)
			consumer_templates.put(c.getDeclaredConstructor(OutputManager.class).newInstance(this).getUUID(), c);
		
		Reflections producer_reflection = new Reflections("org.out.producers");
		Set<Class<? extends OutputProducer>> producer_types = producer_reflection.getSubTypesOf(OutputProducer.class);
		for(Class<? extends OutputProducer> c : producer_types)
			producer_templates.put(c.getDeclaredConstructor(OutputManager.class).newInstance(this).getUUID(), c);
	}
	
	private void load() {
		// load consumers:
		String[] consumer_types = Config.getProperty("stream", "general.consumer.types").replaceAll(" ", "").split(",");
		for(String type : consumer_types) {
			if(type.equals("null"))
				continue;
			
			OutputConsumer consumer = null;
			try {
				consumer = consumer_templates.get(type).getDeclaredConstructor(OutputManager.class).newInstance(this);
				consumers.add(consumer);
			} catch(Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		// load producers:
		String[] producer_types = Config.getProperty("stream", "general.producer.types").replaceAll(" ", "").split(",");
		for(String type : producer_types) {
			if(type.equals("null"))
				continue;
			
			OutputProducer producer = null;
			try {
				producer = producer_templates.get(type).getDeclaredConstructor(OutputManager.class).newInstance(this);
				producers.add(producer);
			} catch(Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public Object[] consumerListen() {
		// execute all consumers
		for(OutputConsumer consumer : consumers)
			if(!consumer.init() || !consumer.listen())
				return new Object[] {false, consumer.getUUID()};
		
		return new Object[] {true, ""};
	}
	
	public Object[] producerListen() {
		// execute all producers
		for(OutputProducer producer : producers)
			if(!producer.init() || !producer.listen())
				return new Object[] {false, producer.getUUID()};
		
		return new Object[] {true, ""};
	}
	
	public final Response send(String tag, String sub_tag, String... data) {
		return handler.send(tag, sub_tag, data);
	}
	
	public final synchronized void add(OutputDestination destination) {
		destinations.put(destination.getKey(), destination);
	}
	
	public final synchronized void remove(String key) {
		destinations.remove(key);
	}
	
	public final boolean containsDestination(String key) {
		return destinations.containsKey(key);
	}
	
	public final boolean send(String key, Packet packet) {
		if(!destinations.containsKey(key))
			return false;
		
		return destinations.get(key).send(packet);
	}
}