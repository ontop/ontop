/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.owlrefplatform.exception.MemoryLowException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryUtils {
	
	static Logger log = LoggerFactory.getLogger(MemoryUtils.class);
	
	public static void checkAvailableMemory() throws MemoryLowException {
//		
//        
//        
//		Runtime run = Runtime.getRuntime();
////		double used = run.maxMemory() - run.freeMemory();
//		double used = run.totalMemory() - run.freeMemory();
////		double used = run.totalMemory();
//		double maxmem = run.maxMemory();
//		double percentused = used / maxmem;
//		// log.debug("Checking: {}", percentused);
//		if (percentused > .90) {
//			/*
//			 * More than 75% is being used, dangerous, abort before swaping
//			 * ocurss
//			 */
//			log.debug("Total memory: {} Free memory: {}", run.totalMemory()/1024/1024, run.freeMemory()/1024/1024 );
//			log.debug("Total memory: {}", run.totalMemory()/1024/1024);
//			MemoryLowException memex = new MemoryLowException(
//			"The process was aborted because memory was running dangerously low. Please increase your JVM's max memory. Used memory: " + (long)(used/1024/1024) + " Max memory: " + (long)(maxmem/1024/1024));
//			memex.fillInStackTrace();
//			throw memex;
//		}
	}
	
	
}
