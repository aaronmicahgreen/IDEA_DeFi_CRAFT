package org.framework.interfaces;

import java.util.HashMap;

/**
 * Interface used for requiring components to have a unique hash based on the passed data.
 * The hash does not require any standard formatting so long as it is unique.
 * <br>
 * The standard algorithm that will be used is a salted SHA-512.
 * 
 * @author Conor Flynn
 */
public interface Hash {
	/**
	 * Unique hash based on the passed data for identification. Algorithm is recommended to be
	 * a salted SHA-512.
	 * 
	 * @param data {@link HashMap<String, String>} which holds all data, primarily that used for authorization.
	 * @return {@link String} that contains the newly created hash.
	 */
	public String getHash(HashMap<String, String> data);
}
