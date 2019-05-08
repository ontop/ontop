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
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/***
 * Tests that jtds jdbc driver for SQL Server returns the datatypes correctly
 */
public class JTDSDatatypesTest extends AbstractVirtualModeTest {

	static final String owlfile = "/mssql/datatype/datatypesjtds.owl";
	static final String obdafile = "/mssql/datatype/datatypejtds.obda";
	static final String propertiesfile = "/mssql/datatype/datatypejtds.properties";

	public JTDSDatatypesTest() {
		super(owlfile, obdafile, propertiesfile);
	}


	/**
	 * Test use of datetime with jtds driver
	 *
	 * NB: no timezone stored in the DB (DATETIME column type)
	 *
	 */
	@Test
	public void testDatetime() throws Exception {

		String query =  "PREFIX : <http://knova.ru/adventureWorks.owl#>\n" +
				"SELECT DISTINCT ?s ?x { ?s :SpecialOffer_ModifiedDate ?x }";
		String val = runQueryAndReturnStringOfLiteralX(query);
		assertEquals("\"2005-05-02T00:00:00\"^^xsd:dateTime", val);
	}





}

