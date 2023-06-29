package org.framework.router;

import java.util.Arrays;
import java.util.HashMap;

public class ResponseFactory {

	public static void responseNotHandled(String message) {
		System.err.println(message);
		System.exit(1);
	}
	
	/**
	 * Blank template response used for sending non-required responses
	 * 
	 * @return A new {@link Response} object with a response code of 0 and a blank response message.
	 */
	public static Response response0() {
		return Response.create(0, "");
	}
	
	public static Response response200() {
		return Response.create(200, "Successful Response.");
	}
	
	public static Response response200(String data) {
		return Response.create(200, "", data);
	}
	
	public static Response response220(String hash) {
		return Response.create(220, String.format("Stream with generated hash <%s> already exists. Using existing stream for connections.", hash), String.format("%s", hash));
	}
	
	public static Response response400(String router) {
		return Response.create(400, String.format("Router <%s> has not been connected to a network and cannot send Packets. "
				+ "Connect to another Router before sending Packets.", router));
	}
	
	public static Response response404(String router, String destination) {
		return Response.create(404, String.format("Destination with tag <%s> was not found within Router <%s>. Check Router "
				+ "connections to make sure all necessary connections are made.", router, destination));
	}
	
	public static Response response405(String router, String subtag) {
		return Response.create(405, String.format("Router <%s> does not contain sub process <%s>. Check to make sure subtag "
				+ "is written properly.", router, subtag));
	}
	
	public static Response response407(String router, String tag, String sub_tag, String... data) {
		return Response.create(407, String.format("Malformed packet when sending data from <%s> to <%s> using protocol "
				+ "<%s>. Data does not contain an even amount of <key, value> pairs. Data <%s>.", 
				router, tag, sub_tag, Arrays.toString(data)));
	}
	
	public static Response response410(String router, String subtag) {
		return Response.create(410, String.format("Router <%s> process <%s> is formatted incorrectly. Check to make sure the process' "
				+ "method contains the proper format of <public final Response methodName(Packet)>.", router, subtag));
	}
	
	public static Response response420(String source) {
		return Response.create(420, String.format("Requested data source <%s> does not exist in cache.", source));
	}
	
	public static Response response421(String hash) {
		return Response.create(421, String.format("Requested data stream with given hash <%s> does not exist in cache.", hash));
	}
	
	public static Response response422(String source) {
		return Response.create(422, String.format("Failure to authorize the external data source <%s> with the given properties.", source));
	}
	
	public static Response response423(String hash) {
		return Response.create(423, String.format("Stream with hash <%s> is not ready and cannot be executed.", hash));
	}
	
	public static Response response424(String hash) {
		return Response.create(424, String.format("Stream with hash <%s> is already active and cannot be executed again.", hash));
	}
	
	public static Response response425(String hash) {
		return Response.create(425, String.format("Stream with hash <%s> is not active and cannot be killed.", hash));
	}
	
	public static Response response426(String hash, String subscription) {
		return Response.create(426, String.format("Stream with hash <%s> does not contain a subscription request of type <%s>.", hash, subscription));
	}
	
	public static Response response427(String type, String response) {
		return Response.create(427, String.format("Stream of type <%s> returned an irregular response when attempting to send request. Response returned is: <%s>", type, response));
	}
	
	public static Response response428(String hash, String request) {
		return Response.create(428, String.format("Stream with hash <%s> does not contain a request type of <%s>", hash, request));
	}
	
	public static Response response429(String hash, String request, String response) {
		return Response.create(429, String.format("Stream with hash <%s> returned an irregular response when attempting to subscribe to <%s>. Response returned is: <%s>", hash, request, response));
	}
	
	public static Response response430(HashMap<String, String> data) {
		return Response.create(430, String.format("Stream hash could not be generated with the given properties: <%s>", data));
	}
	
	public static Response response440(String source) {
		return Response.create(440, String.format("Requested data source <%s> does not exist in cache.", source));
	}
	
	public static Response response441(String source) {
		return Response.create(441, String.format("Local data stream with source <%s> is not ready to handle queries.", source));
	}
	
	public static Response response442(String source) {
		return Response.create(442, String.format("Failed to add local data source <%s>.", source));
	}
	
	public static Response response443(String source) {
		return Response.create(443, String.format("Local data stream with source <%s> already exists.", source));
	}
	
	public static Response response444(String source) {
		return Response.create(444, String.format("Failure to authorize the local data source <%s> with the given properties.", source));
	}
	
	public static Response response445(String source, String query) {
		return Response.create(445, String.format("Local data stream with source <%s> could not validate passed query <%s>.", source, query));
	}
	
	public static Response response446(String source, String query) {
		return Response.create(446, String.format("Local data stream with source <%s> does not contain data from requested query <%s>.", source, query));
	}
	
	public static Response response447(String source, String query) {
		return Response.create(447, String.format("Local data stream with source <%s> failed to process the query <%s>.", source, query));
	}
	
	public static Response response448(String source, String query) {
		return Response.create(448, String.format("Local data stream with source <%s> failed to retrieve state with the query <%s>.", source, query));
	}
	
	public static Response response449(String source, String data, String... location) {
		return Response.create(449, String.format("Local data stream with source <%s> failed to push data point <%s> to given location <%s>", source, data, Arrays.toString(location)));
	}
	
	public static Response response460(String consumer) {
		return Response.create(460, String.format("Output consumer <%s> failed to listen to consumption channel.", consumer));
	}
	
	public static Response response470(String producer) {
		return Response.create(470, String.format("Output producer <%s> failed to listen to production channel.", producer));
	}
	
	public static Response response471(String key) {
		return Response.create(471, String.format("Output manager does not contain destination with key <%s>", key));
	}
	
	public static Response response472(String key) {
		return Response.create(472, String.format("Output producer failed to send data to external connection <%s>.", key));
	}
	
	public static Response response500(String loc, String parameter) {
		return Response.create(500, String.format("Engine component <%s> missing required parameter <%s>. Recall action with proper formatting.", loc, parameter));
	}
	
	public static Response response501() {
		return Response.create(501, String.format("Fatal error occurred. This response should not be displayed."));
	}
	
	public static Response response501(String message) {
		return Response.create(501, String.format("Fatal error occurred. This response should not be displayed. Message: <%s>", message));
	}
	
	public static Response response502() {
		return Response.create(502, String.format("Internal language failure. This error is commonly causes by a static protocol being treated as a live protocol or vice versa."));
	}
	
	public static Response response503(String format, String... dates) {
		return Response.create(503, String.format("Local data stream failed to process date of the format <%s> from given strings <%s>", format, Arrays.toString(dates)));
	}
}
