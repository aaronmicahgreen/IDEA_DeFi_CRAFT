package org.framework.interfaces;

/**
 * The UUID interface is used for requiring reflected classes to have a unique id that
 * they can be referenced by. 
 * <br>
 * Standard syntax for a UUID is all lower case, no numbers, and words being separated
 * by _.
 */
public interface UUID {
	
	/**
	 * UUID of the implementing class. Recommended to follow standard syntax
	 * as referenced by {@link UUID}.
	 * 
	 * @return {@link String} representing the UUID of the implementing class.
	 */
	public String getUUID();
}
