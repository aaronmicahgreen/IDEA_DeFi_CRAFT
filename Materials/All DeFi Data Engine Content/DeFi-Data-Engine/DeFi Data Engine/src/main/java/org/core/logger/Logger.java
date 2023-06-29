package org.core.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.framework.router.Packet;
import org.framework.router.Response;

public class Logger {
	
	public static final void log(String message) {
		System.out.println(messageFormat("INFO", message));
	}
	
	public static final void log(Packet packet) {
		System.out.println(packetFormat(packet));
	}
	
	public static final void log(Response response) {
		System.out.println(responseFormat(response));
	}
	
	public static final void warn(String message) {
		System.out.println(messageFormat("WARN", message));
	}
	
	public static final void warn(Packet packet) {
		System.err.println(packetFormat(packet));
	}
	
	public static final void warn(Response response) {
		System.err.println(responseFormat(response));
	}
	
	public static final void terminate(String message) {
		System.err.println(messageFormat("ERROR", message));
	}
	
	public static final void terminate(Packet packet) {
		System.err.println(packetFormat(packet));
		System.exit(1);
	}
	
	public static final void terminate(Response response) {
		System.err.println(responseFormat(response));
		System.exit(1);
	}
	
	private static final String messageFormat(String type, String message) {
		return String.format("[%s] [%-10s] %-9s- [%s]", 
				time(),
				Thread.currentThread().getName(),
				type,
				message);
	}
	
	private static final String packetFormat(Packet packet) {
		return String.format("[%s] [%-10s] PACKET   - [%3s -> %3s] [%4s] [%s]", 
				time(), 
				Thread.currentThread().getName(),
				packet.getSender(), 
				packet.getTag(),
				packet.getSubTag(),
				packet.getData());
	}
	
	private static final String responseFormat(Response response) {
		return String.format("[%s] [%-10s] RESPONSE - [%3d] [%s] [%s]",
				time(),
				Thread.currentThread().getName(),
				response.code(),
				response.message(),
				response.data());
	}
	
	private static final String time() {
		return LocalDateTime.now()
			       .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnnnn"));
	}
}
