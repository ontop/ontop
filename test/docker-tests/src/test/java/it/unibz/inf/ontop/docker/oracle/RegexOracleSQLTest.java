package it.unibz.inf.ontop.docker.oracle;

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

/**
 * Test to check if the sql parser supports regex correctly when written with oracle syntax. 
 * Translated in a datalog function and provides the correct results
 */
public class RegexOracleSQLTest extends AbstractVirtualModeTest {

	static final String owlfile = "/oracle/regex/stockBolzanoAddress.owl";
	static final String obdafile = "/oracle/regex/stockexchangeRegexLike.obda";
	static final String propertiesfile = "/oracle/oracle.properties";

	public RegexOracleSQLTest() {
		super(owlfile, obdafile, propertiesfile);
	}

	/**
	 * Test use of regex in Oracle
	 * select id, street, number, city, state, country from address where  regexp_like(city, 'b.+z', 'i')
	 * @throws Exception
	 */
	@Test
	public void testOracleRegexLike() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :BolzanoAddress}";
		countResults(query, 2);
	}
	
	/**
	 * Test use of regex in Oracle
	 * select "ID", "NAME", "LASTNAME", "DATEOFBIRTH", "SSN" from "BROKER" where regexp_like("NAME", 'J.+a')
	 * @throws Exception
	 */
	@Test
	public void testOracleRegexLikeUppercase() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :PhysicalPerson}";
		countResults(query, 1);
	}
	

	
	
	

		
}
