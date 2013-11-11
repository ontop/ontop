/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package inf.unibz.ontop.sesame.tests.general;
import org.junit.Test;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SesameRepoTest {
Logger log = LoggerFactory.getLogger("newlogger");
	@Test
	public void test() {
		RemoteRepositoryManager m = new RemoteRepositoryManager("http://localhost:8080/openrdf-sesame");
		log.info("New message");
	}

}
