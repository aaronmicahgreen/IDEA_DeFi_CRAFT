package org.framework.router;

import org.core.logger.Logger;
import org.properties.Config;

/**
 * The {@link Response} class is used to relay information from a
 * given {@link Packet} sent through a {@link Router}. {@link Response}
 * objects contain two fields: code and message. 
 * 
 * The code refers to the response code listed in the documentation, which
 * can be used to determine certain interactions the sent {@link Packet} may 
 * have had.
 * 
 * The message refers to the message sent with the response code, which may
 * contain more detailed information of the response. This value may be left
 * blank based on the response code as some codes do not need any more information
 * than what is provided.
 * 
 * @author Conor Flynn
 *
 */
public final class Response {
	
	private final static boolean log = Config.getProperty("app", "general.logging.responses").equals("true");

	private final int code;
	private final String message;
	private final String data;

	/**
	 * Constructor used for creating a new {@link Response} object. Must
	 * be accessed through the {@link Response#create(int, String, String)} function.
	 * 
	 * @param code Response code to be sent.
	 * @param message Message to be sent to accompany the response code.
	 */
	private Response(int code, String message) {
		this.code = code;
		this.message = message;
		this.data = "";
	}
	
	/**
	 * Constructor used for creating a new {@link Response} object. Must
	 * be accessed through the {@link Response#create(int, String, String)} function.
	 * 
	 * @param code Response code to be sent.
	 * @param message Message to be sent to accompany the response code.
	 * @param data {@link String} of data to be returned in the response.
	 */
	private Response(int code, String message, String data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}
	
	/**
	 * Response code of the {@link Response} object. Gives high level overview of the
	 * sent {@link Packet} object's response.
	 * 
	 * See documentation for more detailed explanation of all response codes.
	 * 
	 * @return {@link Integer} value representing response from {@link Packet} submission.
	 */
	public int code() {
		return code;
	}
	
	/**
	 * Response message which is used for containing more detailed information about the
	 * response code if necessary. This field may be left blank if not needed.
	 * 
	 * @return {@link String} value representing the response message.
	 */
	public String message() {
		return message;
	}
	
	/**
	 * {@link String} of all data contained within the {@link Response} object. Parameter
	 * is optional and will return an empty {@link String} if not defined on initialization.
	 * 
	 * @return {@link String} containing all returned data by the {@link Response} object.
	 */
	public String data() {
		return data;
	}
	
	/**
	 * Static function used for creating a new {@link Response} object. Formats and returns
	 * the new response based on the parameters included below.
	 * 
	 * @param code Response code of the {@link Response} object.
	 * @param message Response message of the {@link Response} object. Uses {@link String#format(String, Object...)} for formatting with {@code args} parameter.
	 * @return New {@link Response} object formatted based on the passed parameters.
	 */
	protected static Response create(int code, String message) {
		Response response = new Response(code, message);
		if(log)
			Logger.log(response);
		return response;
	}
	
	/**
	 * Static function used for creating a new {@link Response} object. Formats and returns
	 * the new response based on the parameters included below.
	 * 
	 * @param code Response code of the {@link Response} object.
	 * @param message Response message of the {@link Response} object. Uses {@link String#format(String, Object...)} for formatting with {@code args} parameter.
	 * @param data {@link String} of data to be returned in the response.
	 * @return New {@link Response} object formatted based on the passed parameters.
	 */
	protected static Response create(int code, String message, String data) {
		Response response = new Response(code, message, data);
		if(log)
			Logger.log(response);
		return response;
	}
}
