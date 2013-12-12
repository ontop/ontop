package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
