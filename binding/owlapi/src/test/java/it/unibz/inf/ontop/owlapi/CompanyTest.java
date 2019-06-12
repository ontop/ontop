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
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

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
	Connection sqlConnection;

//	public CompanyTest() {
//		super(owlfile, obdafile);
//	}

	@Before
	public void setUp() throws Exception {

		sqlConnection = DriverManager.getConnection("jdbc:h2:mem:questjunitdb","sa", "");
		java.sql.Statement s = sqlConnection.createStatement();
		String text = new Scanner( new File("src/test/resources/optional/company-h2.sql") ).useDelimiter("\\A").next();
		s.execute(text);
		s.close();

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFile)
				.nativeOntopMappingFile(obdaFile)
				.propertyFile(propertyfile)
				.build();

		/*m
		 * Create the instance of Quest OWL reasoner.
		 */
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

		reasoner = factory.createReasoner(config);
		conn = reasoner.getConnection();



	}

	@After
	public void tearDown() throws Exception {
		try {
			dropTables();
			conn.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void dropTables() throws SQLException, IOException {

		Statement st = sqlConnection.createStatement();

		FileReader reader = new FileReader("src/test/resources/optional/drop-company.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		st.close();
		sqlConnection.commit();
	}

	@Test
	public void runOptionalTest() throws Exception {

		OWLStatement st = conn.createStatement();


		String queryEx =  "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT * WHERE"
				+ "{  ?v ?w  ?x } ";

		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT * WHERE"
				+ "{ :A a :Company .  OPTIONAL  {  ?x :companyName :A .  ?x :depName \"HR\" .  OPTIONAL{?z :depId ?x " +
				"}}}";

		try {

			System.out.println(query);

			TupleOWLResultSet rs2 = st.executeSelectQuery(query);

			assertTrue(rs2.hasNext());
            final OWLBindingSet bindingSet = rs2.next();
            OWLObject ind2 = bindingSet.getOWLIndividual("z");
			OWLObject ind3 = bindingSet.getOWLIndividual("x");

			assertEquals("<http://it.unibz.krdb/obda/test/company#mark>", ind2.toString());
			assertEquals("<http://it.unibz.krdb/obda/test/company#1>", ind3.toString());

			assertFalse(rs2.hasNext());

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
		}
	}

	@Test
	public void runOptionalFilterTest() throws Exception {

		OWLStatement st = conn.createStatement();

		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT ?y?z WHERE"
				+ "{ ?c a :Company . Filter (?c=:A) OPTIONAL  {  ?x :companyName ?c .  ?x :depName ?y .  FILTER (?y =" +
				" \"HR\") OPTIONAL{?z :depId ?x }}}";


		try {

			System.out.println(query);

			TupleOWLResultSet  rs2 = st.executeSelectQuery(query);

			assertTrue(rs2.hasNext());
            final OWLBindingSet bindingSet = rs2.next();
            OWLLiteral ind1 = bindingSet.getOWLLiteral("y");
			OWLObject ind2 = bindingSet.getOWLIndividual("z");

			assertEquals("HR", ind1.getLiteral());
			assertEquals("<http://it.unibz.krdb/obda/test/company#mark>", ind2.toString());
			assertFalse(rs2.hasNext());
		} catch (Exception e) {
			throw e;
		} finally {
			st.close();
		}
	}

	@Test
	public void runSPOWithFilterTest() throws Exception {

		OWLStatement st = conn.createStatement();

		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> " +
				" SELECT * WHERE"
				+ "{ ?c a :Department . ?c ?p ?o .}";


		try {

			System.out.println(query);

			TupleOWLResultSet  rs2 = st.executeSelectQuery(query);

			while(rs2.hasNext()){
				final OWLBindingSet bindings = rs2.next();
				System.out.println(bindings);
			}


		} catch (Exception e) {
			throw e;
		} finally {
			st.close();
		}
	}

}
