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

/***
 * Oracle long name.
 */
public class OracleLongNameTest extends AbstractVirtualModeTest {

	
	final static String owlFile = "/oracle/oraclesql/o.owl";
	final static String obdaFile1 = "/oracle/oraclesql/o1.obda";
	final static String propertyFile = "/oracle/oracle.properties";

	public OracleLongNameTest() {
		super(owlFile, obdaFile1, propertyFile);
	}


	
	
	/**
	 * Short variable name
	 */
	@Test
	public void testShortVarName() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT ?x WHERE { ?x a :Country}";
		checkThereIsAtLeastOneResult(query);
	}

	/**
	 * Short variable name
	 */
	@Test
	public void testLongVarName() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT ?veryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongVarName WHERE { ?veryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongVarName a :Country}";
		checkThereIsAtLeastOneResult(query);
	}
}

