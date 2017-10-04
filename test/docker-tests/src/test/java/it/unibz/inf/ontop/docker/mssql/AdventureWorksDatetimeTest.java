package it.unibz.inf.ontop.docker.mssql;

/*
 * #%L
 * ontop-test
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/***
 * Tests that SQL Server returns the datetimes correctly
 */
public class AdventureWorksDatetimeTest extends AbstractVirtualModeTest {

	private OWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());

	static final String owlFile = "/mssql/adventureWorks.owl";
	static final String obdaFile = "/mssql/adventureWorks.obda";
	static final String propertiesFile = "/mssql/adventureWorks.properties";

	public AdventureWorksDatetimeTest() {
		super(owlFile, obdaFile, propertiesFile);
	}


    /**
	 * Test use of datetime with jtds driver
	 * @throws Exception
	 */
	@Test
	public void testDatetime() throws Exception {

		String query =  "PREFIX : <http://knova.ru/adventureWorks.owl#>\n" +
				"SELECT DISTINCT ?x ?y { ?y :SpecialOffer_ModifiedDate ?x }";
		String val = runQueryAndReturnStringOfLiteralX(query);
		assertEquals("\"2005-05-02T00:00:00+02:00\"^^xsd:dateTime", val);
	}





}

