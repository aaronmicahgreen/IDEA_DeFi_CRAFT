package org.main;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.core.core.Core;

public class Main {
	
	public static void main(String[] args) throws InterruptedException {
		// disable loggers
		LogManager.getRootLogger().setLevel(Level.OFF);
		
		// initialize new Core
		Thread thread = new Thread() {
			public void run() {
				new Core();
			}
		};
		
		// start thread
		thread.start();
		
		while(true) {
			Thread.sleep(10000);
		}
	}
}