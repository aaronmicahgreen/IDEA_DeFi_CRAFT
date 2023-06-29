package org.core.logger;

/**
 * {@link LogSeverity} is a enum class used by all processes that interact
 * with the {@link Logger} class. There are several values which are used
 * to determine the severity of the message passed to the {@link Logger}.
 * 
 * {@link LogSeverity#INFO}: General information regarding the system.
 * {@link LogSeverity#WARNING}: Warnings about system inconsistencies.
 * {@link LogSeverity#ERROR}: Errors that cause system failure.
 * 
 * @author Conor Flynn
 *
 */
public enum LogSeverity {

	INFO("INFO"),
	WARNING("WARNING"),
	ERROR("ERROR");
	
	private final String tag;
	
	private LogSeverity(String tag) {
		this.tag = tag;
	}
	
	/**
	 * Tag related to the enum for printing.
	 * 
	 * @return String corresponding to the given enum.
	 */
	public String getTag() {
		return tag;
	}
}
