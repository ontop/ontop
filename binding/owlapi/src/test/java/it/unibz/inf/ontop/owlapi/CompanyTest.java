package it.unibz.inf.ontop.owlapi;

/*
 * #%L
 * ontop-quest-owlapi3
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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;

import java.sql.Connection;
import java.sql.DriverManager;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static junit.framework.TestCase.*;


/***
 * Check how the optional filter is converted in left join
 * We do not support this kind of SPARQL query because it is not a well designed graph pattern
 *
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class CompanyTest  {

	private static final String owlFile = "src/test/resources/optional/company.owl";
	private static final String obdaFile = "src/test/resources/optional/company.obda";
	private static final String propertyfile = "src/test/resources/optional/company.properties";

	private OntopOWLReasoner reasoner;
	private OWLConnection conn;
	private Connection sqlConnection;

	@Before
	public void setUp() throws Exception {

		sqlConnection = DriverManager.getConnection("jdbc:h2:mem:questjunitdb","sa", "");
		executeFromFile(sqlConnection, "src/test/resources/optional/company-h2.sql");

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFile)
				.nativeOntopMappingFile(obdaFile)
				.propertyFile(propertyfile)
				.build();

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

		reasoner = factory.createReasoner(config);
		conn = reasoner.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		executeFromFile(sqlConnection,"src/test/resources/optional/drop-company.sql");
		conn.close();
	}

	@Test
	public void runOptionalTest() throws Exception {

		String queryEx =  "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT * WHERE"
				+ "{  ?v ?w  ?x } ";

		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT * WHERE"
				+ "{ :A a :Company .  OPTIONAL  {  ?x :companyName :A .  ?x :depName \"HR\" .  OPTIONAL{?z :depId ?x " +
				"}}}";

		try (OWLStatement st = conn.createStatement()) {

			System.out.println(query);

			TupleOWLResultSet rs2 = st.executeSelectQuery(query);

			assertTrue(rs2.hasNext());
            final OWLBindingSet bindingSet = rs2.next();
            OWLObject ind2 = bindingSet.getOWLIndividual("z");
			OWLObject ind3 = bindingSet.getOWLIndividual("x");

			assertEquals("<http://it.unibz.krdb/obda/test/company#mark>", ind2.toString());
			assertEquals("<http://it.unibz.krdb/obda/test/company#1>", ind3.toString());

			assertFalse(rs2.hasNext());

		}
	}

	@Test
	public void runOptionalFilterTest() throws Exception {

		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT ?y?z WHERE"
				+ "{ ?c a :Company . Filter (?c=:A) OPTIONAL  {  ?x :companyName ?c .  ?x :depName ?y .  FILTER (?y =" +
				" \"HR\") OPTIONAL{?z :depId ?x }}}";


		try (OWLStatement st = conn.createStatement()) {
			System.out.println(query);

			TupleOWLResultSet  rs2 = st.executeSelectQuery(query);

			assertTrue(rs2.hasNext());
            final OWLBindingSet bindingSet = rs2.next();
            OWLLiteral ind1 = bindingSet.getOWLLiteral("y");
			OWLObject ind2 = bindingSet.getOWLIndividual("z");

			assertEquals("HR", ind1.getLiteral());
			assertEquals("<http://it.unibz.krdb/obda/test/company#mark>", ind2.toString());
			assertFalse(rs2.hasNext());
		}
	}

	@Ignore
	@Test
	public void runSPOWithFilterTest() throws Exception {

		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> " +
				" SELECT * WHERE"
				+ "{ ?c a :Department . ?c ?p ?o .}";

		try (OWLStatement st = conn.createStatement()) {

			System.out.println(query);

			TupleOWLResultSet  rs2 = st.executeSelectQuery(query);

			while(rs2.hasNext()){
				final OWLBindingSet bindings = rs2.next();
				System.out.println(bindings);
			}
		}
	}
}
