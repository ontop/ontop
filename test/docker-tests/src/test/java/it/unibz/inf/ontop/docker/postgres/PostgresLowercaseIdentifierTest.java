package it.unibz.inf.ontop.docker.postgres;

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
 * Tests that oracle identifiers for tables and columns are treated
 * correctly. Especially, that the unquoted identifers are treated as uppercase, and
 * that the case of quoted identifiers is not changed
 */
public class PostgresLowercaseIdentifierTest extends AbstractVirtualModeTest {

	static final String owlfile = "/pgsql/identifiers/identifiers.owl";
	static final String obdafile = "/pgsql/identifiers/identifiers-lowercase-postgres.obda";
	static final String propertyfile = "/pgsql/identifiers/identifiers-lowercase-postgres.properties";

	public PostgresLowercaseIdentifierTest() {
		super(owlfile, obdafile, propertyfile);
	}

	/**
	 * Test use of unquoted uppercase to access lowercase column and table names in postgres 
	 * (Postgres converts unquoted identifer letters to lowercase)
	 * @throws Exception
	 */
	@Test
	public void testLowercaseUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country} ORDER BY ?x";
		checkThereIsAtLeastOneResult(query);
	}

			
}
