package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.owlrefplatform.exception.MemoryLowException;

public class MemoryUtils {
	
	
	public static void checkAvailableMemory() throws MemoryLowException {
		Runtime run = Runtime.getRuntime();
		double used = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
		double maxmem = run.maxMemory();
		double percentused = used / maxmem;
		// log.debug("Checking: {}", percentused);
		if (percentused > .90) {
			/*
			 * More than 75% is being used, dangerous, abort before swaping
			 * ocurss
			 */
			MemoryLowException memex = new MemoryLowException(
			"The process was aborted because memory was running dangerously low. Please increase your JVM's max memory. Used memory: " + (long)(used/1024/1024) + " Max memory: " + (long)(maxmem/1024/1024));
			memex.fillInStackTrace();
			throw memex;
		}
	}
	
	
}
